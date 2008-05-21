/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.swt.widgets.Shell;

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
public class ExperimentManager {
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
	public ExperimentManager(IWorkbenchWindow win) {
		this.window = win;
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		if(ExperimentManager.sLastPath == null)
			ExperimentManager.sLastPath = objPref.getString(PreferenceConstants.P_PATH);
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
	 * Get the list of database file name
	 * @param sTitle
	 * @return the list of XML files in the selected directory
	 * null if the user click the "cancel" button
	 */
	public File[] getDatabaseFileList(Shell shell, String sTitle) {
		// preparing the dialog for selecting a directory
		Shell objShell = shell;
		if(shell == null)
			if(this.window == null) {
				this.window = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				objShell = this.window.getShell();
			}
		DirectoryDialog dirDlg = new DirectoryDialog(objShell);
		dirDlg.setText("hpcviewer");
		dirDlg.setFilterPath(ExperimentManager.sLastPath);		// recover the last opened path
		dirDlg.setMessage(sTitle);
		String sDir = dirDlg.open();	// ask the user to select a directory
		if(sDir != null){
			// find XML files in this directory
			File files = new File(sDir);
			// for debugging purpose, let have separate variable
			File filesXML[] = files.listFiles(new FileXMLFilter());
			// store it in the class variable for further usage
    		ExperimentManager.sLastPath = sDir;
    		// store the current path in the preference
    		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
    		objPref.setValue(PreferenceConstants.P_PATH, sDir);
			return filesXML;
		}
		
		return null;
	}
	
	/**
	 * Open experiment database based on database directory
	 * @param sDatabaseDir
	 * @return
	 */
	public boolean openFileExperimentFromDatabase(String sDatabaseDir) {
		// find XML files in this directory
		File files = new File(sDatabaseDir);
		// for debugging purpose, let have separate variable
		File filesXML[] = files.listFiles(new FileXMLFilter());
		// store it in the class variable for further usage
		ExperimentManager.sLastPath = sDatabaseDir;
		// store the current path in the preference
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		objPref.setValue(PreferenceConstants.P_PATH, sDatabaseDir);

		return openFileExperimentFromFiles(filesXML);
	}
	
	/**
	 * Open database that can be based on the database directory or file
	 * @param sPath
	 * @return
	 */
	public boolean openFileOrDatabase(String sPath) {
		File objPath = new File(sPath);
		if(objPath.isFile()) {
			return this.setExperiment(sPath);
		} else if(objPath.isDirectory()) {
			return this.openFileExperimentFromDatabase(sPath);
		} 
		return false;
	}
	
	/**
	 * Open an experiment database based on given an array of java.lang.File
	 * @param filesXML: list of files
	 * @return true if the opening is successful
	 */
	private boolean openFileExperimentFromFiles(File []filesXML) {
		if((filesXML != null) && (filesXML.length>0)) {
			boolean bContinue = true;
			// let's make it complicated: assuming there are more than 1 XML file in this directory,
			// we need to test one by one if it is a valid database file.
			// Problem: if in the directory it has two XML files, then the second one will NEVER be opened !
			for(int i=0;i<(filesXML.length) && (bContinue);i++) {
				String sFile=filesXML[i].getAbsolutePath();
				// we will continue to verify the content of the list of XML files
				// until we fine the good one.
		    	bContinue = (this.setExperiment(sFile) == false);
		    	//System.out.println(fileXML[i].getName()+":"+(!bContinue));
			}
	   		if(bContinue) {
	   			MessageDialog.openError(window.getShell(), "Failed to open a database", "The directory is not a database.\n"+
	    					"The database directory has to contains at least one XML file\n containing the information of the profiling.");
	   		} else
	   			return true;
		}
		return false;
	}
	
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * @return true if everything is OK. false otherwise
	 */
	public boolean openFileExperiment(Shell shell) {
		File []fileXML = this.getDatabaseFileList(shell, "Select a directory containing a profiling database.");
    	return this.openFileExperimentFromFiles(fileXML);
	}
	
	/**
	 * Get the experiment to be processed
	 * @param sFilename
	 * @return
	 */
	private boolean setExperiment(String sFilename) {
		IWorkbenchPage objPage= this.window.getActivePage();
		// read the XML experiment file
		ExperimentView expViewer = new ExperimentView(objPage);
	    if(expViewer != null) {
	    	// data looks OK
	    	expViewer.loadExperimentAndProcess(sFilename);
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
