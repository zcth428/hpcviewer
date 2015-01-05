package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
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
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;

public class DerivedPercentVisitor implements IScopeVisitor {
	private int iExclusive;
	private int iInclusive;
	private MetricValue objIncAggValue;
	private MetricValue objExcAggValue;

	public DerivedPercentVisitor(BaseMetric []metrics, RootScope scopeRoot, int iMetricInc, int iMetricExc) {
		this.iInclusive = iMetricInc;
		this.iExclusive = iMetricExc;
		objIncAggValue = scopeRoot.getMetricValue(iMetricInc);
		if (this.iExclusive > 0) {
			this.objExcAggValue = scopeRoot.getMetricValue(iMetricExc);
		}
		//System.out.println("DPV: inc:"+ objIncAggValue.getValue());
	}
	
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
			this.setPercent(scope, this.iInclusive, this.objIncAggValue);
			if (this.iExclusive > 0) {
				this.setPercent(scope, this.iExclusive, this.objExcAggValue);
			}
		}
	}

	
	private void setPercent (Scope scope, int iMetricPosition, MetricValue objAggregate) {
		MetricValue m = scope.getMetricValue(iMetricPosition);
		if ( (m != MetricValue.NONE) && objAggregate != MetricValue.NONE ) {
			double value = MetricValue.getValue(m);
			double total = MetricValue.getValue(objAggregate);
			
			if ( total != 0.0 )
				MetricValue.setAnnotationValue(m, value/total);
		}
	}
}
