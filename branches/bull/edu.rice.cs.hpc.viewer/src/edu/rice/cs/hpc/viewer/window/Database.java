package edu.rice.cs.hpc.viewer.window;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;

public class Database {
	private int winIndex;
	private int dbIndex;
	private Experiment experiment;
	private ExperimentView view;

	/**
	 *  get the index of the viewer window in which this database is displayed.
	 * @return
	 */
	public int getWindowIndex () {
		return winIndex;
	}

	/**
	 *  get the index of which database in a window this object represents.
	 * @return
	 */
	public int getDatabaseIndex () {
		return dbIndex;
	}

	/**
	 *  get the Experiment class used for this database
	 * @return
	 */
	public Experiment getExperiment () {
		return experiment; //this.view.getExperimentData().getExperiment(); // 
	}

	/**
	 *  get the ExperimentView class used for this database
	 * @param path
	 */
	public ExperimentView getExperimentView () {
		return view;
	}

	/**
	 *  set the viewer window index to record the window in which this database is displayed.
	 * @param index
	 */
	public void setWindowIndex (int index) {
		winIndex = index;
		return; 
	}

	/**
	 *  set the database index to record which database in a window this object represents.
	 * @param index
	 */
	public void setDatabaseIndex (int index) {
		dbIndex = index;
		return; 
	}

	/**
	 *  set the Experiment class used for this database
	 * @param path
	 */
	public void setExperiment (Experiment exper) {
		experiment = exper;
		return;
	}

	/**
	 *  set the ExperimentView class used for this database
	 * @param path
	 */
	public void setExperimentView (ExperimentView experView) {
		view = experView;
		return;
	}
}
