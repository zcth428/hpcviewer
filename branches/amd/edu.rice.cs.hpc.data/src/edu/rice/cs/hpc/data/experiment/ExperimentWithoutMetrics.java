package edu.rice.cs.hpc.data.experiment;

import java.util.List;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;

/*************************************
 * 
 * Experiment class without metrics
 *
 *************************************/
public class ExperimentWithoutMetrics extends BaseExperiment {

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#setMetrics(java.util.List)
	 */
	public void setMetrics(List<BaseMetric> metricList) 
	{
		// no action needed here, since we don't handle metrics
	}

}
