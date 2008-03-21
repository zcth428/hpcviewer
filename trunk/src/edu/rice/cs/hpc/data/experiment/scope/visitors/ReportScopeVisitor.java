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

public class ReportScopeVisitor implements ScopeVisitor {
		public double total;

		public ReportScopeVisitor() {
		}
		//----------------------------------------------------
		// visitor pattern instantiations for each Scope type
		//----------------------------------------------------

		public void visit(Scope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(RootScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(LoadModuleScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(FileScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(GroupScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(AlienScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(LoopScope scope, ScopeVisitType vt) { calc(scope, vt, false); }
		public void visit(StatementRangeScope scope, ScopeVisitType vt) { calc(scope, vt, false); }

		public void visit(CallSiteScope scope, ScopeVisitType vt) { calc(scope, vt, true); }
		public void visit(LineScope scope, ScopeVisitType vt) { calc(scope, vt, true); }
		public void visit(ProcedureScope scope, ScopeVisitType vt) { calc(scope, vt, true); }

		//----------------------------------------------------
		// propagate a child's metric values to its parent
		//----------------------------------------------------

		protected void calc(Scope scope, ScopeVisitType vt, boolean b) {
			if (vt == ScopeVisitType.PreVisit && b) {
				MetricValue m = scope.getMetricValue(1);
				if (m != MetricValue.NONE) {
					total += m.getValue();
				}

			}
		}
}
