package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//only propagagate exclusive metrics
public class ExclusiveOnlyMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected BaseMetric[] metrics;

	public ExclusiveOnlyMetricPropagationFilter(BaseMetric[] metricArray) {
		this.metrics = metricArray;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		return ( metrics[src_idx].getMetricType() == MetricType.EXCLUSIVE );
	}
}