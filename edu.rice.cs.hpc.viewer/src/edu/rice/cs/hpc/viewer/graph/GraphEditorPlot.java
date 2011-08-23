package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;
import java.text.DecimalFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;

import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphEditorPlot extends GraphEditor {

    public static final String ID = "edu.rice.cs.hpc.viewer.graph.GraphEditorPlot";
    
	@Override
	protected double[] getValuesX(ThreadLevelDataManager objDataManager, 
			Scope scope, MetricRaw metric) 
	throws NumberFormatException {

		double []x_values = objDataManager.getProcessIDsDouble( metric.getID() );				
		return x_values;
	}

	@Override
	protected double[] getValuesY(ThreadLevelDataManager objDataManager, 
			Scope scope, MetricRaw metric) {

		try {
			double []y_values = objDataManager.getMetrics( metric, scope.getCCTIndex() );
			return y_values;
			
		} catch (IOException e) {
			MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected String getXAxisTitle(ThreadLevelDataFile data) {
		String axis_x;
		IAxisSet axisSet = this.getChart().getAxisSet();
		IAxisTick xTick = axisSet.getXAxis(0).getTick();

		axis_x = "Processes";
		xTick.setFormat(new DecimalFormat("##########"));

		if (data.isHybrid()) 
		{
			axis_x = "Process.Thread";
			xTick.setFormat(new DecimalFormat("######00.00##"));			
		} else if (data.isMultiThreading()) {
			axis_x = "Threads";
			xTick.setFormat(new DecimalFormat("##########"));
		}

		return axis_x;
	}

}
