package edu.rice.cs.hpc;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.util.*;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private ExperimentData dataEx ;
	private IWorkbench workbench;
	/**
	 * Creates a new workbench window advisor for configuring a workbench window via the given workbench window configurer
	 * Retrieve the RCP's arguments and verify if it contains database to open
	 * 
	 * @param configurer
	 * @param args
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String []args) {
		super(configurer);
		this.workbench = configurer.getWindow().getWorkbench();
		if(args != null && args.length > 0) {
			dataEx = ExperimentData.getInstance(this.workbench.getActiveWorkbenchWindow());
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
		/*
		 * try {
		   workbench.showPerspective("edu.rice.cs.hpc.perspective", 
		      workbench.getActiveWorkbenchWindow());
		   
		} catch (org.eclipse.ui.WorkbenchException e) {
			e.printStackTrace();
		}
		*/
		// set the status bar
		IWorkbenchWindow windowCurrent = workbench.getActiveWorkbenchWindow(); 
		org.eclipse.jface.action.IStatusLineManager statusline = getWindowConfigurer()
		.getActionBarConfigurer().getStatusLineManager();
		// -------------------
		// see if the argument provides the database to load
		if(this.dataEx != null) {
			// possibly we have express the experiment file in the command line
			if(windowCurrent == null) {
				System.err.println("Anomaly event occured: active window not found");
				return;
			}
			IWorkbenchPage pageCurrent = windowCurrent.getActivePage();
			if(pageCurrent == null) {
				System.err.println("Anomaly event occured: active page not found");
			}
			ExperimentView expViewer = new ExperimentView(pageCurrent);
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
		    	if(sFilename != null) {
		    		// Bug fixed: DO NOT make a reference of window based on configurer
		    		// In some machines (especially the slow ones) the window is not instantiated yet !!
		    		ExperimentData objData = ExperimentData.getInstance(windowCurrent);
		    		objData.getExperimentManager().openDatabase(sFilename);
		    		//expViewer.asyncLoadExperimentAndProcess(sFilename);
		    	} else 
		    		this.openDatabase();
		     } else {
		    	 statusline.setMessage("Cannot relocate the viewer. Please open the database manually.");
		    	 System.err.println("Cannot relocate the viewer. Please open the database manually.");
		    	 
		     }
		} else {
			// there is no information about the database
			statusline.setMessage(null, "Load a database to start.");
			// we need load the file ASAP
			this.openDatabase();
		}
	}
	
	/**
	 * Open an experiment database. A database is a folder that contains XML experiment files 
	 * (only the first one will be taken into account)
	 */
	private void openDatabase() {
		this.dataEx = new ExperimentData(this.workbench.getActiveWorkbenchWindow());
		//ExperimentData.getInstance(this.workbench.getActiveWorkbenchWindow());
		ExperimentManager expFile = this.dataEx.getExperimentManager();
		if(expFile != null) {
			IWorkbenchWindow windowCurrent = workbench.getActiveWorkbenchWindow();
			if(windowCurrent != null) {
				Shell objShell = windowCurrent.getShell();
				if(objShell != null)
					expFile.openFileExperiment();
				else
					System.out.println("AWWA: shell is null. please open the database manually.");
			} else 
				System.err.println("AWWA: No active window detected");

		} else {
			System.out.println("AWWA: exp manager is null. create a new one.");
		}
	}

	/**
	 * Performs arbitrary actions as the window's shell is being closed directly, and possibly veto the close.
	 */
	public boolean preWindowShellClose() {
		boolean bClosed = this.workbench.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		//System.out.println("Close all editors:"+bClosed);
		return super.preWindowShellClose();
	}
}
