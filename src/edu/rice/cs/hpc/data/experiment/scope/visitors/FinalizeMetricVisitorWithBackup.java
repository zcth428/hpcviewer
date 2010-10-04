package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
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

public class FinalizeMetricVisitorWithBackup extends FinalizeMetricVisitor {

	public FinalizeMetricVisitorWithBackup(BaseMetric[] listOfMetrics) {
		super(listOfMetrics);
	}

    public void visit(LineScope scope, ScopeVisitType vt) { process(scope, vt); }
    public void visit(StatementRangeScope scope, ScopeVisitType vt)  {process(scope, vt); }
    public void visit(LoopScope scope, ScopeVisitType vt)  { process(scope, vt);}
    public void visit(CallSiteScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(ProcedureScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(FileScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(GroupScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(LoadModuleScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(RootScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(Scope scope, ScopeVisitType vt)  { process(scope, vt); }
    

    
    private void process( Scope scope, ScopeVisitType vt) {
    	if (vt == ScopeVisitType.PreVisit ) {
    		this.setValue(scope);
    	}
    }

    
    protected void setValue ( Scope scope) {
    	if (scope instanceof CallSiteScope || 
    			(scope instanceof ProcedureScope && !((ProcedureScope)scope).isAlien()) )
    		scope.backupMetricValues();
    	
    	super.setValue(scope);
    }

}
