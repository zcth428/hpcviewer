package edu.rice.cs.hpc.viewer.scope;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
    
    private ChartComposite chartFrame;
    
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
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getIndex());
		
		String sTitle = getGraphTitle(scope, metric, data.metric_index);
		this.setPartName(sTitle);

		XYSeriesCollection table = new XYSeriesCollection();

		for (int i=0; i<data.series.length; i++) {
			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(data.series[i], data.node_index, data.metric_index);
				ArrayList<String> x_values = data.objDataManager.getProcessIDs( data.series[i] );				
				table.addSeries(this.setData( data.series[i], x_values, y_values));
				
			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		}
		
		JFreeChart chart = ChartFactory.createScatterPlot(sTitle, "Process.Threads", "Metrics", table,
				PlotOrientation.VERTICAL, data.series.length>1, false, false);

		this.finalizeGraph(chart);
	}

	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 * @param num_metrics
	 */
	public void plotSortedData(Experiment exp, Scope scope, BaseMetric metric, int num_metrics) {
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getIndex());

		String sTitle = "[Sorted] " + getGraphTitle(scope, metric, data.metric_index);
		this.setPartName(sTitle);
		XYSeriesCollection table = new XYSeriesCollection();

		for (int i=0; i<data.series.length; i++) {
			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(data.series[i], data.node_index, data.metric_index);
				
				java.util.Arrays.sort(y_values);
				
				ArrayList<String> x_values = data.objDataManager.getProcessIDs(data.series[i]);	
				double x_vals[] = new double[x_values.size()];
				for(int j=0; j<x_values.size(); j++) {
					x_vals[j] = Double.valueOf(x_values.get(j));
				}
				
				table.addSeries(this.setData(data.series[i], x_vals, y_values));
				
			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		}
		
		JFreeChart chart = ChartFactory.createScatterPlot(sTitle, "Process.Threads", "Metrics", table,
				PlotOrientation.VERTICAL, data.series.length>1, false, false);

		this.finalizeGraph(chart);
	}

	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 * @param num_metrics
	 */
	public void plotHistogram(Experiment exp, Scope scope, BaseMetric metric, int num_metrics) {
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getIndex());
		HistogramDataset table = null;
		
		String sTitle = "[Histogram] " + getGraphTitle(scope, metric, data.metric_index);
		this.setPartName(sTitle);
		
		for (int i=0; i<data.series.length; i++) {
			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(data.series[i], data.node_index, data.metric_index);
				
				table = this.setHistoData(sTitle, y_values);

			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		}
		
		JFreeChart chart = ChartFactory.createHistogram(sTitle, "Metrics", "Frequency", table,
				PlotOrientation.VERTICAL, true, true, false);
		this.finalizeGraph(chart);
	}

	
	/***
	 * grab the title of the graph
	 * @param scope 
	 * @param metric
	 * @param metric_index (normalized 0-based index)
	 * @return
	 */
	static public String getGraphTitle(Scope scope, BaseMetric metric, int metric_index) {
		String sTitle = metric.getDisplayName();
		int pos = sTitle.indexOf(':');

		if (pos>0) {
			sTitle = sTitle.substring(0, pos);
		}
		String sMetricStatus = (metric_index % 2 == 0? " (I)" : " (E)");
		return scope.getShortName() + ": " + sTitle + sMetricStatus;
		
	}

	
	/**
	 * get the index metric for data level thread
	 * @param metric_index
	 * @return
	 */
	public static int getNormalizedMetricIndex(int metric_index) {
		return metric_index >> 3;
	}
	
	
	public static int getStandardMetricIndex(int normal_metric_index) {
		return normal_metric_index << 3;
	}
	
	
	private void finalizeGraph(JFreeChart chart) {

		Plot plot = chart.getPlot();
		plot.setBackgroundPaint(java.awt.Color.WHITE);
		plot.setOutlinePaint(java.awt.Color.GRAY);
		plot.setForegroundAlpha(0.85F);
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

	/**
	 * primitive plotting of a set of Xs and Ys
	 * @param title
	 * @param x_values
	 * @param y_values
	 */
	private HistogramDataset setHistoData(String Series, double y_values[]) {
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(Series, y_values, 10);

		return dataset;
	}

	/**
	 * primitive plotting of a set of Xs and Ys
	 * @param title
	 * @param x_values
	 * @param y_values
	 */
	private XYSeries setData(String Series, double x_values[], double y_values[]) {
		XYSeries dataset = new XYSeries(Series, true, false);
		int num_data = x_values.length;
		
		if (num_data>y_values.length)
			num_data = y_values.length;

		for (int i=0; i<num_data; i++) {
			try {
				dataset.add(x_values[i], y_values[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dataset;
	}


	private class PlotData {
		int metric_index;
		long node_index;
		String series[];
		ThreadLevelDataManager objDataManager;
		
		public PlotData( Experiment exp, long node, int metric) {
				objDataManager = exp.getThreadLevelDataManager();
			
			// adjust the node index: 1=the root, 2=node-0, 3=node-1, .... 
			node_index = node - 1;
			// adjust the metric index: start from the first metric
			metric_index = GraphScopeView.getNormalizedMetricIndex(metric - exp.getMetric(0).getIndex());
			
			if (!objDataManager.isDataAvailable()) {
				return;
			}
			
			series = objDataManager.getSeriesName();

		}
	}
}
