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
	private final ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private final InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	
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


		if (vt == ScopeVisitType.PreVisit) { 
						
			// Find (or add) callee in top-level hashtable
			ProcedureScope callee = this.createProcedureIfNecessary(scope);
			
			this.combinedMetrics.setEntryScope(scope);

			LinkedList<CallSiteScopeCallerView> callPathList = createCallChain(scope, scope, 
					combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);

			//-------------------------------------------------------
			// ensure my call path is represented among my children.
			//-------------------------------------------------------
			mergeCallerPath(callee, callPathList, combinedMetrics, this.inclusiveOnly, this.exclusiveOnly);

		} else if (vt == ScopeVisitType.PostVisit)  {
			ProcedureScope mycallee  = scope.getProcedureScope();
			Integer objCode = new Integer(mycallee.hashCode());
			
			ProcedureScope callee = (ProcedureScope) calleeht.get(objCode);
			// it is nearly impossible that the callee is null but I prefer to do this in case we encounter
			//		a bug or a very strange call path
			if(callee != null) {
				this.decrementCounter(callee);
			}
			
			//---------------------------------------------------------------------------
			// decrement all the combined scopes which are computed in this scope
			// 	When a caller view scope is created, it creates also its children and its merged children
			//		the counter of these children are then need to be decremented based on the CCT scope
			//---------------------------------------------------------------------------
			ArrayList<Scope> list = this.combinedMetrics.getListCombinedScopes().getList(scope);
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
			ProcedureScope callee = (ProcedureScope) calleeht.get(new Integer(scope.hashCode()));
			if  (callee != null) {
				this.decrementCounter(callee);

			}
		}
	
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
			caller_proc.iCounter = 0;
			
			// add to the tree
			callersViewRootScope.addSubscope(caller_proc);
			caller_proc.setParentScope(this.callersViewRootScope);
			
			// add to the dictionary
			calleeht.put(objCode, caller_proc);
		}
		
		// accumulate the metrics
		this.combinedMetrics.combine(cct_s, caller_proc, cct_s);
		
		return caller_proc;
	}
		

	
	/********
	 * decrement the counter of a caller scope
	 * @param caller_s
	 ********/
	private void decrementCounter(Scope caller_s) {
		if (caller_s == null)
			return;

		if (caller_s.iCounter>0) {
			caller_s.iCounter--;
		} else {
			System.err.println("CVSV Err dec "+caller_s.getName()+" \t"+ caller_s.getClass() + "\t" + caller_s.hashCode());
		}
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
			
			ArrayList<Scope> list = this.combinedScopes.get(key);
			if (list == null) {
				list = new ArrayList<Scope>();
			}
			list.add(combined);
			this.combinedScopes.put(key.getCCTIndex(), list);
			
		}
		
		
		public ArrayList<Scope> getList(Scope key) {
			return this.combinedScopes.get(key.getCCTIndex());
		}
	}
	
	
	/********************************************************************
	 * class to combine metrics from different scopes
	 * @author laksonoadhianto
	 *
	 ********************************************************************/
	private class CombineCallerScopeMetric extends AbstractCombineMetric {
		final private ListCombinedScopes listCombinedScopes;
		private Scope cct_entry;
		
		public CombineCallerScopeMetric() {
			this.listCombinedScopes = new ListCombinedScopes();
		}
		
		public void combine(Scope cct_entry, Scope target, Scope source) {
			this.setEntryScope(cct_entry);
			this.combine(target, source, inclusiveOnly, exclusiveOnly);
		}
		
		public ListCombinedScopes getListCombinedScopes() {
			return this.listCombinedScopes;
		}

		
		public void setEntryScope( Scope key ) {
			this.cct_entry = key;
		}

		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {

			super.combine_internal(target, source, inclusiveOnly, exclusiveOnly);
			if (this.cct_entry != null)
				this.listCombinedScopes.addList(cct_entry, target);
		}
	}
}
