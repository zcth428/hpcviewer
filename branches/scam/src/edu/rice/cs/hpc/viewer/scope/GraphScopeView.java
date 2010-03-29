package edu.rice.cs.hpc.viewer.scope;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile;
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
    private ChartComposite chartFrame;

    private boolean debug = true;
    
	@Override
	public void createPartControl(Composite parent) {
		chartFrame = new ChartComposite(parent, SWT.NONE, null, true, true, false, true, true);
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
		
		int node_index = scope.getCCTIndex() - 1;
		int metric_index = metric.getIndex() - exp.getMetric(0).getIndex();
		
		if (!objDataManager.isDataAvailable()) {
			return;
		}
		
		String series[] = objDataManager.getSeriesName();
		XYSeriesCollection table = new XYSeriesCollection();

		for (int i=0; i<series.length; i++) {
			double y_values[];
			try {
				y_values = objDataManager.getMetrics(series[i],node_index, metric_index, num_metrics);
				ArrayList<String> x_values = objDataManager.getProcessIDs(series[i]);				
				table.addSeries(this.setData(series[i], x_values, y_values));
				
			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				e.printStackTrace();
				return;
			}			
		}
		
		JFreeChart chart = ChartFactory.createXYLineChart(sTitle, "Process.Threads", "Metrics", table,
				PlotOrientation.VERTICAL, true, false, false); 

		chart.setBackgroundPaint(java.awt.Color.WHITE);
		chartFrame.setChart(chart);
	}
	
	
	/**
	 * primitive plotting of a set of Xs and Ys
	 * @param title
	 * @param x_values
	 * @param y_values
	 */
	private XYSeries setData(String Series, ArrayList<String> x_values, double y_values[]) {
		XYSeries dataset = new XYSeries(Series, true, false);
		int num_data = x_values.size();
		
		if (num_data>y_values.length)
			num_data = y_values.length;

		for (int i=0; i<num_data; i++) {
			dataset.add(Double.valueOf(x_values.get(i)).doubleValue(), y_values[i]);
		}
		return dataset;
	}

	
	private void print_debug(String s) {
		if (debug)
			System.out.println(s);
	}

	private String print_array(PrintStream out, double o_a[]) {
		if (( o_a == null) || (o_a.length == 0))
			return null;
				
		out.print("[");
		for(int i=0; i<o_a.length; i++) {
			out.print(o_a[i]);
			if (i<o_a.length-1)
				out.print(", ");
		}
		out.println("]");
		return null;
	}
}
