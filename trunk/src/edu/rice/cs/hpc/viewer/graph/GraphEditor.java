package edu.rice.cs.hpc.viewer.graph;

import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.LineStyle;
import org.swtchart.ISeries.SeriesType;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public abstract class GraphEditor extends GraphEditorBase {


	
	//========================================================================
	// Protected method
	//========================================================================
	
	
	/**
	 * Plot a given metrics for a specific scope
	 * @param exp
	 * @param scope
	 * @param metric
	 */
	protected void plotData(Experiment exp, Scope scope, MetricRaw metric ) {
		
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(exp);

		double y_values[] = this.getValuesY(objDataManager, scope, metric);
		double []x_values;

		x_values = this.getValuesX(objDataManager, scope, metric);

		Chart chart = this.getChart();

		// -----------------------------------------------------------------
		// create scatter series
		// -----------------------------------------------------------------
		ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, metric.getDisplayName() );
		scatterSeries.setLineStyle(LineStyle.NONE);
		
		// -----------------------------------------------------------------
		// set the values x and y to the plot
		// -----------------------------------------------------------------
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		ThreadLevelDataFile.ApplicationType type = objDataManager.getApplicationType();
		
		String axis_x = this.getXAxisTitle(type);
		chart.getAxisSet().getXAxis(0).getTitle().setText( axis_x );
		chart.getAxisSet().getYAxis(0).getTitle().setText( "Metrics" );
		
		// -----------------------------------------------------------------
		// adjust the axis range
		// -----------------------------------------------------------------
		chart.getAxisSet().adjustRange();

		updateRange(x_values.length);
	}

	/***
	 * retrieve the title of the X axis
	 * @param type
	 * @return
	 */
	protected abstract String getXAxisTitle(ThreadLevelDataFile.ApplicationType type);

	/*****
	 * retrieve the value of Xs
	 * @param objManager
	 * @param scope
	 * @param metric
	 * @return
	 */
	protected abstract double[] getValuesX(ThreadLevelDataManager objManager, Scope scope, MetricRaw metric);
	
	/*****
	 * retrieve the value of Y
	 * @param objManager
	 * @param scope
	 * @param metric
	 * @return
	 */
	protected abstract double[] getValuesY(ThreadLevelDataManager objManager, Scope scope, MetricRaw metric);


}
