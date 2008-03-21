package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//prevent inclusive metrics from being propagated to the FileScope level in the flat view
public class FlatViewInclMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected Metric[] metrics;

	public FlatViewInclMetricPropagationFilter(Metric[] metricArray) {
		this.metrics = metricArray;
	}
	
	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		if (target instanceof FileScope) {
			return ( metrics[src_idx].getMetricType() == MetricType.EXCLUSIVE);
		}
		return true;
	}
}