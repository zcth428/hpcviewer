package edu.rice.cs.hpc;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.application.IWorkbenchConfigurer;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "edu.rice.cs.hpc.perspective";

	// laks: we need to save and restore the configuration
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		// enable the workbench state save mechanism
		configurer.setSaveAndRestore(true);
		// others
		/*java.util.Map<String, String> env = System.getenv();
		int iSize=env.size();
		String []strenv = (String [])env.keySet().toArray();
		String []strVal = (String []) env.values().toArray();
		for(int i=0;i<iSize;i++) {
			System.out.println("Activator: "+strenv[i] + "->"+strVal[i]);			
		}*/
	}
	
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

}
