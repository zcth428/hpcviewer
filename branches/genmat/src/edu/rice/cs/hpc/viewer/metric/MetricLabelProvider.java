package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.Utilities;

/**
 * Label provide class to write text (and image) in the column item
 * Designed specifically for metric column (font, content, ...)
 * @author laksono
 *
 */
public class MetricLabelProvider extends ColumnLabelProvider {
	private BaseMetric metric;	// metric of this column

	public MetricLabelProvider(BaseMetric metricNew /*, Font font*/ ) {
		super();

		this.metric = metricNew;
	}
	
	public String getText(Object element) {
		String text = null; // we don't need this
		if ((metric != null) && (element instanceof Scope.Node)) {
			Scope.Node node = (Scope.Node) element;
			Scope scope = node.getScope();
			text = metric.getMetricTextValue(scope.getMetricValue(metric));
		}
		return text;
	}

	public Font getFont(Object element) {
		return Utilities.fontMetric;
	}

}
