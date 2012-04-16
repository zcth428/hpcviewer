package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.Utilities;

public class BaseMetricLabelProvider extends ColumnLabelProvider implements IMetricLabelProvider {
	protected Scope scope = null;
	protected BaseMetric metric = null;
	protected MetricValue metricValue	 = null;

	public BaseMetricLabelProvider() {
	}

	public BaseMetricLabelProvider(BaseMetric metricNew) {
		this.metric = metricNew;
	}

	public boolean isEnabled() {
		return false;
	}
	
	public void setScope(Object scope) {
		if (scope instanceof Scope) {
			this.scope = (Scope)scope;
		}
		return;
	}

	public void setMetric(Object metric) {
		if (metric instanceof BaseMetric) {
			this.metric = (BaseMetric)metric;
		}
		return;
	}

	public void setMetricValue(Object metricValue) {
		if (metric instanceof BaseMetric) {
			this.metricValue = (MetricValue)metricValue;
		}
		return;
	}

	public Font getFont(Object element) {
		return Utilities.fontMetric;
	}

	public String getText(Object element) {
		String text = null; 

		if ((metric != null) && (element instanceof Scope)) {
			Scope node = (Scope) element;
			text = metric.getMetricTextValue(node);
		}
		return text;
	}

	public Color getBackground(final Object element) {
		return null;
	}

	public Color getForeground(final Object element) {
		return null;
	}
}
