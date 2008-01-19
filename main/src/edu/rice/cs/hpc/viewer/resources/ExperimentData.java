package edu.rice.cs.hpc.viewer.resources;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class ExperimentData {
    private Experiment experimentActive;	// experiment data
    private String sXMLFilename;	// XML filename
    private String[] args; // command line arguments
    
	static private ExperimentData _singleton = null;
	
	/**
	 * Get the single instance of this class. If the object is already created, then 
	 * return the object.
	 * @return
	 */
	static public ExperimentData getInstance() {
		if(ExperimentData._singleton == null) {
			ExperimentData._singleton = new ExperimentData();
		}
		return ExperimentData._singleton;
	}
	
	/**
	 * Update a new experiment data
	 * @param experiment
	 */
	public void setExperiment(Experiment experiment) {
		this.experimentActive = experiment;
	}
	
	/**
	 * Retrieve the current active experiment
	 * @return
	 */
	public Experiment getExperiment() {
		return this.experimentActive;
	}
	
	/**
	 * Set the XML filename
	 * @param sXMLfile
	 */
	public void setFilename(String sXMLfile) {
		this.sXMLFilename = sXMLfile;
	}
	
	/**
	 * Get the XML filename
	 * @return
	 */
	public String getFilename() {
		return this.sXMLFilename;
	}
	
	/**
	 * Update the commmand line argument
	 * @param arguments
	 */
	public void setArguments(String []arguments) {
		this.args = arguments;
	}
	
	/**
	 * Retrieve the application command line arguments
	 * @return
	 */
	public String[] getArguments() {
		return this.args;
	}
}
