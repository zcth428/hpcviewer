package edu.rice.cs.hpc.data.experiment.scope.filters;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/***********************
 * Filter metric to filter inclusive metrics and pre-aggregated metrics 
 * @author laksonoadhianto
 *
 */
public class AggregatePropagationFilter extends InclusiveOnlyMetricPropagationFilter {

	public AggregatePropagationFilter(BaseMetric[] metricArray) {
		super(metricArray);
	}

	/****************
	 *  propagate only if the parent's diPropagation filter it and the metric is a type of aggregate
	 */
	public boolean doPropagation(Scope source, Scope target, int src_idx,
			int targ_idx) {
		boolean bParentResult = super.doPropagation(source, target, src_idx, targ_idx);
		if (!bParentResult)
			bParentResult = bParentResult || ( metrics[src_idx].getMetricType() == MetricType.PREAGGREGATE );
		return bParentResult;
	}

}
