package edu.rice.cs.hpc.viewer.resources;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class ExperimentData {
    private Experiment experimentActive;
    private String sXMLFilename;
    
	static private ExperimentData _singleton = null;
	
	static public ExperimentData getInstance() {
		if(ExperimentData._singleton == null) {
			ExperimentData._singleton = new ExperimentData();
		}
		return ExperimentData._singleton;
	}
	
	public void setExperiment(Experiment experiment) {
		this.experimentActive = experiment;
	}
	
	public Experiment getExperiment() {
		return this.experimentActive;
	}
	
	public void setFilename(String sXMLfile) {
		this.sXMLFilename = sXMLfile;
	}
	
	public String getFilename() {
		return this.sXMLFilename;
	}
}
