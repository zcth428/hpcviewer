package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpc.viewer.util.Utilities;


/****
 * 
 * Class to provide basic label provider for metric columns
 *
 */
public class BaseMetricLabelProvider extends ColumnLabelProvider implements IMetricLabelProvider {
	protected Scope scope = null;
	protected BaseMetric metric = null;
	private FilterStateProvider filterState;


	public BaseMetricLabelProvider(BaseMetric metricNew) {
		this.metric = metricNew;
		
		final ISourceProviderService service   = (ISourceProviderService)Util.getActiveWindow().
				getService(ISourceProviderService.class);
		filterState  = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#isEnabled()
	 */
	public boolean isEnabled() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#setScope(java.lang.Object)
	 */
	public void setScope(Object scope) {
		if (scope instanceof Scope) {
			this.scope = (Scope)scope;
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#setMetric(java.lang.Object)
	 */
	public void setMetric(Object metric) {
		if (metric instanceof BaseMetric) {
			this.metric = (BaseMetric)metric;
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		return Utilities.fontMetric;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String text = null; 

		if ((metric != null) && (element instanceof Scope)) {
			Scope node = (Scope) element;
			if (filterState.isEnabled())
			{
				// the filter is enabled. we need to ensure if this node has
				// filtered children or not by checking if the filtered metrics
				// are empty or not
				if (node.getFilteredMetricsSize() > 0) 
				{
					// some (or all) of the children have been filtered. 
					// we need to use filtered metric value to display the adjusted value
					MetricValue mv = node.getFilteredMetric(metric.getIndex());
					text = metric.getMetricTextValue(mv);
					return text;
				}
			}
			text = metric.getMetricTextValue(node);
		}
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(final Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(final Object element) {
		return null;
	}
}
