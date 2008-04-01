/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import java.io.File;
import java.io.FilenameFilter;

import edu.rice.cs.hpc.Activator;
import edu.rice.cs.hpc.analysis.ExperimentView;
import edu.rice.cs.hpc.viewer.scope.ScopeView;

/**
 * This class manages to select, load and open a database directory
 * We assume that a database directory contains an XML file (i.e. extension .xml)
 * Warning: This class is not compatible with the old version of experiment file 
 *  (the old version has no xml extension)
 * @author laksono
 *
 */
public class ExperimentFile {
	/**
	 * Last path of the opened directory
	 */
	static public String sLastPath=null;
	/**
	 * pointer to the current active workbench window (supposed to be only one)
	 */
	private IWorkbenchWindow window;
	
	/**
	 * Constructor to instantiate experiment file
	 * @param win: the current workbench window
	 */
	public ExperimentFile(IWorkbenchWindow win) {
		this.window = win;
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		if(ExperimentFile.sLastPath == null)
			ExperimentFile.sLastPath = objPref.getString(PreferenceConstants.P_PATH);
	}
	
	/**
	 * Class to filter the list of files in a directory and return only XML files 
	 * The filter is basically very simple: if the last 3 letters has "xml" substring
	 * then we consider it as XML file.
	 * TODO: we need to have a more sophisticated approach to filter only the real XML files
	 * @author laksono
	 *
	 */
	static class FileXMLFilter implements FilenameFilter {
		public boolean accept(File pathname, String sName) {
			int iLength = sName.length();
			if (iLength <4) // the file should contain at least four letters: ".xml"
				return false;
			String sExtension = (sName.substring(iLength-3, iLength)).toLowerCase();
			return (pathname.canRead() && sExtension.endsWith("xml"));
		}
	}
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * @return true if everything is OK. false otherwise
	 */
	public boolean openFileExperiment() {
		// preparing the dialog for selecting a directory
		DirectoryDialog dirDlg = new DirectoryDialog(this.window.getShell());
		dirDlg.setText("hpcviewer");
		dirDlg.setFilterPath(ExperimentFile.sLastPath);		// recover the last opened path
		dirDlg.setMessage("Select a database directory");
		String sDir = dirDlg.open();	// ask the user to select a directory
		if(sDir == null)
			return false;				// return immediately for "cancel"
		
		// find XML files in this directory
		File files = new File(sDir);
		File filesXML[] = files.listFiles(new FileXMLFilter());
		if(filesXML.length>0) {
			String sFile = filesXML[0].getAbsolutePath(); // by default, we look at the first file
    		System.out.println("Opening "+ sFile + " .... ");
    		ExperimentFile.sLastPath = sDir;
    		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
    		objPref.setValue(PreferenceConstants.P_PATH, sDir);
    		boolean bSuccessful = this.setExperiment(sFile);
    		if(!bSuccessful) {
    			MessageDialog.openError(window.getShell(), "Failed to open a database", "The directory is not a database.\n"+
    					"The database directory has to contains an XML file\n containing the information of the profiling.");
    		} else
    			return true;
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
