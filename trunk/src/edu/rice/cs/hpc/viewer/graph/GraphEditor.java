package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.Range;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.ThreadScopeView;

public class GraphEditor extends EditorPart {

    public static final String ID = "edu.rice.cs.hpc.viewer.graph.GraphEditor";
    
    private Chart chart;
    //private ChartComposite chartFrame;

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

		IEditorInput input = this.getEditorInput();
		if (input == null || !(input instanceof GraphEditorInput) )
			throw new RuntimeException("Invalid input for graph editor");
		
		GraphEditorInput editor_input = (GraphEditorInput) input;
		String title = editor_input.getName();
		
		this.setPartName( title );

		//----------------------------------------------
		// chart creation
		//----------------------------------------------
		chart = new InteractiveChart(parent, SWT.NONE);
		chart.getTitle().setText( title );

		
		//----------------------------------------------
		// axis title
		//----------------------------------------------
		chart.getAxisSet().getXAxis(0).getTitle().setText("Process.Threads");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Metrics");

		//----------------------------------------------
		// formatting axis
		//----------------------------------------------
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick xTick = axisSet.getXAxis(0).getTick();
		xTick.setFormat(new DecimalFormat("######00.00##"));
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("0.0##E0##"));

		// turn off the legend
		chart.getLegend().setVisible(false);
		
		//----------------------------------------------
		// the chart should occupy all the client area
		//----------------------------------------------
		//GridDataFactory.fillDefaults().grab(true, true).applyTo(chart);
		
		//----------------------------------------------
		// plot data
		//----------------------------------------------
		Experiment exp = editor_input.getExperiment();
		Scope scope = editor_input.getScope();
		MetricRaw metric = editor_input.getMetric();
		
		switch (editor_input.getType()) {
		case PLOT:
			this.plotData(exp, scope, metric);
			break;
		case SORTED:
			this.plotSortedData(exp, scope, metric, this.getPartName());
			break;
		case HISTO:
			this.plotHistogram(exp, scope, metric, this.getPartName());
			break;
		}

		this.createContextMenu(scope, metric);
		//addEvent();
	}

	@Override
	public void setFocus() {
		chart.setFocus();
	}

	
	//========================================================================
	// Private method
	//========================================================================
	
    private void createContextMenu(final Scope scope, final MetricRaw metric) {

		Composite plotArea = chart.getPlotArea();
		Menu menu = new Menu(plotArea);
		MenuItem mnuItem = new MenuItem(menu, SWT.PUSH);
		mnuItem.setText("Show metrics");
		mnuItem.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				showThreadView( scope, metric, 0);
			}
			
		});
		plotArea.setMenu(menu);
	}
	
	
	private void showThreadView( Scope scope, MetricRaw metric, int rank_sequence ) {
		final IViewPart view = this.getSite().getWorkbenchWindow().getActivePage().findView(edu.rice.cs.hpc.viewer.scope.ThreadScopeView.ID);
		if (view != null) {
			final ThreadScopeView thread_view = (ThreadScopeView) view;

			final Experiment exp = scope.getExperiment();
			final RootScope root = (RootScope) exp.getRootScope();

			thread_view.setInput( exp, root, metric, rank_sequence);
		}
	}
	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotData(Experiment exp, Scope scope, MetricRaw metric ) {
		
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(exp);

		double y_values[];
		double []x_values;
		try {
			y_values = objDataManager.getMetrics( metric, scope.getCCTIndex() );
			x_values = objDataManager.getProcessIDsDouble( metric.getID() );				
			
		} catch (IOException e) {
			MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}

		// create scatter series
		ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, metric.getDisplayName() );
		scatterSeries.setLineStyle(LineStyle.NONE);
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		// adjust the axis range
		chart.getAxisSet().adjustRange();

		updateRange(x_values.length);

	}

	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotSortedData(Experiment exp, Scope scope, MetricRaw metric, String sTitle) {
		
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(exp);

		double y_values[], x_values[];
		try {
			y_values = objDataManager.getMetrics( metric, scope.getCCTIndex());
			
			java.util.Arrays.sort(y_values);
			
			x_values = objDataManager.getProcessIDsDouble(metric.getID());	
			
		} catch (IOException e) {
			MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}			
		
		chart.getAxisSet().getXAxis(0).getTitle().setText("Rank sequence");
	
		// create scatter series
		ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, metric.getDisplayName());
		scatterSeries.setLineStyle(LineStyle.NONE);
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		// adjust the axis range
		chart.getAxisSet().adjustRange();

		updateRange(x_values.length);
	}

	
	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	private void plotHistogram(Experiment exp, Scope scope, MetricRaw metric, String sTitle) {
		final int bins = 10;
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(exp);
		
		double y_values[], x_values[];
		try {
			y_values = objDataManager.getMetrics(metric, scope.getCCTIndex());

		} catch (IOException e) {
			MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}			

		Histogram histo = new Histogram(bins, y_values);
		y_values = histo.getAxisY();
		x_values = histo.getAxisX();
		double min = histo.min();
		double max = histo.max();
		double single = 0.1 * (max-min)/bins;
		
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick xTick = axisSet.getXAxis(0).getTick();
		xTick.setFormat(new DecimalFormat("0.###E0##"));
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("#######"));

		IAxis axis = axisSet.getXAxis(0); 
		axis.getTitle().setText("Metrics");
		axis.getRange().lower = min - single;
		axis.getRange().upper = max + single;
		axisSet.getYAxis(0).getTitle().setText("Frequency");
		
		// create scatter series
		IBarSeries scatterSeries = (IBarSeries) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, metric.getDisplayName() );
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		// adjust the axis range
		chart.getAxisSet().adjustRange();
		
		updateRange( histo.getWidth() );
	}


	/***
	 * temporary SWTChart bug fix 
	 * add axis padding for scatter graph
	 */
	private void updateRange(int num_items_x) {
		IAxis axis = chart.getAxisSet().getXAxis(0);
		Range range = axis.getRange();
		double delta = 0.1 * (range.upper - range.lower) / num_items_x;
		Range new_range = new Range(range.lower - delta, range.upper + delta);
		axis.setRange(new_range);
		chart.updateLayout();		
	}
	
	/**
	 * temporary SWTChart bug fix 
	 * add padding for histogram
	 * @param width
	 */
	private void updateRange(double width) {
		IAxis axis = chart.getAxisSet().getXAxis(0);
		Range range = axis.getRange();
		double pad = 0.5 * width;
		Range new_range = new Range(range.lower - pad, range.upper + pad);
		axis.setRange(new_range);
		chart.updateLayout();		
	}
	

}
