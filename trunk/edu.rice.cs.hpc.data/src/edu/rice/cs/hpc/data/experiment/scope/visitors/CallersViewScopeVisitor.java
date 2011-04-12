package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;


/*************************
 * A class that manages the initialization phase of the creation of callers tree
 * For the second phase of callers tree (incremental callers path) should be seen in CallSiteScopeCallerView
 * 
 * @author laksonoadhianto
 *
 */
public class CallersViewScopeVisitor extends CallerScopeBuilder implements IScopeVisitor {


	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	protected boolean isdebug = false; // true;
	protected Hashtable/*<int, Scope>*/ calleeht = new Hashtable/*<int, Scope>*/();
	
	protected Experiment exp;
	protected Scope callersViewRootScope;
	protected MetricValuePropagationFilter filter;
	protected int numberOfPrimaryMetrics;
	
	private final CombineCallerScopeMetric combinedMetrics;
	private final CombineProcedureScope combineProcedureMetrics;
	
	private final ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private final InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	
	final private ListCombinedScopes listCombinedScopes;

	
	/****--------------------------------------------------------------------------------****
	 * 
	 * @param experiment
	 * @param cvrs
	 * @param nMetrics
	 * @param dodebug
	 * @param filter
	 ****--------------------------------------------------------------------------------****/
	public CallersViewScopeVisitor(Experiment experiment, Scope cvrs, 
			int nMetrics, boolean dodebug, MetricValuePropagationFilter filter) {
		this.exp = experiment;
		this.callersViewRootScope = cvrs;
		this.numberOfPrimaryMetrics = nMetrics;
		this.isdebug = dodebug;
		this.filter = filter;

		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(experiment);
		inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(experiment);
		combinedMetrics = new CombineCallerScopeMetric();
		combineProcedureMetrics = new CombineProcedureScope();
		listCombinedScopes = new ListCombinedScopes();

	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		
		//--------------------------------------------------------------------------------
		// if there are no exclusive costs to attribute from this context, we are done here
		//--------------------------------------------------------------------------------
		if (!scope.hasNonzeroMetrics()) {
			return; 
		}
		
		if (scope.getCCTIndex() == 20) {
			System.out.println();
		}
		if (vt == ScopeVisitType.PreVisit) {
			
			// Find (or add) callee in top-level hashtable
			ProcedureScope callee = this.createProcedureIfNecessary(scope);
			
			this.combinedMetrics.setEntryScope(scope);

			LinkedList<CallSiteScopeCallerView> callPathList = createCallChain(IMergedScope.MergingStatus.INIT, scope, scope, 
					combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);

			//-------------------------------------------------------
			// ensure my call path is represented among my children.
			//-------------------------------------------------------
			mergeCallerPath(IMergedScope.MergingStatus.INIT, 0, callee, callPathList, 
					combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);

		} else if (vt == ScopeVisitType.PostVisit)  {

			//---------------------------------------------------------------------------
			// decrement all the combined scopes which are computed in this scope
			// 	When a caller view scope is created, it creates also its children and its merged children
			//		the counter of these children are then need to be decremented based on the CCT scope
			//---------------------------------------------------------------------------
			ArrayList<Scope> list = this.listCombinedScopes.getList(scope);
			if (list != null) {
				Iterator<Scope> iter = list.iterator();
				while (iter.hasNext()) {
					Scope combinedScope = iter.next();
					this.decrementCounter(combinedScope);
				}
			}
		}
	}
	
		 
	public void visit(Scope scope, ScopeVisitType vt) { }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	
	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		
		//--------------------------------------------------------------------------------
		// if there are no exclusive costs to attribute from this context, we are done here
		//--------------------------------------------------------------------------------
		if (!scope.hasNonzeroMetrics() || scope.isAlien()) {
			return; 
		}
		
