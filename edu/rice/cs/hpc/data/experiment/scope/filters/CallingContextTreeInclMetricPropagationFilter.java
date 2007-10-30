package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class CallingContextTreeInclMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected Metric[] metrics;

	public CallingContextTreeInclMetricPropagationFilter(Metric[] metricArray) {
		this.metrics = metricArray;
	}
	
//	 don't propagate exclusive metrics across upward from callsites in the CCT. this has
//	 the effect of accumulating exclusive costs for each procedure frame on its callsite in
//	 the exclusive view in the CCT.
	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		if ( metrics[src_idx].getMetricType() == MetricType.INCLUSIVE) return false;
		if (!(source instanceof CallSiteScope)) return true;
		if (((CallSiteScope) source).getProcedureScope().isAlien()) return true;
		return false;
	}

}
