package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
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
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphEditor extends EditorPart {

    public static final String ID = "edu.rice.cs.hpc.viewer.scope.GraphEditor";
    
    private ChartComposite chartFrame;

	public GraphEditor() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		this.setSite(site);
		this.setInput(input);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		chartFrame = new ChartComposite(parent, SWT.NONE, null, true, true, false, true, true);
		GraphEditorInput editor_input = (GraphEditorInput) this.getEditorInput();
		setPartName(editor_input.getName());
		
		Experiment exp = editor_input.getExperiment();
		Scope scope = editor_input.getScope();
		MetricRaw metric = editor_input.getMetric();
		
		switch (editor_input.getType()) {
		case PLOT:
			this.plotData(exp, scope, metric, this.getPartName());
			break;
		case SORTED:
			this.plotSortedData(exp, scope, metric, this.getPartName());
			break;
		case HISTO:
			this.plotHistogram(exp, scope, metric, this.getPartName());
			break;
		}

	}

	@Override
	public void setFocus() {

		chartFrame.setFocus();
	}

	
	//========================================================================
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotData(Experiment exp, Scope scope, MetricRaw metric, String sTitle ) {
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getRawID());
		
		this.setPartName(sTitle);

		XYSeriesCollection table = new XYSeriesCollection();

			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(metric.getID(), data.node_index, data.metric_index);
				String[] x_values = data.objDataManager.getProcessIDs( metric.getID() );				
				table.addSeries(this.setData( metric.getTitle(), x_values, y_values));
				
			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		
		JFreeChart chart = ChartFactory.createScatterPlot(sTitle, "Process.Threads", "Metrics", table,
				PlotOrientation.VERTICAL, false, false, false);

		this.finalizeGraph(chart);
	}

	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotSortedData(Experiment exp, Scope scope, MetricRaw metric, String sTitle) {
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getRawID());

		this.setPartName(sTitle);
		XYSeriesCollection table = new XYSeriesCollection();

			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(metric.getID(), data.node_index, data.metric_index);
				
				java.util.Arrays.sort(y_values);
				
				String[] x_values = data.objDataManager.getProcessIDs(metric.getID());	
				double x_vals[] = new double[x_values.length];
				for(int j=0; j<x_values.length; j++) {
					x_vals[j] = Double.valueOf(x_values[j]);
				}
				
				table.addSeries(this.setData(metric.getTitle(), x_vals, y_values));
				
			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		
		JFreeChart chart = ChartFactory.createScatterPlot(sTitle, "Rank sequence", "Metrics", table,
				PlotOrientation.VERTICAL, false, false, false);

		this.finalizeGraph(chart);
	}

	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotHistogram(Experiment exp, Scope scope, MetricRaw metric, String sTitle) {
		
		PlotData data = new PlotData(exp, scope.getCCTIndex(), metric.getRawID());
		HistogramDataset table = null;
		
		this.setPartName(sTitle);
		
			double y_values[];
			try {
				y_values = data.objDataManager.getMetrics(metric.getID(), data.node_index, data.metric_index);
				
				table = this.setHistoData(sTitle, y_values);

			} catch (IOException e) {
				MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
				System.err.println(e.getMessage());
				e.printStackTrace();
				return;
			}			
		
		JFreeChart chart = ChartFactory.createHistogram(sTitle, "Metrics", "Frequency", table,
				PlotOrientation.VERTICAL, false, true, false);
		this.finalizeGraph(chart);
	}


	
	
	/**
	 * get the usual standard metric index defined in experiment.xml
	 * @param normal_metric_index
	 * @return
	 */
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
	private XYSeries setData(String Series, String []x_values, double y_values[]) {
		XYSeries dataset = new XYSeries(Series, true, false);
		int num_data = x_values.length;
		
		if (num_data>y_values.length)
			num_data = y_values.length;

		for (int i=0; i<num_data; i++) {
			dataset.add(Double.valueOf(x_values[i]).doubleValue(), y_values[i]);
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

	
	/*************************************************
	 * class to manage data for plotting graph
	 * @author laksonoadhianto
	 *
	 *************************************************/
	private class PlotData {
		int metric_index;
		long node_index;
		ThreadLevelDataManager objDataManager;
		
		/**
		 * constructor to initialize and normalize data for plotting graph
		 * @param exp: experiment
		 * @param node: node index (1-based)
		 * @param metric: metric index (not normalized)
		 */
		public PlotData( Experiment exp, long node, int metric) 
			throws RuntimeException {
			
			objDataManager = exp.getThreadLevelDataManager();
			
			if (!objDataManager.isDataAvailable()) {
				throw new RuntimeException("Experiment has no thread-level data");
			}
			
			// adjust the node index: 1=the root, 2=node-0, 3=node-1, .... 
			node_index = node - 1;
			// adjust the metric index: start from the first metric
			metric_index = metric;
			
		}
	}

}
