package edu.rice.cs.hpc.data.experiment;

import java.util.List;

import com.graphbuilder.math.Expression;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;

public abstract class BaseExperiment implements IExperiment {

	protected List<BaseMetric> metrics;
	protected RootScope rootScope;
	
	public void setMetrics(List<BaseMetric> metricList) {
		
		metrics = metricList;
	}

	public void setRootScope(Scope rootScope) {
		this.rootScope = (RootScope) rootScope;
	}

	public Scope getRootScope() {
		return rootScope;
	}

	public RootScope getCallerTreeRoot() {
		
		if (this.rootScope.getSubscopeCount()==3) {
			
			Scope scope = this.rootScope.getSubscope(1);
			if (scope instanceof RootScope)
				return (RootScope) scope;
			
		}
		return null;
	}

	
	public DerivedMetric addDerivedMetric(RootScope scopeRoot,
			Expression expFormula, String sName, AnnotationType annotationType,
			MetricType metricType) {
	
		return null;
	}

	public TreeNode[] getRootScopeChildren() {
		return this.rootScope.getChildren();
	}
}
