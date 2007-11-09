package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;

//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.IViewPart;
import org.eclipse.jface.dialogs.MessageDialog;

//import edu.rice.cs.HPCVision.data.Experiment.*;
/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class LoadExperiment implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public LoadExperiment() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
    	// open a file dialog
    	org.eclipse.swt.widgets.FileDialog fileDialog=new org.eclipse.swt.widgets.FileDialog(
    			window.getShell(),
    			org.eclipse.swt.SWT.OPEN);
    	fileDialog.setText("Load an XML experiment file");
    	String sFile = fileDialog.open();
    	// load the experiment file
    	if(sFile != null) {
    		System.out.println("Opening "+ sFile + " .... ");
    		this.setExperiment(sFile);
 //   		loadData(sFile);
    	}
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void setExperiment(String sFilename) {
		org.eclipse.ui.IWorkbenchPage objPage= this.window.getActivePage();
		// read the XML experiment file
	    edu.rice.cs.hpc.analysis.ExperimentView data = new edu.rice.cs.hpc.analysis.ExperimentView(objPage);
	    if(data != null) {
	    	// data looks OK
	    	data.loadExperimentAndProcess(sFilename);
	    	
	     } else
	    	 return; //TODO we need to throw an exception instead

		edu.rice.cs.hpc.viewer.scope.ScopeView objView=(edu.rice.cs.hpc.viewer.scope.ScopeView) 
			objPage.findView(edu.rice.cs.hpc.viewer.scope.ScopeView.ID);
		if(objView == null) {
   	     //the view is not hidden, instead it has not
   	     //been opened yet
			try {
				objView=(edu.rice.cs.hpc.viewer.scope.ScopeView) objPage.showView(
						edu.rice.cs.hpc.viewer.scope.ScopeView.ID, "Scope", org.eclipse.ui.IWorkbenchPage.VIEW_CREATE);
			} catch(org.eclipse.ui.PartInitException e) {
				MessageDialog.openError(window.getShell(), 
						"Error opening view", "Unabale to open the scope view. Please activate the scope view manually.");
				return;
			}
		}
		//objView.loadExperiment(sFilename);
	}
} 