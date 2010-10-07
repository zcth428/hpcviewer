package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public class CombineMetricUsingCopy extends AbstractCombineMetric {


	public void combine(Scope target, Scope source,
			MetricValuePropagationFilter inclusiveOnly,
			MetricValuePropagationFilter exclusiveOnly) {

		Scope copy = source.duplicate();
		copy.setMetricValues( source.getCombinedValues() );
		
		if (inclusiveOnly != null) {
			target.safeCombine(copy, inclusiveOnly);
		}
		if (exclusiveOnly != null)
			target.combine(copy, exclusiveOnly);

		//super.combine_internal(target, copy, inclusiveOnly, exclusiveOnly);
		
	}
}
