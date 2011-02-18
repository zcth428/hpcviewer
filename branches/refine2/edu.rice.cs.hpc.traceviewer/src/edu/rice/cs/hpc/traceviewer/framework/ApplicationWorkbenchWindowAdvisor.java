package edu.rice.cs.hpc.traceviewer.framework;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.ui.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.ui.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.ui.HPCTraceView;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
	 */
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1200, 800));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
	 */
	public void postWindowOpen() {
		
		//---------------------------------------------------------------------
		// once the widgets have been created, we ask user a database to open
		// ---------------------------------------------------------------------
		
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		
		TraceDatabase trace_db = new TraceDatabase();
		Shell shell = configurer.getWindow().getShell();
		
		if (trace_db.open(shell)) {
			
			//---------------------------------------------------------------------
			// Try to open the database and refresh the data
			// ---------------------------------------------------------------------
			
			File experimentFile = trace_db.getExperimentFile();
			ArrayList<File> traceFiles = trace_db.getTraceFiles();
			
			SpaceTimeData stData = new SpaceTimeData(shell, experimentFile, traceFiles);
			configurer.setData("trace-data", stData);
			
			try {
				//---------------------------------------------------------------------
				// Tell all views that we have the data, and they need to refresh their content
				// ---------------------------------------------------------------------				

				HPCTraceView tview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
				tview.updateData(stData);
				
				HPCDepthView dview = (HPCDepthView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCDepthView.ID);
				dview.updateData(stData);
				
				HPCCallStackView cview = (HPCCallStackView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCCallStackView.ID);
				cview.updateData(stData);
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
	}

}
