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

public class InclusiveMetricsScopeVisitor implements ScopeVisitor {
	private int numberOfPrimaryMetrics;
	MetricValuePropagationFilter filter;

	public InclusiveMetricsScopeVisitor(int nMetrics, MetricValuePropagationFilter filter) {
		this.numberOfPrimaryMetrics = nMetrics;
		this.filter = filter;
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
					} else if (filter instanceof FlatViewInclMetricPropagationFilter){
						parent.accumulateMetrics(((CallSiteScope)scope).getLineScope(), 
								new ExclusiveOnlyMetricPropagationFilter(scope.getExperiment().getMetrics()), numberOfPrimaryMetrics);
						parent.accumulateMetrics(scope, 
								new InclusiveOnlyMetricPropagationFilter(scope.getExperiment().getMetrics()), numberOfPrimaryMetrics);
					} else {
						parent.accumulateMetrics(scope, filter, numberOfPrimaryMetrics);						
					}
				} else {
					parent.accumulateMetrics(scope, filter, numberOfPrimaryMetrics);
				}
			}
		}
	}
}
