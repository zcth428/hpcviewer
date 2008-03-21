package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//only propagagate inclusive metrics
public class InclusiveOnlyMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected Metric[] metrics;

	public InclusiveOnlyMetricPropagationFilter(Metric[] metricArray) {
		this.metrics = metricArray;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		if ((source instanceof LineScope)) {
		   Scope parent = source.getParentScope();
		   if ((parent != null) && (parent instanceof CallSiteScope)) {
                     if ((target != null) && (target == parent.getParentScope())) return false;
		   }
		}
		return ( metrics[src_idx].getMetricType() == MetricType.INCLUSIVE );
	}
}