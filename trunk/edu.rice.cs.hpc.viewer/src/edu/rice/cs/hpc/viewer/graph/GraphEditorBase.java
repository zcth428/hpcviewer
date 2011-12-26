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
import edu.rice.cs.hpc.viewer.experiment.ThreadLevelDataManager;

public abstract class GraphEditorBase extends EditorPart implements IViewerEditor {
    private Chart chart;
	protected ThreadLevelDataManager threadData;

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
		
		if (input instanceof GraphEditorInput) {
			final GraphEditorInput editorInput = (GraphEditorInput) input; 
			threadData = editorInput.getDatabase().getThreadLevelDataManager();
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
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
		Experiment exp = editor_input.getDatabase().getExperiment();
		Scope scope = editor_input.getScope();
		MetricRaw metric = editor_input.getMetric();
		
		this.plotData(exp, scope, metric);
	}

	public void resetPartName() {
		GraphEditorInput input = (GraphEditorInput) this.getEditorInput();
		final String name = input.getName();
		this.setPartName(name);
	}
	
	
	public Experiment getExperiment() {
		GraphEditorInput input = (GraphEditorInput) this.getEditorInput();
		return input.getDatabase().getExperiment();
	}

	
	protected Chart getChart() {
		return this.chart;
	}

	
	protected abstract void plotData( Experiment exp, Scope scope, MetricRaw metric );

}
