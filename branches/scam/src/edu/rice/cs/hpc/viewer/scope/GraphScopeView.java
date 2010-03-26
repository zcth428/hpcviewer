package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.experimental.chart.swt.ChartComposite;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


/*****************************************************************************************
 * view to display a graph
 * @author laksonoadhianto
 *
 *****************************************************************************************/
public class GraphScopeView extends ViewPart {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.GraphScopeView";
    private Composite clientArea;
    private ChartComposite chartFrame;

	@Override
	public void createPartControl(Composite parent) {
		clientArea = parent;
		chartFrame = new ChartComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
	}

	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 * @param num_metrics
	 */
	public void plotData(Experiment exp, Scope scope, BaseMetric metric, int num_metrics) {
		String sTitle = scope.getName() + ": "  + metric.getDisplayName();
		ThreadLevelDataManager objDataManager = exp.getThreadLevelDataManager();
		double y_values[] = objDataManager.getMetrics(scope.getCCTIndex(), metric.getIndex(), num_metrics);
		double x_values[] = objDataManager.getProcessIDs();
		this.setData(sTitle, x_values, y_values);
	}
	
	
	/**
	 * primitive plotting of a set of Xs and Ys
	 * @param title
	 * @param x_values
	 * @param y_values
	 */
	private void setData(String title, double x_values[], double y_values[]) {
		XYSeries dataset = new XYSeries(title, true, false);
		int num_data = x_values.length;
		if (num_data>y_values.length)
			num_data = y_values.length;

		for (int i=0; i<num_data; i++) {
			dataset.add(x_values[i], y_values[i]);
		}
		
		DefaultTableXYDataset table = new DefaultTableXYDataset();
		table.addSeries(dataset);
		
		JFreeChart chart = ChartFactory.createHistogram(title, "Process.Threads", "Metrics", table, 
				org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
		chartFrame.setChart(chart);
	}
	

}
