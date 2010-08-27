package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.DerivedIncrementalVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;

public interface IMergedScope {
	public Object[] getAllChildren(DerivedIncrementalVisitor finalizeVisitor,
			PercentScopeVisitor percentVisitor, 
			MetricValuePropagationFilter inclusiveOnly, 
			MetricValuePropagationFilter exclusiveOnly );
	
}
