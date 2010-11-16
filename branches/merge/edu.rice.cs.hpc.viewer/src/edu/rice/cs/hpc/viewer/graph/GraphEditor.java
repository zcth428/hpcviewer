package edu.rice.cs.hpc.viewer.graph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.ISeries.SeriesType;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.ThreadScopeView;

public abstract class GraphEditor extends GraphEditorBase {


	
	//========================================================================
	// Private method
	//========================================================================
	
	
	
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
	protected void plotData(Experiment exp, Scope scope, MetricRaw metric ) {
		
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(exp);

		double y_values[] = this.getValuesY(objDataManager, scope, metric);
		double []x_values = this.getValuesX(objDataManager, scope, metric);

		Chart chart = this.getChart();

		// create scatter series
		ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, metric.getDisplayName() );
		scatterSeries.setLineStyle(LineStyle.NONE);
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		// adjust the axis range
		chart.getAxisSet().adjustRange();

		updateRange(x_values.length);

		final Menu menuPopup = chart.getPlotArea().getMenu();
		if (menuPopup != null) {
			MenuItem item = new MenuItem(menuPopup, SWT.PUSH);
			item.setText("Show metric from all processes");
		}
	}

	
	protected abstract double[] getValuesX(ThreadLevelDataManager objManager, Scope scope, MetricRaw metric);
	
	protected abstract double[] getValuesY(ThreadLevelDataManager objManager, Scope scope, MetricRaw metric);

	@Override
	protected String getXAxisTitle() {
		return "Process.Thread";
	}


	@Override
	protected String getYAxisTitle() {
		return "Metric";
	}
	

}
