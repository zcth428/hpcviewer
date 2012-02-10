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

public interface IExperiment {

	public void setMetrics(List<BaseMetric> metricList);
	
	public void setRootScope(Scope rootScope);
	public Scope getRootScope();
	
	public RootScope getCallerTreeRoot();
	public DerivedMetric addDerivedMetric(RootScope scopeRoot, Expression expFormula, String sName, 
			AnnotationType annotationType, MetricType metricType);
	
	public TreeNode[] getRootScopeChildren();
}
