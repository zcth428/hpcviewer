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


public class GraphScopeView extends ViewPart {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.GraphScopeView";
    private Composite clientArea;
    private ChartComposite chartFrame;

	@Override
	public void createPartControl(Composite parent) {
		clientArea = parent;
		chartFrame = new ChartComposite(parent, SWT.NONE);
		//this.setData("test", x_values, y_values);
	}

	@Override
	public void setFocus() {
	}

	
	public void setData(Experiment exp, Scope scope, BaseMetric metric, int num_metrics) {
		String sTitle = scope.getName() + ": "  + metric.getDisplayName();
		ThreadLevelDataManager objDataManager = exp.getThreadLevelDataManager();
		double y_values[] = objDataManager.getMetrics(scope.getCCTIndex(), metric.getIndex(), num_metrics);
		double x_values[] = objDataManager.getProcessIDs();
		this.setData(sTitle, x_values, y_values);
	}
	
	
	private void setData(String title, double x_values[], double y_values[]) {
		XYSeries dataset = new XYSeries(title, true, false);
		int num_data = x_values.length;
		if (num_data>y_values.length)
			num_data = y_values.length;

		double x[] = {308, 514, 1667, 3335, 4045, 4685, 6083, 6817, 7865};
		double y[] = {2.091000123E10, 1.0081000593E10, 2.0587001211E10, 3.0498001794E10, 3.553000209E9, 2.4344001432E10, 2.4191001423E10, 2.1896001288E10,6.919000407E9};
		
		for (int i=0; i<num_data; i++) {
			dataset.add(x[i], y[i]);
			//dataset.add(x_values[i], y_values[i]);
		}
		
		DefaultTableXYDataset table = new DefaultTableXYDataset();
		table.addSeries(dataset);
		
		JFreeChart chart = ChartFactory.createHistogram(title, "Process.Threads", "Metrics", table, 
				org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
		chartFrame.setChart(chart);
		
		//ChartComposite frame = new ChartComposite(clientArea, SWT.NONE, chart, true);
	}
	

}
