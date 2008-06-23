/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.ExtDerivedMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author la5
 *
 */
public class ExtDerivedMetricLabelProvider extends MetricLabelProvider {
	private ExtDerivedMetric metricDerivation;
	
	/**
	 * @param metricNew
	 * @param font
	 */
	public ExtDerivedMetricLabelProvider(ExtDerivedMetric metricNew, Font font) {
		super(metricNew, font);
		this.metricDerivation = metricNew;
	}

	/**
	 * Callback to retrieve the texgt of the element
	 */
	public String getText(Object element) {
		if(element != null && element instanceof Scope.Node) {
			Scope.Node node = (Scope.Node) element;
			Scope scope = node.getScope();
			return this.metricDerivation.getTextValue(scope);
		}
		return null;
	}
}
