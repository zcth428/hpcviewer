package edu.rice.cs.hpc;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IWorkbench;

import edu.rice.cs.hpc.viewer.resources.ExperimentData;

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
	public void postWindowOpen() {

		IWorkbench workbench = org.eclipse.ui.PlatformUI.getWorkbench();
		try {
		   workbench.showPerspective("edu.rice.cs.hpc.perspective", 
		      workbench.getActiveWorkbenchWindow());
		   
		} catch (org.eclipse.ui.WorkbenchException e) {
			e.printStackTrace();
		}
		org.eclipse.jface.action.IStatusLineManager statusline = getWindowConfigurer()
		.getActionBarConfigurer().getStatusLineManager();
		if(this.dataEx != null) {
			// possibly we have express the experiment file in the command line
		    edu.rice.cs.hpc.analysis.ExperimentView data = new edu.rice.cs.hpc.analysis.ExperimentView(this.getWindowConfigurer().getWindow().getActivePage());
		    if(data != null) {
		    	// data looks OK
		    	String sFilename = this.dataEx.getArguments()[0];
		    	data.loadExperimentAndProcess(sFilename);
		     }
		} else
			statusline.setMessage(null, "Load an experiment file to start.");
	}

}
