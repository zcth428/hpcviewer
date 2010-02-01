package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.Hashtable;
import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.AggregatePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.RemoveCallsiteCostPropagationFilter;

public class CallersViewScopeVisitor implements IScopeVisitor {


	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	protected boolean isdebug = false; // true;
	protected Hashtable/*<int, Scope>*/ calleeht = new Hashtable/*<int, Scope>*/();
	protected Experiment exp;
	protected Scope callersViewRootScope;
	protected MetricValuePropagationFilter filter;
	protected int numberOfPrimaryMetrics;
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
		BaseMetric []metrics = experiment.getMetrics();
		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(metrics);
		inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(metrics);
		new RemoveCallsiteCostPropagationFilter(metrics);
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		ProcedureScope mycallee  = scope.getProcedureScope();
		Integer objCode = new Integer(mycallee.hashCode());
		
		if (vt == ScopeVisitType.PreVisit) { 
			String procedureName = mycallee.getName();

			CallSiteScope scopeCall = (CallSiteScope)scope;
			
			// if there are no exclusive costs to attribute from this context, we are done here
			if (!scopeCall.hasNonzeroMetrics()) {
				return; 
			}

			// Find (or add) callee in top-level hashtable
			ProcedureScope callee = this.createProcedureIfNecessary(scope);

			//-----------------------------------------------------------------------
			// compute callPath: a chain of my callers
			//
			// we build a chain of callers  by tracing the path from a procedure
			// up to the root of the calling context tree. notice that 
			// this isn't simply a reversal of the path. in the chain of callers,
			// we associate the call site with the caller. in the calling context
			// tree, the call site is paired with the callee. that's why we
			// work with a pair of CallSiteScopes at a time (innerCS and enclosingCS)
			//-----------------------------------------------------------------------
			trace("determine call path for " + procedureName);
			CallSiteScope innerCS = scope;
			LinkedList<CallSiteScopeCallerView> callPathList = new LinkedList<CallSiteScopeCallerView>();
			Scope next = scope.getParentScope();
			while ( (next != null) && !(next instanceof RootScope)) 
			{
				// Laksono 2009.01.14: we only deal with call site OR procedure scope
				if ( ((next instanceof CallSiteScope) || (next instanceof ProcedureScope))
						/*!(next instanceof LoopScope) */ && innerCS != null) {
					CallSiteScope enclosingCS = null;
					ProcedureScope mycaller = null;
					if (next instanceof ProcedureScope) {
						mycaller = (ProcedureScope) next; 
						ProcedureScope scopeProc = (ProcedureScope) next;
						// Laks 2008.11.11: bug fix for adding alien proc into call chain
						// ----
						// for alien procedure (such as inlined proc) we need to add it into the call chain.
						// however, since there is way to convert from Procedure scope into CallSite scope,
						// 	we are forced to create a new dummy instance of CallSiteScope based on this Procedure 
						if(scopeProc.isAlien()) {
							// FIXME two dummies instance creation. we hope this doesn't make significant 
							//			performance degradation !
							LineScope scopeLine = new LineScope(scopeProc.getExperiment(), scopeProc.getSourceFile(), 
									scopeProc.getFirstLineNumber(), scopeProc.hashCode());
							enclosingCS = new CallSiteScope(scopeLine, scopeProc, CallSiteScopeType.CALL_FROM_PROCEDURE, 0);
						}
					}
					else if (next instanceof CallSiteScope) {
						enclosingCS = (CallSiteScope) next;
						mycaller = (ProcedureScope) enclosingCS.getProcedureScope(); 
					}
					LineScope lineScope = innerCS.getLineScope();
					if(lineScope != null) {
						CallSiteScopeCallerView callerScope =
							new CallSiteScopeCallerView((LineScope) lineScope.duplicate(), 
									mycaller,
									CallSiteScopeType.CALL_FROM_PROCEDURE, lineScope.hashCode(), next);

						this.combine(callerScope, scopeCall);
						callPathList.addLast(callerScope);
						
						innerCS = enclosingCS;
					}
				}
				next = next.getParentScope();
			}

			//-------------------------------------------------------
			// ensure my call path is represented among my children.
			//-------------------------------------------------------
			mergeCallerPath(callee, callPathList);
			
		} else if (vt == ScopeVisitType.PostVisit)  {
			
			ProcedureScope callee = (ProcedureScope) calleeht.get(objCode);
			// it is nearly impossible that the callee is null but I prefer to do this in case we encounter
			//		a bug or a very strange call path
			if(callee != null) {
				this.decrementCounter(callee);
			}
		}
	}

	public void visit(Scope scope, ScopeVisitType vt) { }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	
	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		ProcedureScope mycallee  = scope;
		if (vt == ScopeVisitType.PreVisit) {
			if (!mycallee.isAlien()) {
				String procedureName = mycallee.getName();
				trace("handling scope " + procedureName);

				// if there are no exclusive costs to attribute from this context, we are done here
				if (!mycallee.hasNonzeroMetrics()) 
					return; 

				// Find (or add) callee in top-level hashtable
				this.createProcedureIfNecessary(mycallee);
				
			} else {
				
			}
		} else if (vt == ScopeVisitType.PostVisit){
			ProcedureScope callee = (ProcedureScope) calleeht.get(new Integer(mycallee.hashCode()));
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
	
	protected void mergeCallerPath(Scope callee, 
			LinkedList<CallSiteScopeCallerView> callerPathList) 
	{
		if (callerPathList.size() == 0) return; // merging an empty path is trivial

		CallSiteScopeCallerView first = callerPathList.removeFirst();

		// -------------------------------------------------------------------------
		// attempt to merge first node on caller path with existing caller of callee  
		//--------------------------------------------------------------------------
		int nCallers = callee.getSubscopeCount();
		for (int i = 0; i < nCallers; i++) {
			CallSiteScope existingCaller = (CallSiteScope) callee.getSubscope(i);

			//------------------------------------------------------------------------
			// we check if the flat ID (or static ID) of the scope in caller view is
			// the same with the flat ID in cct. If this is the case, we can merge it.
			// ATTENTION: this will give incorrect representation for mutual recursives,
			// 	since the same callsites can be called within the same tree (since 
			//	they have the same flat ID).
			//	A correct way is to use isMyCCT() method, BUT this will create a huge
			//	branches and consume enormous memory (it's so enormous that even the
			//	JVM gives up).
			//------------------------------------------------------------------------
			if (first.isMyCCT( existingCaller)) {

				//------------------------------------------------------------------------
				// add metric values for first to those of existingCaller.
				//------------------------------------------------------------------------
				this.combine(first, existingCaller);
				
				// merge rest of call path as a child of existingCaller.
				mergeCallerPath(existingCaller, callerPathList);

				return; // merged with existing child. nothing left to do.
			}
		}

		//----------------------------------------------
		// no merge possible. add new path into tree.
		//----------------------------------------------
		callee.addSubscope(first);
		first.setParentScope(callee);
		for (Scope prev = first; callerPathList.size() > 0; ) {
			Scope next = (Scope) callerPathList.removeFirst();
			prev.addSubscope(next);
			next.setParentScope(prev);
			prev = next;
		}
	}
	
	
	/****--------------------------------------------------------------------------------****
	 * Find caller view's procedure of a given scope. 
	 * If it doesn't exist, create a new one, attach to the tree, and copy the metrics
	 * @param cct_s
	 * @return
	 ****--------------------------------------------------------------------------------****/
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
			trace("added top level entry in bottom up tree");
		} else {
			//System.err.println("Error: procedure "+cct_s+" has been instantiated more than once.");
		}
		
		// accumulate the metrocs
		this.combine(caller_proc, cct_s);
		return caller_proc;
	}
	
	
	/****--------------------------------------------------------------------------------****
	 * Integrating cost of caller view and cct
	 * @param caller_s
	 * @param cct_s
	 ****--------------------------------------------------------------------------------****/
	private void combine(Scope caller_s, Scope cct_s) {
		if (caller_s.iCounter == 0) {
			caller_s.combine(cct_s, inclusiveOnly);
			//caller_s.accumulateMetrics(cct_s, inclusiveOnly, numberOfPrimaryMetrics);
		}
		caller_s.combine(cct_s, exclusiveOnly);
		//caller_s.accumulateMetrics(cct_s, exclusiveOnly, numberOfPrimaryMetrics);
		caller_s.iCounter++;
	}
	
	
	/****--------------------------------------------------------------------------------****
	 * decrement the counter of a caller scope
	 * @param caller_s
	 ****--------------------------------------------------------------------------------****/
	private void decrementCounter(Scope caller_s) {
		if (caller_s == null)
			return;
		
		if (caller_s.iCounter>0) {
			caller_s.iCounter--;
		} else {
			System.err.println("CVSV Err dec "+caller_s.getName()+" \t"+caller_s.iCounter);
		}
	}
	
	
	//----------------------------------------------------
	// debugging support 
	//----------------------------------------------------

	void trace(String msg) {
		if (isdebug) System.out.println(msg);
	}
}
