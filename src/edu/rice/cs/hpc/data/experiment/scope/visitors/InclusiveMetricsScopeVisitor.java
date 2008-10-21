package edu.rice.cs.hpc.data.experiment.scope.visitors;

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
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.FlatViewInclMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

import edu.rice.cs.hpc.data.experiment.metric.*;

public class InclusiveMetricsScopeVisitor implements ScopeVisitor {
	private int numberOfPrimaryMetrics;
	MetricValuePropagationFilter filter;
	private ExclusiveOnlyMetricPropagationFilter filterExclusive;
	private InclusiveOnlyMetricPropagationFilter filterInclusive;

	public InclusiveMetricsScopeVisitor(BaseMetric []metrics, MetricValuePropagationFilter filter) {
		this.numberOfPrimaryMetrics = metrics.length;
		this.filter = filter;
		filterExclusive = new ExclusiveOnlyMetricPropagationFilter(metrics);
		filterInclusive = new InclusiveOnlyMetricPropagationFilter(metrics);
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

	protected void up(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			Scope parent = scope.getParentScope();
			if (parent != null) {
				if (scope instanceof CallSiteScope) {
					if (filter instanceof ExclusiveOnlyMetricPropagationFilter) {
						parent.accumulateMetrics(((CallSiteScope)scope).getLineScope(), filter, numberOfPrimaryMetrics);
					} 
					// in case of FLAT VIEW, we need to specially deal with recursive functions:
					//	avoid recomputation of the cost
					else if (filter instanceof FlatViewInclMetricPropagationFilter){
						// Exclusive metrics: Add the cost of the line scope into the parent
						parent.accumulateMetrics(((CallSiteScope)scope).getLineScope(), 
								this.filterExclusive, numberOfPrimaryMetrics);
						// Inclusive metrics: if the parent is not procedure, add the scope into the parent
						// the cost of the procedure scope should have to be precomputed in the FlatViewScopeVisitor class
						if(!(parent instanceof ProcedureScope))
							parent.accumulateMetrics(scope, 
								this.filterInclusive, numberOfPrimaryMetrics);
					} else {
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
						}
						return;
					}
					parent.accumulateMetrics(scope, filter, numberOfPrimaryMetrics);
				}
			}
		}
	}
}
