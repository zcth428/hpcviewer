package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

/**
 * Visitor class to accumulate metrics
 * This class is used to compute inclusive and exclusive cost for :
 *  - CCT when filter are InclusiveOnlyMetricPropagationFilter and ExclusiveOnlyMetricPropagationFilter
 *  - FT when filter is FlatViewInclMetricPropagationFilter
 *  
 * @author laksonoadhianto
 *
 */
public class InclusiveMetricsScopeVisitor extends AbstractInclusiveMetricsVisitor {
	private int numberOfPrimaryMetrics;
	MetricValuePropagationFilter filter;
	//private ExclusiveOnlyMetricPropagationFilter filterExclusive;
	//private InclusiveOnlyMetricPropagationFilter filterInclusive;

	public InclusiveMetricsScopeVisitor(Experiment experiment, MetricValuePropagationFilter filter) {
		super(experiment, filter);
		this.numberOfPrimaryMetrics = experiment.getMetricCount();
		this.filter = filter;
/*
		filterExclusive = new ExclusiveOnlyMetricPropagationFilter(metrics);
		filterInclusive = new InclusiveOnlyMetricPropagationFilter(metrics);
		*/
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { up(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { up(scope, vt); }

	//----------------------------------------------------
	// propagate a child's metric values to its parent
	//----------------------------------------------------
/*
	protected void up(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if (parent != null) {
				if (scope instanceof CallSiteScope) {
					// in case of FLAT VIEW, we need to specially deal with recursive functions:
					//	avoid recomputation of the cost
					if (filter instanceof FlatViewInclMetricPropagationFilter){
						// Exclusive metrics: Add the cost of the line scope into the parent
						parent.accumulateMetrics(((CallSiteScope)scope).getLineScope(), 
								this.filterExclusive, numberOfPrimaryMetrics);
						// Inclusive metrics: if the parent is not procedure, add the scope into the parent
						// the cost of the procedure scope should have to be precomputed in the FlatViewScopeVisitor class
						if(!(parent instanceof ProcedureScope))
							parent.accumulateMetrics(scope, 
								this.filterInclusive, numberOfPrimaryMetrics);
					} else {
						// inclusive view: add everything
						parent.accumulateMetrics(scope, filter, numberOfPrimaryMetrics);						
					}
				} else {
					// New definition of exclusive cost:
					//	The cost of Outer loop does not include the cost of inner loop 
					if(parent instanceof LoopScope && scope instanceof LoopScope) {
						// for nested loop: we need to accumulate the inclusive but not exclusive.
						if(filter instanceof InclusiveOnlyMetricPropagationFilter) {
							// During the creation of CCT, we call this class twice: one for exclusive, the other for incl
							// so we need to make sure that only the inclusive is taken
							parent.accumulateMetrics(scope, this.filter, this.numberOfPrimaryMetrics);
						} else if (filter instanceof FlatViewInclMetricPropagationFilter) {
							// This path is from flat tree construction, we just take into account inclusive loops
							parent.accumulateMetrics(scope, this.filterInclusive, this.numberOfPrimaryMetrics);
							this.accumulateAncestor(scope, scope);
						}
						return;
					} 
					parent.accumulateMetrics(scope, filter, numberOfPrimaryMetrics);
				}
			}
		}
	}
	*/
	/**
	 * Accumulate the exclusive cost of a scope into its procedure scope.
	 * Specifically designed for Flat View.
	 * @param scope
	 * @param parent
	 */
	/*
	private void accumulateAncestor(Scope scope, Scope parent) {
		Scope ancestor = parent.getParentScope();
		while((ancestor != null ) && !(ancestor instanceof RootScope) &&
				!(ancestor instanceof ProcedureScope) && !(ancestor instanceof CallSiteScope)) {
			ancestor = ancestor.getParentScope();
		}
		if(ancestor != null && (ancestor instanceof ProcedureScope) ) {
			ProcedureScope scopeAncestorProc = (ProcedureScope) ancestor;
			if (!scopeAncestorProc.isAlien())
				ancestor.accumulateMetrics(scope, this.filterExclusive, this.numberOfPrimaryMetrics);
			//else
			//	System.err.println("IMSV alien detected for "+scope.getName()+"\t: "+scopeAncestorProc.getName());
		}
	}
	*/
	/**
	 * Method to accumulate the metric value from the child to the parent
	 * @param parent
	 * @param source
	 */
	protected void accumulateToParent(Scope parent, Scope source) {
		parent.accumulateMetrics(source, this.filter, this.numberOfPrimaryMetrics);
	}
	
	/**
	 * Method to accumulate the metric value from the child to the parent based on a given filter
	 * @param parent
	 * @param source
	 * @param filter
	 */
	protected void accumulateToParent(Scope parent, Scope source, MetricValuePropagationFilter myfilter) {
		parent.accumulateMetrics(source, myfilter, this.numberOfPrimaryMetrics);
	}


}
