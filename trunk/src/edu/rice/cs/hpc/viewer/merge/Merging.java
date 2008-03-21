package edu.rice.cs.hpc.viewer.merge;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class Merging {
	private Shell objShell;
	private IWorkbenchWindow wParent;
	
	public Merging(Shell shell, IWorkbenchWindow window) {
		this.objShell = shell;
		this.wParent = window;
	}
	
	/**
	 * Merging two different experiment files. We assume the two files are not the same
	 * @param sFile1: the name of the first file
	 * @param sFile2: the name of the other one
	 */
	public void Merge(String sFile1, String sFile2) {
		// prepare the view
		org.eclipse.ui.IWorkbenchPage objPage= this.wParent.getActivePage();
		// read the XML experiment file
	    edu.rice.cs.hpc.analysis.ExperimentView data = new edu.rice.cs.hpc.analysis.ExperimentView(objPage);
	    if(data != null) {
	    	// merge two files
	    	Experiment ex1 = data.loadExperiment(sFile1);
	    	Experiment ex2 = data.loadExperiment(sFile2);
	    	// merging !
	    	Experiment exMerged = Experiment.merge(ex1, ex2);
	    	if(exMerged != null) {
		    	exMerged.postprocess();		// prepare the output
		    	data.generateView(exMerged);	// show the view
	    	}
	    } else {
	    	// do not continue if the view is not available
	    	org.eclipse.jface.dialogs.MessageDialog.openError(this.objShell, "Error", "Unable to open a view.");
	    }
	}
}
