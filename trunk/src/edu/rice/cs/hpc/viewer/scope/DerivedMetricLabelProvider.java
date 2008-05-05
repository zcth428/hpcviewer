/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author la5
 * Label provider for derived metric. This class is intended to computed
 * the derived metric "on-the-fly", therefore, it may not be able to
 * compute the percentage corerectly.
 */
public class DerivedMetricLabelProvider extends MetricLabelProvider {

	DerivedMetric metricDerived;
	
	/**
	 * @param metricNew: the derived metric
	 * @param font: font needed to display the metric
	 */
	public DerivedMetricLabelProvider(DerivedMetric metricNew, Font font) {
		super(metricNew, font);
		this.metricDerived = metricNew;
		// TODO Auto-generated constructor stub
	}
	
	public String getText(Object element) {
		if(element != null && element instanceof Scope.Node) {
			Scope.Node node = (Scope.Node) element;
			Scope scope = node.getScope();
			return DerivedMetric.getTextValue(scope, this.metricDerived);
		}
		return null;
	}
	
}
