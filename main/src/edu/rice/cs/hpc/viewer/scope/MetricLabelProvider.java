package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class MetricLabelProvider extends ColumnLabelProvider {
	private Metric metric;
	private Font fontColumn; //$NON-NLS-1$

	public MetricLabelProvider(Metric metricNew, Font font) {
		super();
		// TODO Auto-generated constructor stub
		this.metric = metricNew;
		this.fontColumn = font;
	}
	
	public String getText(Object element) {
		String text = "-";
		if ((metric != null) && (element instanceof Scope.Node)) {
			MetricValue mv;
			Scope.Node node = (Scope.Node) element;
			// laks
			mv = node.getScope().getMetricValue(metric);
			if(mv.getPercentValue() == 0.0) text = "";
			else{
					text = metric.getDisplayFormat().format(mv);
				//if (text.compareTo("-1.00e00       ") == 0) text = "0.0%";
			}
		}
		return text;
	}

	public Font getFont(Object element) {
		return this.fontColumn;
	}

}
