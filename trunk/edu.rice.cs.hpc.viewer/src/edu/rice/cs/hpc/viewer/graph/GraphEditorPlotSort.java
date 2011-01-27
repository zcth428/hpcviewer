package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;

import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile.ApplicationType;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphEditorPlotSort extends GraphEditor {

    public static final String ID = "edu.rice.cs.hpc.viewer.graph.GraphEditorPlotSort";


	@Override
	protected double[] getValuesX(ThreadLevelDataManager objDataManager, 
			Scope scope, MetricRaw metric) {

		double x_values[] = objDataManager.getProcessIDsDouble(metric.getID());
		double sequence_x[] = new double[x_values.length];
		for (int i=0; i<x_values.length; i++) {
			sequence_x[i] = (double) i;
		}
		return sequence_x;
	}



	@Override
	protected double[] getValuesY(ThreadLevelDataManager objDataManager, 
			Scope scope, MetricRaw metric) {

		double y_values[] = null;
		try {
			y_values = objDataManager.getMetrics( metric, scope.getCCTIndex());
			
			java.util.Arrays.sort(y_values);
						
		} catch (IOException e) {
			MessageDialog.openError(this.getSite().getShell(), "Error reading file !", e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
		}			
		return y_values;
	}



	@Override
	protected String getXAxisTitle(ApplicationType type) {
		return "Rank sequence";
	}

	

}
