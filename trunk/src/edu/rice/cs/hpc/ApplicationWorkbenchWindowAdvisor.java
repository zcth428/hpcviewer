package edu.rice.cs.hpc;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IWorkbench;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.viewer.resources.ExperimentData;
import edu.rice.cs.hpc.analysis.ExperimentView;
import edu.rice.cs.hpc.viewer.util.*;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private ExperimentData dataEx ;

	/**
	 * Creates a new workbench window advisor for configuring a workbench window via the given workbench window configurer
	 * Retrieve the RCP's arguments and verify if it contains database to open
	 * 
	 * @param configurer
	 * @param args
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String []args) {
		super(configurer);
		if(args != null && args.length > 0) {
			dataEx = ExperimentData.getInstance();
			dataEx.setArguments(args);
		}
	}

	/**
	 * Creates a new action bar advisor to configure the action bars of the window via 
	 * the given action bar configurer. The default implementation returns a new instance of ActionBarAdvisor
	 */
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/**
	 * Performs arbitrary actions before the window is opened.
	 */
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(false);	// remove toolbar/coolbar
		configurer.setShowStatusLine(true);	// show status bar
		configurer.setTitle("hpcviewer");	// default title (to be updated)
		
	}

	/**
	 * Action when the window is already opened
	 */
	public void postWindowOpen() {
		// set the perspective (to setup the view as well)
		IWorkbench workbench = org.eclipse.ui.PlatformUI.getWorkbench();
		try {
		   workbench.showPerspective("edu.rice.cs.hpc.perspective", 
		      workbench.getActiveWorkbenchWindow());
		   
		} catch (org.eclipse.ui.WorkbenchException e) {
			e.printStackTrace();
		}
		// set the status bar
		org.eclipse.jface.action.IStatusLineManager statusline = getWindowConfigurer()
		.getActionBarConfigurer().getStatusLineManager();
		// -------------------
		// see if the argument provides the database to load
		if(this.dataEx != null) {
			// possibly we have express the experiment file in the command line
			ExperimentView expViewer = new ExperimentView(this.getWindowConfigurer().getWindow().getActivePage());
		    if(expViewer != null) {
		    	// data looks OK
		    	String []sArgs = this.dataEx.getArguments();
		    	String sFilename = null;
		    	for(int i=0;i<sArgs.length;i++) {
		    		if(sArgs[i].charAt(0) != '-') {
		    			sFilename = sArgs[i];
		    			break;
		    		}
		    	}
		    	if(sFilename != null)
		    		expViewer.asyncLoadExperimentAndProcess(sFilename);
		     }
		} else {
			// there is no information about the database
			statusline.setMessage(null, "Load a database to start.");
			// we need load the file ASAP
			this.dataEx = ExperimentData.getInstance();
			ExperimentManager expFile = this.dataEx.getExperimentManager();
			if(expFile != null) {
				Shell objShell = this.getWindowConfigurer().getWindow().getShell();
				if(objShell != null)
					expFile.openFileExperiment(objShell);
				else
					System.out.println("AWWA: shell is null. please open the database manually.");
			} else {
				System.out.println("AWWA: exp manager is null. create a new one.");
			}
		}
	}

	/**
	 * Performs arbitrary actions as the window's shell is being closed directly, and possibly veto the close.
	 */
	public boolean preWindowShellClose() {
		boolean bClosed = this.getWindowConfigurer().getWindow().getActivePage().closeAllEditors(false);
		//System.out.println("Close all editors:"+bClosed);
		return super.preWindowShellClose();
	}
}
