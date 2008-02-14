/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;

import edu.rice.cs.hpc.analysis.ExperimentView;
import edu.rice.cs.hpc.viewer.scope.ScopeView;
/**
 * @author laksono
 *
 */
public class ExperimentFile {

	private IWorkbenchWindow window;
	
	/**
	 * 
	 * @param win
	 */
	public ExperimentFile(IWorkbenchWindow win) {
		this.window = win;
	}
	
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * @return true if everything is OK. false otherwise
	 */
	public boolean openFileExperiment() {
		FileDialog fileDialog=new FileDialog(window.getShell(),
    			org.eclipse.swt.SWT.OPEN);
    	fileDialog.setText("Load an XML experiment file");
    	String sFile = fileDialog.open();
    	// load the experiment file
    	if(sFile != null) {
    		System.out.println("Opening "+ sFile + " .... ");
    		return this.setExperiment(sFile);
    	}
    	return false;
	}
	
	/**
	 * Get the experiment to be processed
	 * @param sFilename
	 * @return
	 */
	private boolean setExperiment(String sFilename) {
		IWorkbenchPage objPage= this.window.getActivePage();
		// read the XML experiment file
		ExperimentView data = new ExperimentView(objPage);
	    if(data != null) {
	    	// data looks OK
	    	data.loadExperimentAndProcess(sFilename);
	     } else
	    	 return false; //TODO we need to throw an exception instead

	    ScopeView objView=(ScopeView) objPage.findView(ScopeView.ID);
		if(objView == null) {
   	     //the view is not hidden, instead it has not
   	     //been opened yet
			try {
				objView=(ScopeView) objPage.showView(
						ScopeView.ID, "Scope", IWorkbenchPage.VIEW_CREATE);
			} catch(org.eclipse.ui.PartInitException e) {
				MessageDialog.openError(window.getShell(), 
						"Error opening view", "Unabale to open the scope view. Please activate the scope view manually.");
				return false;
			}
		}
		return true;
	}

}
