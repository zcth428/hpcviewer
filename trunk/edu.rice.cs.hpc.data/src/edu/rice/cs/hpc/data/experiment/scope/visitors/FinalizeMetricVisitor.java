package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


/***
 * Visitor to finalize metrics
 * Only aggregate metrics will be finalized
 * @author laksonoadhianto
 *
 */
public class FinalizeMetricVisitor extends AbstractFinalizeMetricVisitor {


    public FinalizeMetricVisitor(BaseMetric[] _metrics) {
		super(_metrics);
	}


	protected void setValue(Scope scope) {
		super.setValue_internal(scope);
	}
}
