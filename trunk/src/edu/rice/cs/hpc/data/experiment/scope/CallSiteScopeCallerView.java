package edu.rice.cs.hpc.data.experiment.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.TreeNode;

import sun.tools.tree.ThisExpression;

import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.CallersViewScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FinalizeMetricVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;

public class CallSiteScopeCallerView extends CallSiteScope implements IMergedScope {

	public int numChildren;

	private Scope scopeCCT; 
	private ArrayList<CallSiteScopeCallerView> listOfmerged;

	/**
	 * 
	 * @param scope
	 * @param scope2
	 * @param csst
	 * @param id
	 * @param cct
	 */
	public CallSiteScopeCallerView(LineScope scope, ProcedureScope scope2,
			CallSiteScopeType csst, int id, Scope cct) {
		super(scope, scope2, csst, id, cct.getFlatIndex());

		this.scopeCCT = cct;
		numChildren = 0;
	}


	public Scope getScopeCCT() {
		return this.scopeCCT;
	}
	
	public boolean isMyCCT( Scope cct ) {
		return (this.scopeCCT == cct);
	}
	
	public void merge(CallSiteScopeCallerView scope) {
		if (listOfmerged == null) 
			listOfmerged = new ArrayList<CallSiteScopeCallerView>();
		
		listOfmerged.add(scope);
	}
	


	/*****************
	 * retrieve the child scopes of this node. 
	 * If a node has merged siblings, then we need to reconstruct the children of the merged scopes
	 * @param finalizeVisitor: visitor traversal for finalization phase
	 * @param percentVisitor: visitor traversal to compute the percentage
	 * @param inclusiveOnly: filter for inclusive metrics
	 * @param exclusiveOnly: filter for exclusive metrics 
	 */
	public Object[] getAllChildren(FinalizeMetricVisitor finalizeVisitor, PercentScopeVisitor percentVisitor, 
			MetricValuePropagationFilter inclusiveOnly, 
			MetricValuePropagationFilter exclusiveOnly ) {

		boolean percent_need_recompute = false;
		
		TreeNode children[] = this.getChildren();

		if (children != null && children.length>0) {
			
		} else {
			
			//-------------------------------------------------------------------------
			// construct my own child
			//-------------------------------------------------------------------------

			LinkedList<CallSiteScopeCallerView> listOfChain = CallersViewScopeVisitor.createCallChain
				((CallSiteScope) this.scopeCCT, this, true, null, inclusiveOnly, exclusiveOnly);

			CallSiteScopeCallerView first = listOfChain.removeFirst();
			CallersViewScopeVisitor.addNewPathIntoTree(this, first, listOfChain);
			percent_need_recompute = true;
		}
		
		//----------------------------------------------------------------
		// get the list of children from the merged siblings
		//----------------------------------------------------------------

		if (this.listOfmerged != null) {
			for(Iterator<CallSiteScopeCallerView> iter = this.listOfmerged.iterator(); iter.hasNext(); ) {
				
				CallSiteScopeCallerView scope = iter.next();
				
				CallSiteScope scope_cct = (CallSiteScope) scope.scopeCCT;
				LinkedList<CallSiteScopeCallerView> listOfChain = CallersViewScopeVisitor.createCallChain
					(scope_cct, scope, true, null, inclusiveOnly, exclusiveOnly);
				
				CallersViewScopeVisitor.mergeCallerPath(this, listOfChain, true, scopeCCT, null, inclusiveOnly, null);
				percent_need_recompute = true;
				
			}
		}

		//-------------------------------------------------------------------------
		// set the percent
		//-------------------------------------------------------------------------
		if (percent_need_recompute) {
			this.dfsVisitScopeTree(finalizeVisitor);
			this.dfsVisitScopeTree(percentVisitor);
		}
		
		return this.getChildren();
	}
}
