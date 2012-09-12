package edu.rice.cs.hpc.viewer.graph;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.Chart;
import org.swtchart.Range;
import org.swtchart.ext.InteractiveChart;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.editor.IViewerEditor;
import edu.rice.cs.hpc.viewer.metric.ThreadLevelDataManager;
import edu.rice.cs.hpc.viewer.util.WindowTitle;


/**
 * Base class for hpcviewer editor to display graph
 *  
 * The class implements IViewerEditor, so it can be renamed, manipulated and changed
 * 	by the viewer manager
 */
public abstract class GraphEditorBase extends EditorPart implements IViewerEditor {
	
	// chart is used to plot graph or histogram on canvas. each editor has its own chart
    private Chart chart;
    
    // a database of an experiment containing raw metrics to plot
	protected ThreadLevelDataManager threadData;

	//@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	//@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
	}

	//@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		this.setSite(site);
		this.setInput(input);
		
		if (input instanceof GraphEditorInput) {
			final GraphEditorInput editorInput = (GraphEditorInput) input; 
			threadData = editorInput.getDatabase().getThreadLevelDataManager();
		}
	}

	//@Override
	public boolean isDirty() {
		return false;
	}

	//@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}


	//@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	/******
	 * Do finalization of the editor
	 * 
	 * Due to SWT Chart bug, we need to adjust the range once the create-part-control
	 * 	finishes its layout.
	 */
	public void finalize() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		axisSet.adjustRange();

		// set the lower range to be zero so that we can see if there is load imbalance or not
		Range range = axisSet.getAxes()[1].getRange();
		if (range.lower > 0) {
			range.lower = 0;
			axisSet.getAxes()[1].setRange(range);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		
		IEditorInput input = this.getEditorInput();
		if (input == null || !(input instanceof GraphEditorInput) )
			throw new RuntimeException("Invalid input for graph editor");
		
		GraphEditorInput editor_input = (GraphEditorInput) input;
		String title = editor_input.getName();
		
		this.setPartName( title );

		// set the window title with a possible db number
		WindowTitle wt = new WindowTitle();
		wt.setEditorTitle(this.getEditorSite().getWorkbenchWindow(), this); //, exp, editorName);

		//----------------------------------------------
		// chart creation
		//----------------------------------------------
		chart = new InteractiveChart(parent, SWT.NONE);
		chart.getTitle().setText( title );


		//----------------------------------------------
		// formatting axis
		//----------------------------------------------
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("0.0##E0##"));

		// turn off the legend
		chart.getLegend().setVisible(false);
		
		//----------------------------------------------
		// plot data
		//----------------------------------------------
		Scope scope = editor_input.getScope();
		MetricRaw metric = editor_input.getMetric();
		
		this.plotData(scope, metric);
	}


	public String getEditorPartName() {
		final GraphEditorInput input = (GraphEditorInput) this.getEditorInput();
		final String name = input.getName();
		return name;
	}

	public void setEditorPartName(String title) {
		this.setPartName(title);
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.editor.IViewerEditor#getExperiment()
	 */
	public Experiment getExperiment() {
		final GraphEditorInput input = (GraphEditorInput) this.getEditorInput();
		return input.getDatabase().getExperiment();
	}

	
	protected Chart getChart() {
		return this.chart;
	}

	/**
	 * method to plot a graph of a specific scope and metric of an experiment
	 * 
	 * @param scope: the scope to plot
	 * @param metric: the raw metric to plot
	 */
	protected abstract void plotData(Scope scope, MetricRaw metric );

}
