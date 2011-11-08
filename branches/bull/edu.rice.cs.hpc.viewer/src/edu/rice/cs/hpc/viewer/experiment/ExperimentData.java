package edu.rice.cs.hpc.viewer.experiment;

import edu.rice.cs.hpc.data.experiment.Experiment;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * Class to store an experiment data for each instance of Workbench Window
 * This class is used to share the data between different objects within the
 * same workbench window.
 * Since it is possible to have multiple instances of hpcviewer RCP, and 
 * consequently multiple instances of WorkbenchWindow, ExperimentData has
 * to stores multiple experiment data (one for each workbench window).
 * 
 * In order to use this class, user needs to call getInstance() method first 
 * 
 * @author laksonoadhianto
 *
 */
public class ExperimentData {
    private Experiment experimentActive;	// experiment data
    private IWorkbenchWindow window;
    private ExperimentManager expManager;
    
    private ThreadLevelDataManager threadsData = null;

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
	 * Retrieve the current experiment manager of this workbench window.
	 * Remark: each workbench window has its own data and its own ExperimentManager
	 * @return
	 */
	public ExperimentManager getExperimentManager() {
		if(this.expManager == null) {
			// In order to reduce memory consumption we need to make sure that only one
			// ExperimentManager is created per workbench window
			this.expManager = new ExperimentManager(this.window);
		}
		return this.expManager;
	}
	/**
	 * Update a new experiment data
	 * @param experiment
	 */
	public void setExperiment(Experiment experiment) {
		this.experimentActive = experiment;
		threadsData = new ThreadLevelDataManager(experiment);
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
	
	public ThreadLevelDataManager getThreadLevelDataManager() {
		return this.threadsData;
	}
}
