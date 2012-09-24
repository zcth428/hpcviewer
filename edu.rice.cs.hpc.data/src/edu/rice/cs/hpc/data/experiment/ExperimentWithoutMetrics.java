package edu.rice.cs.hpc.data.experiment;

/*************************************
 * 
 * Experiment class without metrics
 *
 *************************************/
public class ExperimentWithoutMetrics extends BaseExperiment {



	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#duplicate()
	 */
	public ExperimentWithoutMetrics duplicate() {

		ExperimentWithoutMetrics copy = new ExperimentWithoutMetrics();

		copy.configuration = configuration;
		copy.fileExperiment = fileExperiment;
		
		return copy;
	}

}
