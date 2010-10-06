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

public class FinalizeMetricVisitorWithBackup extends AbstractFinalizeMetricVisitor {

	public FinalizeMetricVisitorWithBackup(BaseMetric[] listOfMetrics) {
		super(listOfMetrics);
	}


    
    protected void setValue ( Scope scope) {
    	if (scope instanceof CallSiteScope || 
    			(scope instanceof ProcedureScope && !((ProcedureScope)scope).isAlien()) )
    		scope.backupMetricValues();
    	
    	super.setValue_internal(scope);
    }

}
