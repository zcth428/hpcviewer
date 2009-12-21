package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class RemoveCallsiteCostPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected BaseMetric[] metrics;

	public RemoveCallsiteCostPropagationFilter(BaseMetric[] arrMetrics) {
		this.metrics = arrMetrics;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx,
			int targ_idx) {
		return this.metrics[targ_idx] instanceof Metric; 
		//(this.metrics[targ_idx].getMetricType() != MetricType.PREAGGREGATE);
	}

}
