package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * Label provide class to write text (and image) in the column item
 * Designed specifically for metric column (font, content, ...)
 * @author laksono
 *
 */
public class MetricLabelProvider extends ColumnLabelProvider {
	private BaseMetric metric;	// metric of this column
	private Font fontColumn; //$NON-NLS-1$

	public MetricLabelProvider(BaseMetric metricNew, Font font) {
		super();
		// TODO Auto-generated constructor stub
		this.metric = metricNew;
		this.fontColumn = font;
	}
	
	public String getText(Object element) {
		String text = "-"; // we don't need this
		if ((metric != null) && (element instanceof Scope.Node)) {
			Scope.Node node = (Scope.Node) element;
//			text = metric.getMetricTextValue(node.getScope());
			Scope scope = node.getScope();
			text = metric.getMetricTextValue(scope.getMetricValue(metric));
			/*
			 * if(metric instanceof Metric) {
				Scope.Node node = (Scope.Node) element;
				text = node.getScope().getMetricTextValue(metric);
			}*/
		}
		return text;
	}

	public Font getFont(Object element) {
		return this.fontColumn;
	}

}
