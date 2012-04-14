package edu.rice.cs.hpc.data.experiment;

import java.io.File;

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
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#duplicate()
	 */
	public ExperimentWithoutMetrics duplicate() {

		ExperimentWithoutMetrics copy = new ExperimentWithoutMetrics(null);

		copy.configuration = configuration;
		copy.defaultDirectory = defaultDirectory;
		
		return copy;
	}

}
