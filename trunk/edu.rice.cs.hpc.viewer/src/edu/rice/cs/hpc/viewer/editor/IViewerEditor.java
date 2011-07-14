package edu.rice.cs.hpc.viewer.editor;

import edu.rice.cs.hpc.data.experiment.Experiment;

/****
 * common interface for all editors in hpcviewer
 *  
 * @author laksonoadhianto
 *
 */
public interface IViewerEditor {

	/****
	 * reset the title of the editor
	 */
	public void resetPartName();
	
	
	/***
	 * retrieve the associated experiment of this editor
	 * @return
	 */
	public Experiment getExperiment();
}
