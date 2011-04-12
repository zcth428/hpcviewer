package edu.rice.cs.hpc.viewer.graph;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.ISeries.SeriesType;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.util.PreferenceConstants;

public abstract class GraphEditor extends GraphEditorBase {

	static private final int DEFAULT_DIAMETER = 3;
	static protected int diameter;
	
	public GraphEditor() {
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		diameter = objPref.getInt(PreferenceConstants.P_GRAPH_DOT_DIAMETER);
		
		if (diameter < 1)
			diameter = DEFAULT_DIAMETER;
	}
	
	
	/*****
	 * change the size of the dot
	 * @param di
	 */
	static public void setSymbolSize(int di) {
		
		if (diameter == di)
			return;
		
		diameter = di;
		GraphEditor.updateChange();
	}
	
	public void refresh() {
		Chart chart = this.getChart();
		if (chart != null) {
			ISeriesSet seriesSet = chart.getSeriesSet();
			if (seriesSet != null) {
				ISeries series[] = seriesSet.getSeries();
				for (ISeries serie : series) {
					((ILineSeries) serie).setSymbolSize(diameter); 
				}
				chart.redraw();
			}
		}
	}
	
	static public void updateChange() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page  = workbench.getActiveWorkbenchWindow().getActivePage();
		
		IEditorReference editors[] = page.getEditorReferences();
		for(IEditorReference editor: editors) {
			IEditorPart editorPart = editor.getEditor(false);
			if (editorPart instanceof GraphEditor) {
				((GraphEditor)editorPart).refresh();
			}
		}
	}
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
		
		ThreadLevelDataManager objDataManager = exp.getThreadLevelDataManager();

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
		scatterSeries.setSymbolSize(diameter);
		
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
