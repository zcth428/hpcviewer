package edu.rice.cs.hpc.data.experiment.scope;

import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.metric.AbstractCombineMetric;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public class CallerScopeBuilder {
	
	static private final int MAX_DESC = 2;

	
	/******
	 * create caller view
	 * @param scope_cct
	 * @param scope_cost
	 * @param combine
	 * @param inclusiveOnly
	 * @param exclusiveOnly
	 * @return list of call path
	 */
	static public LinkedList<CallSiteScopeCallerView> createCallChain(CallSiteScope scope_cct,
			Scope scope_cost, AbstractCombineMetric combine, 
			MetricValuePropagationFilter inclusiveOnly, MetricValuePropagationFilter exclusiveOnly ) {
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
		CallSiteScope innerCS = scope_cct;
		LinkedList<CallSiteScopeCallerView> callPathList = new LinkedList<CallSiteScopeCallerView>();
		Scope next = scope_cct.getParentScope();
		int numKids = 0;
		CallSiteScopeCallerView prev_scope = null;
		while ( (next != null) && !(next instanceof RootScope) && (numKids<MAX_DESC) )
		{
			// Laksono 2009.01.14: we only deal with call site OR pure procedure scope (no alien)
			if ( isCallSiteCandidate(next, innerCS)) {

				CallSiteScope enclosingCS = null;
				ProcedureScope mycaller = null;
				
				if (next instanceof ProcedureScope) {
					mycaller = (ProcedureScope) next;
					
				}	else if (next instanceof CallSiteScope) {
					enclosingCS = (CallSiteScope) next;
					mycaller = (ProcedureScope) enclosingCS.getProcedureScope(); 
				}
				
				LineScope lineScope = innerCS.getLineScope();
				
				if(lineScope != null) {
					numKids++;
					if (prev_scope != null)
						prev_scope.numChildren = 1;

					//--------------------------------------------------------------
					// creating a new child scope if the path is not too long (< MAX_DESC)
					//--------------------------------------------------------------
					if (numKids<MAX_DESC) {
						CallSiteScopeCallerView callerScope =
							new CallSiteScopeCallerView( lineScope, mycaller,
									CallSiteScopeType.CALL_FROM_PROCEDURE, lineScope.hashCode(), next);

						// set the value of the new scope
						combine.combine(callerScope, scope_cost, inclusiveOnly, exclusiveOnly);
						callPathList.addLast(callerScope);
						
						innerCS = enclosingCS;
						prev_scope = callerScope;
					}
				}
			}
			next = next.getParentScope();
		}

		return callPathList;
	}
	
	
	/****
	 * Merge the same path in the caller path (if exist)
	 * @param callee
	 * @param callerPathList
	 * @param combine
	 * @param inclusiveOnly
	 * @param exclusiveOnly
	 */
	static public void mergeCallerPath(Scope callee, 
			LinkedList<CallSiteScopeCallerView> callerPathList, AbstractCombineMetric combine,
			MetricValuePropagationFilter inclusiveOnly, MetricValuePropagationFilter exclusiveOnly) 
	{
		if (callerPathList.size() == 0) return; // merging an empty path is trivial

		CallSiteScopeCallerView first = callerPathList.removeFirst();

		// -------------------------------------------------------------------------
		// attempt to merge first node on caller path with existing caller of callee  
		//--------------------------------------------------------------------------
		int nCallers = callee.getSubscopeCount();
		for (int i = 0; i < nCallers; i++) {
			CallSiteScopeCallerView existingCaller = (CallSiteScopeCallerView) callee.getSubscope(i);

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
			//if (first.isMyCCT(existingCaller) ) {
			if (first.getCCTIndex() == existingCaller.getCCTIndex()) {

				Scope e_cct = existingCaller.getScopeCCT();
				Scope f_cct = first.getScopeCCT();
				if (e_cct.getCCTIndex() == f_cct.getCCTIndex()) {
					//System.out.println("Scope: " + callee + " [" + callee.cct_node_index+ "] " + "\tCCT identical: " + e_cct+ "\t" + e_cct.cct_node_index);
					return;
				}
				//------------------------------------------------------------------------
				// We found the same CCT in the path. let's merge them
				//------------------------------------------------------------------------
				existingCaller.merge(first);

				//------------------------------------------------------------------------
				// combine metric values for first to those of existingCaller.
				//------------------------------------------------------------------------
				combine.combine(existingCaller, first, inclusiveOnly, exclusiveOnly);

				//------------------------------------------------------------------------
				// merge rest of call path as a child of existingCaller.
				//------------------------------------------------------------------------
				mergeCallerPath(existingCaller, callerPathList, combine, inclusiveOnly, exclusiveOnly);
				
				return; // merged with existing child. nothing left to do.
			}
		}

		//----------------------------------------------
		// no merge possible. add new path into tree.
		//----------------------------------------------
		addNewPathIntoTree(callee, first, callerPathList);
	}

	
	
	/**********
	 * add children 
	 * @param callee: the parent
	 * @param first: the first child
	 * @param callerPathList: list of children (excluding the first child)
	 */
	static public void addNewPathIntoTree(Scope callee, CallSiteScopeCallerView first,
			LinkedList<CallSiteScopeCallerView> callerPathList) {
		
		callee.addSubscope(first);
		first.setParentScope(callee);
		for (Scope prev = first; callerPathList.size() > 0; ) {
			Scope next = (Scope) callerPathList.removeFirst();
			prev.addSubscope(next);
			next.setParentScope(prev);
			prev = next;
		}
	}
	
	
	/******
	 * 
	 * @param scope
	 * @param innerCS
	 * @return
	 */
	static private boolean isCallSiteCandidate(Scope scope, CallSiteScope innerCS) {
		return ( ((scope instanceof CallSiteScope) || 
				(scope instanceof ProcedureScope && !((ProcedureScope)scope).isAlien()) )
				&& (innerCS != null));
	}
}
