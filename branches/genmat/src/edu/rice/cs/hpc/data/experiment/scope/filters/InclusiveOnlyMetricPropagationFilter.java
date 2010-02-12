package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//only propagagate inclusive metrics
public class InclusiveOnlyMetricPropagationFilter implements MetricValuePropagationFilter {
	/** The parsed metric objects. */
	protected BaseMetric[] metrics;

	public InclusiveOnlyMetricPropagationFilter(BaseMetric[] metricArray) {
		this.metrics = metricArray;
	}

	public boolean doPropagation(Scope source, Scope target, int src_idx, int targ_idx) {
		//-------------------------------------------------------------------------
		// This part looks suspicious: the target (flat) will never be the same as the 
		// 		source (cct). So far there is no proof that there is a path that return false
		//-------------------------------------------------------------------------
		if ((source instanceof LineScope)) {
		   Scope parent = source.getParentScope();
		   if ((parent != null) && (parent instanceof CallSiteScope)) {
                     if ((target != null) && (target == parent.getParentScope()))
                    	 return false;
		   }
		}
		return ( metrics[src_idx].getMetricType() == MetricType.INCLUSIVE );
	}
}