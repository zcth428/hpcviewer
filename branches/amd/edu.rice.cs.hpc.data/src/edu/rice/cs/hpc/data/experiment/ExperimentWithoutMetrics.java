package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.util.List;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;

/*************************************
 * 
 * Experiment class without metrics
 *
 *************************************/
public class ExperimentWithoutMetrics extends BaseExperiment {

	public ExperimentWithoutMetrics(File filename) {
		super(filename);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#setMetrics(java.util.List)
	 */
	public void setMetrics(List<BaseMetric> metricList) 
	{
		// no action needed here, since we don't handle metrics
	}

}
