package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * Label provide class to write text (and image) in the column item
 * Designed specifically for metric column (font, content, ...)
 * @author laksono
 *
 */
public class MetricLabelProvider extends ColumnLabelProvider {
	private Metric metric;	// metric of this column
	private Font fontColumn; //$NON-NLS-1$

	public MetricLabelProvider(Metric metricNew, Font font) {
		super();
		// TODO Auto-generated constructor stub
		this.metric = metricNew;
		this.fontColumn = font;
	}
	
	public String getText(Object element) {
		String text = "-"; // we don't need this
		if ((metric != null) && (element instanceof Scope.Node)) {
			if(metric instanceof Metric) {
				Scope.Node node = (Scope.Node) element;
				text = node.getScope().getMetricTextValue(metric);
			}
		}
		return text;
	}

	public Font getFont(Object element) {
		return this.fontColumn;
	}

}
