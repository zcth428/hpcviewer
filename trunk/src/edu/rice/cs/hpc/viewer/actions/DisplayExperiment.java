package edu.rice.cs.hpc.viewer.actions;

import java.io.FileNotFoundException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.rice.cs.hpc.viewer.util.EditorManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentData;

/**
 * Class to display the content of the XML file (for debugging purpose only)
 * @author laksono
 *
 */
public class DisplayExperiment implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow windowCurrent;
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		this.windowCurrent = window;
	}

	public void run(IAction action) {
		ExperimentData expData = ExperimentData.getInstance(this.windowCurrent);
		if(expData.getExperiment() != null) {
			EditorManager editor = new EditorManager(this.windowCurrent);
			try {
				editor.openFileEditor(expData.getFilename());
			} catch (FileNotFoundException e) {
				MessageDialog.openError(this.windowCurrent.getShell(), 
						"Error: File not found", 
						e.getMessage());
			}
		} else {
			MessageDialog.openError(this.windowCurrent.getShell(), 
					"Error: Need to open an experiment database", 
					"In order to display the XML file of the experiment, you need to load first the experiment database !");
		}
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
