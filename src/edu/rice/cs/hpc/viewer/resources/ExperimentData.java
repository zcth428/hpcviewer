package edu.rice.cs.hpc.viewer.resources;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.util.ExperimentManager;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ExperimentData {
    private Experiment experimentActive;	// experiment data
    private String[] args; // command line arguments
    private IWorkbenchWindow window;
    private ExperimentManager expManager;
    
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
	 * Retrieve the active experiment manager
	 * @return
	 */
	public ExperimentManager getExperimentManager() {
		if(this.expManager == null) {
			// normally only one single workbench window !
			this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			// theoretically, only one single experiment file for one RCP
			// or do we want to support multiple experiments in the future ?
			this.expManager = new ExperimentManager(ExperimentData._singleton.window);
		}
		return this.expManager;
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
	 * Get the XML filename
	 * @return
	 */
	public String getFilename() {
		return this.experimentActive.getXMLExperimentFile().getAbsolutePath();
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
