package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
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

public class PercentScopeVisitor implements ScopeVisitor {
	MetricValue[] metricCosts;
	int nMetrics;

	public PercentScopeVisitor(int metricCount, RootScope r) {
		nMetrics = metricCount;
		metricCosts = new MetricValue[nMetrics];
		for (int i = 0; i < nMetrics; i++) {
			metricCosts[i] = r.getMetricValue(i);
		}
	}
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { calc(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { calc(scope, vt); }

	//----------------------------------------------------
	// propagate a child's metric values to its parent
	//----------------------------------------------------

	protected void calc(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			for (int i = 0; i < nMetrics; i++) {
				MetricValue m = scope.getMetricValue(i);
				if (m != MetricValue.NONE && metricCosts[i] != MetricValue.NONE) {
					double myValue = m.getValue();
					double total = metricCosts[i].getValue();
					if (total != 0.0) m.setPercentValue(myValue/total);
				}

			}
		}
	}
}
