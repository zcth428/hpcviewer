package edu.rice.cs.hpc.viewer.graph;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.Range;
import org.swtchart.Chart;
import org.swtchart.ext.InteractiveChart;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public abstract class GraphEditorBase extends EditorPart {
    private Chart chart;

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
	public void setFocus() {
		// TODO Auto-generated method stub

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
		// axis title
		//----------------------------------------------
//		chart.getAxisSet().getXAxis(0).getTitle().setText( getXAxisTitle() );
//		chart.getAxisSet().getYAxis(0).getTitle().setText( getYAxisTitle() );

		//----------------------------------------------
		// formatting axis
		//----------------------------------------------
		IAxisSet axisSet = chart.getAxisSet();
//		IAxisTick xTick = axisSet.getXAxis(0).getTick();
//		xTick.setFormat(new DecimalFormat("######00.00##"));
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("0.0##E0##"));

		// turn off the legend
		chart.getLegend().setVisible(false);
		
		//----------------------------------------------
		// plot data
		//----------------------------------------------
		Experiment exp = editor_input.getExperiment();
		Scope scope = editor_input.getScope();
		MetricRaw metric = editor_input.getMetric();
		
		this.plotData(exp, scope, metric);
	}

	protected Chart getChart() {
		return this.chart;
	}

	/***
	 * temporary SWTChart bug fix 
	 * add axis padding for scatter graph
	 */
	protected void updateRange(int num_items_x) {
		Chart chart = getChart();
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
	protected void updateRange(double width) {
		Chart chart = getChart();
		IAxis axis = chart.getAxisSet().getXAxis(0);
		Range range = axis.getRange();
		double pad = 0.5 * width;
		Range new_range = new Range(range.lower - pad, range.upper + pad);
		axis.setRange(new_range);
		chart.updateLayout();		
	}
	
	
//	protected abstract String getXAxisTitle();
//	protected abstract String getYAxisTitle();
//	protected abstract void setAxisTitle();
	protected abstract void plotData( Experiment exp, Scope scope, MetricRaw metric );

}
