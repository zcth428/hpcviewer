package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider;
import edu.rice.cs.hpc.viewer.util.Utilities;

/**
 * This class adds support for the MetricLabelProvider extension point in the viewer.
 * @author mohrg
 *
 */
public class MetricLabelProvider extends BaseMetricLabelProvider {
	public enum MethodFlag { TEXT, FONT, FOREGROUND, BACKGROUND };

	// This is the ID of our extension point
	private static final String METRIC_LABEL_PROVIDER_ID = "edu.rice.cs.hpc.viewer.metric.metricLabelProvider";

	public MetricLabelProvider(BaseMetric metricNew) {
		super(metricNew);
	}

	public Font getFont(Object element) {
		return Utilities.fontMetric;
	}

	public String getText(Object element) {
		boolean calledSomeone = false;
		String result = null;
		if (!(element instanceof Scope)) {
			return null;
		}
		Scope scope = (Scope)element;
		
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(METRIC_LABEL_PROVIDER_ID);
		MySafeRunnable runnable = null;
		try {
			// if there is more than one extension, call all the ones that declare themselves as enabled 
			// the last one called gets to control the value used (no idea how order is controlled).
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o  != null) {
					if (((IMetricLabelProvider)o).isEnabled() == false) {
						continue;
					}
					calledSomeone = true;
					runnable = new MySafeRunnable(element, o, MethodFlag.TEXT);
					((IMetricLabelProvider)o).setScope(element);
					((IMetricLabelProvider)o).setMetric(metric);
					MetricValue metricValue = scope.getMetricValue(metric);
					((IMetricLabelProvider)o).setMetricValue(metricValue);
					SafeRunner.run(runnable);
					result = runnable.getText();
				}
			}
			if (calledSomeone) {
				return result;
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}

		return super.getText(element);
	}

	/**
	 * This method will check to see if anyone has extended this label provider.  If it finds an extension it will create an instance of the 
	 * extending class and call its 0 argument constructor.  Then it calls setters in that class to give it the scope, metric, and metric value for 
	 * the tree viewer cell for which we are providing a label.  Next it will call the getBackground method in the extending class to allow it to 
	 * provide a background color that will be used in this cell.  If an extension is not found this method just returns null to prevent the use 
	 * of color in this tree viewer cell.
	 * @param element (actually the program scope)
	 * @return
	 */
	public Color getBackground(final Object element) {
		boolean calledSomeone = false;
		Color result = null;
		if (!(element instanceof Scope)) {
			return null;
		}
		Scope scope = (Scope)element;
		
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(METRIC_LABEL_PROVIDER_ID);
		MySafeRunnable runnable = null;
		try {
			// if there is more than one extension, call all the ones that declare themselves as enabled 
			// the last one called gets to control the background color used (no idea how order is controlled).
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o  != null) {
					if (((IMetricLabelProvider)o).isEnabled() == false) {
						continue;
					}
					calledSomeone = true;
					runnable = new MySafeRunnable(element, o, MethodFlag.BACKGROUND);
					((IMetricLabelProvider)o).setScope(element);
					((IMetricLabelProvider)o).setMetric(metric);
					MetricValue metricValue = scope.getMetricValue(metric);
					((IMetricLabelProvider)o).setMetricValue(metricValue);
					SafeRunner.run(runnable);
					result = runnable.getColor();
				}
			}
			if (calledSomeone) {
				return result;
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}

		return super.getBackground(element);
	}

	/**
	 * This method will check to see if anyone has extended this label provider.  If it finds an extension it will create an instance of the 
	 * extending class and call its 0 argument constructor.  Then it calls setters in that class to give it the scope, metric, and metric value for 
	 * the tree viewer cell for which we are providing a label.  Next it will call the getForeground method in the extending class to allow it to 
	 * provide a foreground color that will be used in this cell.  If an extension is not found this method just returns null to prevent the use 
	 * of color in this tree viewer cell.
	 * @param element (actually the program scope)
	 * @return
	 */
	public Color getForeground(final Object element) {
		boolean calledSomeone = false;
		Color result = null;
		if (!(element instanceof Scope)) {
			return null;
		}
		Scope scope = (Scope)element;
		
 		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(METRIC_LABEL_PROVIDER_ID);
		MySafeRunnable runnable = null;
		try {
			// if there is more than one extension, call all the ones that declare themselves as enabled 
			// the last one called gets to control the foreground color used (no idea how order is controlled).
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o  != null) {
					if (((IMetricLabelProvider)o).isEnabled() == false) {
						continue;
					}
					calledSomeone = true;
					runnable = new MySafeRunnable(element, o, MethodFlag.FOREGROUND);
					((IMetricLabelProvider)o).setScope(element);
					((IMetricLabelProvider)o).setMetric(metric);
					MetricValue metricValue = scope.getMetricValue(metric);
					((IMetricLabelProvider)o).setMetricValue(metricValue);
					SafeRunner.run(runnable);
					result = runnable.getColor();
				}
			}
			if (calledSomeone) {
				return result;
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}

		return super.getForeground(element);
	}

	class MySafeRunnable implements ISafeRunnable {
		private Object element;
		private Object o;
		MethodFlag mf;
		private String text;
		private Font font;
		private Color color;
		
		MySafeRunnable(final Object element, Object o, MethodFlag methodFlag){
			this.element = element;
			this.o = o;
			this.mf = methodFlag;
		}
		public void handleException(Throwable exception) {
			System.out.println("Exception in label provider extension.");
		}
		public void run() throws Exception {
			if (mf == MethodFlag.TEXT)
			{
				text = ((IMetricLabelProvider) o).getText(element);
				return;
			}
			if (mf == MethodFlag.FONT)
			{
				font = ((IMetricLabelProvider) o).getFont(element);
				return;
			}
			if (mf == MethodFlag.FOREGROUND)
			{
				color = ((IMetricLabelProvider) o).getForeground(element);
				return;
			}
			if (mf == MethodFlag.BACKGROUND)
			{
				color = ((IMetricLabelProvider) o).getBackground(element);
			}
			return;
		}
		protected String getText () {
			return text;
		}
		protected Font getFont () {
			return font;
		}
		protected Color getColor () {
			return color;
		}
	}
}
