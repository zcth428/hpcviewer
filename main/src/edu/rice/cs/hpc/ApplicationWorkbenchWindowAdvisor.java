package edu.rice.cs.hpc;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IWorkbench;

import edu.rice.cs.hpc.viewer.resources.ExperimentData;
import edu.rice.cs.hpc.analysis.ExperimentView;
import edu.rice.cs.hpc.viewer.util.*;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private ExperimentData dataEx ;
	
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String []args) {
		super(configurer);
		if(args != null && args.length > 0) {
			dataEx = ExperimentData.getInstance();
			dataEx.setArguments(args);
		}
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		//configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setTitle("HPCViewer");
		
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
			ExperimentView data = new ExperimentView(this.getWindowConfigurer().getWindow().getActivePage());
		    if(data != null) {
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
		    		data.loadExperimentAndProcess(sFilename);
		     }
		} else {
			// there is no information about the database
			statusline.setMessage(null, "Load an experiment file to start.");
			// we need load the file ASAP
			ExperimentFile expFile = new ExperimentFile(this.getWindowConfigurer().getWindow());
			if(expFile != null) {
				expFile.openFileExperiment();
			}
		}
	}

}
