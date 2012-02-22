package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

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
