package edu.rice.cs.hpc.data.experiment.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.TreeNode;

import edu.rice.cs.hpc.data.experiment.metric.CombineMetricUsingCopy;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.AbstractFinalizeMetricVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.CallersViewScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FinalizeMetricVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;


/**************************
 * special class for caller view's call site scope
 * 
 * @author laksonoadhianto
 *
 */
public class CallSiteScopeCallerView extends CallSiteScope implements IMergedScope {

	public int numChildren;

	private Scope scopeCCT; 
	private ArrayList<CallSiteScopeCallerView> listOfmerged;

	static final private CombineMetricUsingCopy combine_with_dupl = new CombineMetricUsingCopy();
	
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

	/***
	 * retrieve the CCT scope of this scope
	 * @return
	 */
	public Scope getScopeCCT() {
		return this.scopeCCT;
	}
	
	/****
	 * @deprecated
	 * @param cct
	 * @return
	 */
	public boolean isMyCCT( Scope cct ) {
		return (this.scopeCCT == cct);
	}
	
	/***
	 * add merged scope into the list
	 * @param scope
	 */
	public void merge(CallSiteScopeCallerView scope) {
		if (listOfmerged == null) 
			listOfmerged = new ArrayList<CallSiteScopeCallerView>();
		
		listOfmerged.add(scope);	// include the new scope to merge
	}
	


	/*****************
	 * retrieve the child scopes of this node. 
	 * If a node has merged siblings, then we need to reconstruct the children of the merged scopes
	 * @param finalizeVisitor: visitor traversal for finalization phase
	 * @param percentVisitor: visitor traversal to compute the percentage
	 * @param inclusiveOnly: filter for inclusive metrics
	 * @param exclusiveOnly: filter for exclusive metrics 
	 */
	public Object[] getAllChildren(AbstractFinalizeMetricVisitor finalizeVisitor, PercentScopeVisitor percentVisitor, 
			MetricValuePropagationFilter inclusiveOnly, 
			MetricValuePropagationFilter exclusiveOnly ) {

		boolean percent_need_recompute = false;
		
		TreeNode children[] = this.getChildren();

		if (children != null && children.length>0) {
			
			//-------------------------------------------------------------------------
			// this scope has already computed children, we do nothing, just return them
			//-------------------------------------------------------------------------
			
		} else {
			
			//-------------------------------------------------------------------------
			// construct my own child
			//-------------------------------------------------------------------------
			Scope scope_cost = this.scopeCCT;
			if (this.listOfmerged == null){
				scope_cost = this;
			}
			LinkedList<CallSiteScopeCallerView> listOfChain = CallerScopeBuilder.createCallChain
				((CallSiteScope) this.scopeCCT, scope_cost, combine_with_dupl, inclusiveOnly, exclusiveOnly);

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
					(scope_cct, scope, combine_with_dupl, inclusiveOnly, exclusiveOnly);
				
				CallersViewScopeVisitor.mergeCallerPath(this, listOfChain, combine_with_dupl, inclusiveOnly, exclusiveOnly);
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
	
	/*****
	 * retrieve the list of merged scopes
	 * @return
	 */
	public Object[] getMergedScopes() {
		if (this.listOfmerged != null)
			return this.listOfmerged.toArray();
		else 
			return null;
	}


}
