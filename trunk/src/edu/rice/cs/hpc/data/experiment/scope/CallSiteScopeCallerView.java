package edu.rice.cs.hpc.data.experiment.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.TreeNode;

import edu.rice.cs.hpc.data.experiment.metric.AbstractCombineMetric;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.AbstractFinalizeMetricVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.CallersViewScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;


/**************************
 * special class for caller view's call site scope
 * 
 * @author laksonoadhianto
 *
 */
public class CallSiteScopeCallerView extends CallSiteScope implements IMergedScope {

	private boolean flag_scope_has_child;

	private Scope scopeCCT; // store the orignal CCT scope
	private Scope scopeCost; // the original CCT cost scope. In caller view, a caller scope needs 2 pointers: the cct and the scope
	
	private ArrayList<CallSiteScopeCallerView> listOfmerged;

	static final private IncrementalCombineMetricUsingCopy combine_with_dupl = new IncrementalCombineMetricUsingCopy();
	static final private CombineMetricUsingCopyNoCondition combine_without_cond = new CombineMetricUsingCopyNoCondition();
	
	/**
	 * 
	 * @param scope
	 * @param scope2
	 * @param csst
	 * @param id
	 * @param cct
	 */
	public CallSiteScopeCallerView(LineScope scope, ProcedureScope scope2,
			CallSiteScopeType csst, int id, Scope cct, Scope s_cost) {
		super(scope, scope2, csst, id, cct.getFlatIndex());

		this.scopeCCT = cct;
		this.scopeCost = s_cost;
		this.flag_scope_has_child = false;
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
		
		//-----------------------------------------
		// the first phase of caller tree creation has counter equal to zero (at least)
		// the second phase (incremental) should have the counter to be more than 1
		//-----------------------------------------
		if (this.iCounter>1) {
			scope.iCounter = this.iCounter;
		}

/*		System.out.println("  MERGE: " + this + " (" + this.getScopeCCT().getCCTIndex()+") " + this.iCounter +
				" <-- (" + scope.getScopeCCT().getCCTIndex() + ") " + scope.iCounter + "\t m: " + scope.getMetricValue(0).getValue());
*/		listOfmerged.add(scope);	// include the new scope to merge
	}
	

	/*****
	 * Mark that this scope has a child. The number of children is still unknown though
	 * 	and has to computed dynamically
	 */
	public void markScopeHasChildren() {
		this.flag_scope_has_child = true;
	}
	
	/***
	 * check if the scope has a child or not
	 * @return
	 */
	public boolean hasScopeChildren() {
		return this.flag_scope_has_child;
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
			return this.getChildren();
			
		} else {
			
			//-------------------------------------------------------------------------
			// construct my own child
			//-------------------------------------------------------------------------
			Scope scope_cost = this.scopeCost; //this.scopeCCT;
/*			if (this.listOfmerged == null){
				scope_cost = this;
			}
*/			LinkedList<CallSiteScopeCallerView> listOfChain = CallerScopeBuilder.createCallChain
				((CallSiteScope) this.scopeCCT, scope_cost, combine_without_cond, inclusiveOnly, exclusiveOnly);

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
					(scope_cct, scope, combine_without_cond, inclusiveOnly, exclusiveOnly);
				
				CallersViewScopeVisitor.mergeCallerPath(this, listOfChain, combine_with_dupl, inclusiveOnly, exclusiveOnly);
				percent_need_recompute = true;
				
			}
		}

		//-------------------------------------------------------------------------
		// set the percent
		//-------------------------------------------------------------------------
		if (percent_need_recompute) {
			// there were some reconstruction of children. Let's finalize the metrics, and recompute the percent
			for(TreeNode child:this.getChildren()) {
				if (child instanceof CallSiteScopeCallerView) {
					CallSiteScopeCallerView csChild = (CallSiteScopeCallerView) child;
					
					csChild.dfsVisitScopeTree(finalizeVisitor);
					csChild.dfsVisitScopeTree(percentVisitor);
				}
			}
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


		
	
	/************************
	 * combination class to combine two metrics
	 * This class is specifically designed for combining merged nodes in incremental caller view
	 * @author laksonoadhianto
	 *
	 *************************/
	static private class IncrementalCombineMetricUsingCopy extends AbstractCombineMetric {

		/*
		 * (non-Javadoc)
		 * @see edu.rice.cs.hpc.data.experiment.metric.AbstractCombineMetric#combine(edu.rice.cs.hpc.data.experiment.scope.Scope, edu.rice.cs.hpc.data.experiment.scope.Scope, edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter, edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter)
		 */
		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {

			
			if (target instanceof CallSiteScopeCallerView) {
				CallSiteScopeCallerView target_scope = (CallSiteScopeCallerView) target;

				Scope copy = source.duplicate();
				copy.setMetricValues( source.getCombinedValues() );
				
				CallSiteScopeCallerView source_scope = (CallSiteScopeCallerView) source;
				
				//-----------------------------------------------------------
				// only combine the outermost "node" of incremental callsite
				// the counter was initialized by "1" so the outermost must be at least 1
				//-----------------------------------------------------------
				if (inclusiveOnly != null && source_scope.iCounter <= 1) {
					target_scope.safeCombine(copy, inclusiveOnly);
				} 
										
				if (exclusiveOnly != null)
					target_scope.combine(copy, exclusiveOnly);
/*				if (source_scope.iCounter <= 1)
					System.out.println("\tCOMBINE: " + target_scope + " (" + target_scope.getScopeCCT().getCCTIndex()+") " + source_scope.iCounter +
						" <-- " + source_scope.getScopeCCT().getCCTIndex() + "\t m: " + target_scope.getMetricValue(0).getValue());
*/				
			} else {
				System.err.println("ERROR-ICMUC: the target combine is incorrect: " + target + " -> " + target.getClass() );
			}
			
		}
	}


	/************************
	 * combination class specific for the creation of incremental call site
	 * in this phase, we need to store the information of counter from the source
	 * @author laksonoadhianto
	 *
	 ************************/
	static private class CombineMetricUsingCopyNoCondition extends AbstractCombineMetric {

		/*
		 * (non-Javadoc)
		 * @see edu.rice.cs.hpc.data.experiment.metric.AbstractCombineMetric#combine(edu.rice.cs.hpc.data.experiment.scope.Scope, 
		 * edu.rice.cs.hpc.data.experiment.scope.Scope, edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter, 
		 * edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter)
		 */
		public void combine(Scope target, Scope source,
				MetricValuePropagationFilter inclusiveOnly,
				MetricValuePropagationFilter exclusiveOnly) {

			if (target instanceof CallSiteScopeCallerView) {
				CallSiteScopeCallerView target_scope = (CallSiteScopeCallerView) target;
				Scope copy = source.duplicate();
				copy.setMetricValues( source.getCombinedValues() );
				
				if (inclusiveOnly != null) {
					target_scope.safeCombine(copy, inclusiveOnly);
				}
				if (exclusiveOnly != null)
					target_scope.combine(copy, exclusiveOnly);
				
				target_scope.iCounter = source.iCounter;
				
/*				System.out.println("ASSGN: " + target_scope + " (" + target_scope.getScopeCCT().getCCTIndex()+") " + target_scope.iCounter +
						" <-- " + "\t m: " + target_scope.getMetricValue(0).getValue());
*/			} else {
				System.err.println("ERROR-CMUCNC: the target combine is incorrect: " + target + " -> " + target.getClass() );
			}
			
		}
	}

	static private String getObjectID(Object o) {
		return Integer.toHexString(System.identityHashCode(o));
	}
}
