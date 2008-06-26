package edu.rice.cs.hpc.viewer.experiment;

import edu.rice.cs.hpc.data.experiment.Experiment;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Class to store an experiment data for each instance of Workbench Window
 * This class is used to share the data between different objects within the
 * same workbench window.
 * Since it is possible to have multiple instances of hpcviewer RCP, and 
 * consequently multiple instances of WorkbenchWindow, ExperimentData has
 * to stores multiple experiment data (one for each workbench window).
 * 
 * In order to use this class, user needs to 
 * @author laksonoadhianto
 *
 */
public class ExperimentData {
    private Experiment experimentActive;	// experiment data
    private String[] args; // command line arguments
    private IWorkbenchWindow window;
    private ExperimentManager expManager;
    
	//static private ExperimentData _singleton = null;
	// data key for IWorkbenchWindowConfigurer
	//static public String KEY = "data";
    static private java.util.HashMap<IWorkbenchWindow, ExperimentData> mapData = 
    	new java.util.HashMap<IWorkbenchWindow, ExperimentData>(3);
	
	//========================================================================
    // CONSTRUCTOR
	//========================================================================
    /**
     * Constructor of the class. IF POSSIBLE, DO NOT INSTANTIATE THIS CLASS DIRECTLY
     * INSTEAD, USER SHOULD USE THE METHOD getInstance()
     * @param w: the current workbench window
     */
	public ExperimentData(IWorkbenchWindow w) {
		this.window = w;
	}
	/**
	 * Get the single instance of this class. If the object is already created, then 
	 * return the object.
	 * @return
	 */
	/*
	static public ExperimentData getInstance(IWorkbenchWindow w) {
		if(ExperimentData._singleton == null) {
			ExperimentData._singleton = new ExperimentData(w);
		}
		return ExperimentData._singleton;
	}*/
	
	/**
	 * Retrieve a global experiment data of a given workbench window
	 * If the data doesn't exist, create it, otherwise return the existing data
	 */
	static public ExperimentData getInstance(IWorkbenchWindow w) {
		if (mapData.containsKey(w)) {
			return mapData.get(w);
		} else {
			ExperimentData objData = new ExperimentData(w);
			mapData.put(w, objData);
			return objData;
		}
	}
	/**
	 * Retrieve the active experiment manager
	 * @return
	 */
	public ExperimentManager getExperimentManager() {
		if(this.expManager == null) {
			// normally only one single workbench window !
			//this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			// theoretically, only one single experiment file for one RCP
			// or do we want to support multiple experiments in the future ?
			this.expManager = new ExperimentManager(this.window);//(ExperimentData._singleton.window);
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
