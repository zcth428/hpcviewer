package edu.rice.cs.hpc.viewer.metric;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.BaseMetricColumnProvider;

/**
 * Label provide class to write text (and image) in the column item
 * Designed specifically for metric column (font, content, ...)
 * @author laksono
 *
 */
public class MetricLabelProvider extends BaseMetricColumnProvider {
	private BaseMetric metric;	// metric of this column

	public MetricLabelProvider(BaseMetric metricNew) {
		super();
		this.metric = metricNew;
	}
	
	public String getText(Object element) {
		String text = null; 

		if ((metric != null) && (element instanceof Scope)) {
			Scope node = (Scope) element;
			text = metric.getMetricTextValue(node);
		}
		return text;
	}


}
