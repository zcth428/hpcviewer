package edu.rice.cs.hpc.viewer.graph;

import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ThreadDataModel {
	private class DataModel {
		double id;
		MetricRaw metric;
	}
	
	Scope scope;
	DataModel data[];
}
