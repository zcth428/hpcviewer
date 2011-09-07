package edu.rice.cs.hpc.viewer.scope;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.experiment.ThreadLevelDataManager;

public class ThreadMetricColumnLabelProvider extends BaseMetricColumnProvider {

	private MetricRaw _metric;
	private ThreadLevelDataManager dataManager;
	private int _rank_sequence;
	
	public ThreadMetricColumnLabelProvider( Experiment experiment, int rank_sequence, MetricRaw metric ) {
		super();
		_metric = metric;
		//dataManager = experiment.getThreadLevelDataManager();
	}
	
	
	public String getText(Object element) {
		String text = null; 

/*		if ((_metric != null) && (element instanceof Scope)) {
			
			Scope node = (Scope) element;
			try {
				//double value = dataManager.getMetric(_rank_sequence, _metric, node.getCCTIndex());
				//text = String.valueOf(value);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}*/
		return text;

	}
}
