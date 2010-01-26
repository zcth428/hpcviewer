package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.io.PrintStream;

import edu.rice.cs.hpc.data.experiment.Experiment;
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

public class PrintFlatViewScopeVisitor implements IScopeVisitor {

	private Experiment objExperiment;
	private PrintStream objOutputStream;
	
	public PrintFlatViewScopeVisitor(Experiment experiment, PrintStream stream) {
		this.objExperiment = experiment;
		this.objOutputStream = stream;
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(RootScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(FileScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(AlienScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(LineScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { print(scope, vt); }
	public void visit(GroupScope scope, ScopeVisitType vt) { print(scope, vt); }

	private void print(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			this.objOutputStream.print(scope.getName());
			int nbMetrics = objExperiment.getMetricCount();
			for (int i=0; i<nbMetrics; i++) {
				MetricValue metric = scope.getMetricValue(i);
				this.objOutputStream.print(", ");
				if (metric.isAvailable())
					this.objOutputStream.print(metric.getValue());
			}
			this.objOutputStream.println();
		} else {
			
		}
	}
}
