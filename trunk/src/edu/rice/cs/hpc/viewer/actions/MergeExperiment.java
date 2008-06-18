package edu.rice.cs.hpc.viewer.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;

public class MergeExperiment  implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow wParent;
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.wParent = window;
		
	}

	/**
	 * Action to "merge" external experiment into the current experiment
	 * First we ask user to select a database to merge (using folder selection dialog)
	 * Then using the static Experiment.merge method to create a new database
	 */
	public void run(IAction action) {
		// get the database to merge
		ExperimentData expData = ExperimentData.getInstance(this.wParent);
		ExperimentManager expManager= expData.getExperimentManager();
		File[] fileXML = expManager.getDatabaseFileList(this.wParent.getShell(),"Select a database to be augmented");
		
		// if the user click "Ok", then we have to find the XML file
		if(fileXML  != null && fileXML.length>0) {
			String sFile2 = fileXML[0].getAbsolutePath();
			ExperimentView expView = new ExperimentView(this.wParent.getActivePage());
			// load and parse the database into memory
			// TODO: this is not an efficient way to merge, but it' the simplest. We need to improve in the future
			Experiment exp2 = expView.loadExperiment(sFile2);
			// check if this is a valid database
			if(exp2 != null) {
				// create a new database by merging the current database with the new one
				Experiment expNew = Experiment.merge(expData.getExperiment(), exp2);
				// merging process is successful ?
				if(expNew != null) {
					// display to user
					expNew.postprocess();
					expView.generateView(expNew);
					
					// we need to save it
					//expNew.save();
				} else {
			    	// do not continue if the view is not available
			    	MessageDialog.openError(this.wParent.getShell(), "Error", "Unable to merge the experiments.");
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
