
/**
 * This file provides an example of how to build a MetricLabelProvider extension.  This is being provided just so you 
 * can see that the extension works and how the user would implement an extension.  The only thing the user needs to do 
 * is provide a class like this one and add an extension into their plugin.xml file that points at this class.
 * 
 * This should NOT be committed to the Rice viewer SVN, it was just provided so you could see the extension work.
 * 
 */


package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.viewer.scope.ScopeView;

public class MetricLabelFormatProvider extends BaseMetricLabelProvider
		implements IMetricLabelProvider {

	public MetricLabelFormatProvider() {
		super();
	}

	public MetricLabelFormatProvider(BaseMetric metricNew) {
		super(metricNew);
	}

	/**
	 * The following method allows the extension to control if it should be used for the view currently being refreshed.
	 * If it returns false then it will not be used to format the view's contents.  Without this control if multiple extensions 
	 * are configured they would all be trying to set colors and text values for all views.
	 */
	@Override
	public boolean isEnabled() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();		// obtain the active page
		IWorkbenchPart wPart = page.getActivePart();
		if (!(wPart instanceof ScopeView)) {
			return false;
		}
		return true;
	}


	/**
	 * This method returns the color to be used as a background color in the Rice metrics views.  This just forces the
	 * background color for all cells in columns containing max values to red and cells in columns with minimum values to green.
	 * 
	 * If somewhere we had a list of which items should receive color this method could insure that those items were 
	 * displayed using a background color.  And if the list identified the color to use then it could also control which 
	 * color should be used for each item.
	 * 
	 * One possible use of this might be to add a color attribute to the column selection dialog which allows the user to select 
	 * a color that should be used to display that column.  The selected color could be saved in the metric class and this extension 
	 * could just return the color from the metric class.
	 * 
	 * The BaseMetricLabelProvider which this extends has variables which contain the instance of the current scope, metric and 
	 * metric value that are associated with the current cell that this background color will apply to.  So we can make the 
	 * color decision based on the information in any of these classes.  For this example I have just used the metric class.
	 */
	@Override
	public Color getBackground(Object element) {
		Color c = null;				// set to not use color if it is not for the event we care about 
		if (metric.getDisplayName().contains("Max")) {
			// set to use color for this cell in the tree viewer table
			c = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
		}
		if (metric.getDisplayName().contains("Min")) {
			// set to use color for this cell in the tree viewer table
			c = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GREEN);
		}
		return c;
	}
}
