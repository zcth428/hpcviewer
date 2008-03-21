package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.rice.cs.hpc.viewer.util.EditorManager;
import edu.rice.cs.hpc.viewer.resources.ExperimentData;

public class DisplayExperiment implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow windowCurrent;
	private ExperimentData globalData;
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		this.windowCurrent = window;
	}

	public void run(IAction action) {
		this.globalData = ExperimentData.getInstance();
		if(this.globalData.getExperiment() != null) {
			EditorManager editor = new EditorManager(this.windowCurrent);
			editor.openFileEditor(this.globalData.getFilename());
		} else {
			org.eclipse.jface.dialogs.MessageDialog.openError(this.windowCurrent.getShell(), 
					"Error: Need to open an experiment database", 
					"In order to display the XML file of the experiment, you need to load first the experiment database !");
		}
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
