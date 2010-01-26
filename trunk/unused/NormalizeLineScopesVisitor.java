package edu.rice.cs.hpc.data.experiment.scope.visitors;

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

public class NormalizeLineScopesVisitor implements IScopeVisitor {
	private MetricValuePropagationFilter filter;
	private int numberOfPrimaryMetrics;

	public NormalizeLineScopesVisitor(int nMetrics, MetricValuePropagationFilter filter) {
		this.numberOfPrimaryMetrics = nMetrics;
		this.filter = filter;
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(LineScope scope, 				ScopeVisitType vt) {  }
	public void visit(LoopScope scope, 				ScopeVisitType vt) {  }
	public void visit(ProcedureScope scope, 		ScopeVisitType vt) {  }
	public void visit(LoadModuleScope scope, 		ScopeVisitType vt) {  }
	public void visit(StatementRangeScope scope, 	ScopeVisitType vt) {  }
	public void visit(FileScope scope, 				ScopeVisitType vt) {  }
	public void visit(GroupScope scope, 			ScopeVisitType vt) {  }
	public void visit(RootScope scope, 				ScopeVisitType vt) {  }
	public void visit(Scope scope, 					ScopeVisitType vt) {  }
	public void visit(CallSiteScope scope, 			ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			//----------------------------------------------------
			// propagate any metric values associated with the 
			// line of the call site to the CallSiteScope itself
			//----------------------------------------------------
			scope.accumulateMetrics(scope.getLineScope(), filter, numberOfPrimaryMetrics);
		}
	}
}