		if (vt == ScopeVisitType.PreVisit) {
				// Find (or add) callee in top-level hashtable
			this.createProcedureIfNecessary(scope);

		} else if (vt == ScopeVisitType.PostVisit){
			
			ArrayList<Scope> list = this.listCombinedScopes.getList(scope);
			if (list != null) {
				Iterator<Scope> iter = list.iterator();
				while (iter.hasNext()) {
					Scope combinedScope = iter.next();
					this.decrementCounter(combinedScope);
				}
			}

/*			ProcedureScope callee = (ProcedureScope) calleeht.get(new Integer(scope.hashCode()));
			if  (callee != null) {
				this.decrementCounter(callee);

			}
*/		}
	
	}
	
	public void visit(AlienScope scope, ScopeVisitType vt) { }
	public void visit(LoopScope scope, ScopeVisitType vt) { }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { 	}
	public void visit(LineScope scope, ScopeVisitType vt) {  }
	public void visit(GroupScope scope, ScopeVisitType vt) { }

	
	//----------------------------------------------------
	// helper functions  
	//----------------------------------------------------
	
	
	/********
	 * Find caller view's procedure of a given scope. 
	 * If it doesn't exist, create a new one, attach to the tree, and copy the metrics
	 * @param cct_s: either call site or procedure
	 * @return
	 ********/
	private ProcedureScope createProcedureIfNecessary( Scope cct_s ) {
		ProcedureScope cct_proc_s;
		
		if (cct_s instanceof ProcedureScope)
			cct_proc_s = (ProcedureScope) cct_s;
		else
			cct_proc_s = ( (CallSiteScope)cct_s).getProcedureScope();
		
		Integer objCode = Integer.valueOf(cct_proc_s.hashCode());

		ProcedureScope caller_proc = (ProcedureScope) calleeht.get(objCode);
		
		if (caller_proc == null) {
			// create a new procedure scope
			caller_proc = (ProcedureScope) cct_proc_s.duplicate();
			
			// add to the tree
			callersViewRootScope.addSubscope(caller_proc);
			caller_proc.setParentScope(this.callersViewRootScope);
			
			// add to the dictionary
			calleeht.put(objCode, caller_proc);
		}
		
		// accumulate the metrics
		this.combineProcedureMetrics.combine(cct_s, caller_proc, cct_s);
		
		return caller_proc;
	}
		

	
	/********
	 * decrement the counter of a caller scope
	 * @param caller_s
	 ********/
	private void decrementCounter(Scope caller_s) {
		if (caller_s == null)
			return;

		caller_s.decrementCounter();
	}
	

	
	//----------------------------------------------------
	// helper classes  
	//----------------------------------------------------
	
	/********************************************************************
	 * class helper to store the list of combined scopes
	 * 
	 * @author laksonoadhianto
	 *
	 ********************************************************************/
	private class ListCombinedScopes {
		private HashMap<Integer, ArrayList<Scope>> combinedScopes;
		
		public void addList(Scope key, Scope combined) {
			if (this.combinedScopes == null) {
				this.combinedScopes = new HashMap<Integer, ArrayList<Scope>>();
			}
			int intKey = System.identityHashCode(key);
			ArrayList<Scope> list = this.combinedScopes.get(intKey);
			if (list == null) {
				list = new ArrayList<Scope>();
			}
			list.add(combined);
			this.combinedScopes.put(intKey, list);
		}
		
		
		public ArrayList<Scope> getList(Scope key) {
			int intKey = System.identityHashCode(key);
			return this.combinedScopes.get(intKey);
		}
	}
	
	
	/********************************************************************
	 * class to combine metrics from different scopes
	 * @author laksonoadhianto
	 *
	 ********************************************************************/
	private class CombineCallerScopeMetric extends AbstractCombineMetric {
		private Scope cct_entry;
		
		public void setEntryScope( Scope key ) {
			this.cct_entry = key;
		}

		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {

			if (target.isCounterZero() && inclusiveOnly != null) {
				target.safeCombine(source, inclusiveOnly);
			}
			if (exclusiveOnly != null)
				target.combine(source, exclusiveOnly);

			target.incrementCounter();
			
			if (this.cct_entry != null)
				listCombinedScopes.addList(cct_entry, target);
			else {
				System.err.println("CVSV error: no list of combined scopes for " + target + "\t source: " + source );
			}
		}
	}
	
	private class CombineProcedureScope extends AbstractCombineMetric {
		
		public void combine(Scope cct_entry, Scope target, Scope source) {
			listCombinedScopes.addList(cct_entry, target);
			this.combine(target, source, inclusiveOnly, exclusiveOnly);
		}
		
		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {
			
			super.combine_internal(target, source, inclusiveOnly, exclusiveOnly);

		}
	}
	
}
