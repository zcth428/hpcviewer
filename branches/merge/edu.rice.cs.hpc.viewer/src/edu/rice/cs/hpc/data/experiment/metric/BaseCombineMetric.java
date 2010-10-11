package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;


public class BaseCombineMetric extends AbstractCombineMetric {

	public void combine(Scope target, Scope source,
			MetricValuePropagationFilter inclusiveOnly,
			MetricValuePropagationFilter exclusiveOnly) {


		super.combine_internal(target, source, inclusiveOnly, exclusiveOnly);
	}



}
