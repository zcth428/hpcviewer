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


	public Experiment duplicate() {

		Experiment copy = new Experiment(null);

		copy.configuration = configuration;
		copy.defaultDirectory = defaultDirectory;
		
		return copy;
	}

}
