package edu.rice.cs.hpc.traceviewer.framework;

import org.eclipse.jface.action.IStatusLineManager;
//import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
import edu.rice.cs.hpc.traceviewer.ui.OpenDatabaseDialog;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	final private String args[];
	
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String []_args) {
		super(configurer);
		args = _args;
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

		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		
		final IWorkbenchWindow window = configurer.getWindow();
		if (!Util.checkJavaVendor(window.getShell()))
			window.close();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
	 */ 
	public void postWindowOpen() {
		
		//---------------------------------------------------------------------
		// once the widgets have been created, we ask user a database to open
		// ---------------------------------------------------------------------
		
		final IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		final IStatusLineManager status = configurer.getActionBarConfigurer().getStatusLineManager();
		
		//process command line argument - currently only works for local but can be easily modified to work with remote
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg != null && arg.charAt(0) != '-') {
					// this must be the name of the database to open
					TraceDatabase.openDatabase(configurer.getWindow(), args, status, new LocalDBOpener(arg));
				}
			}
		} else { //if no command line argument open dialog box
			AbstractDBOpener opener;
			OpenDatabaseDialog dlg = new OpenDatabaseDialog(new Shell(), status); 
			dlg.open();
			opener=dlg.getDBOpener();
			TraceDatabase.openDatabase(configurer.getWindow(), args, status, opener);
		}
	
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowClose()
	 */
	public void postWindowClose() {

		// remove all the allocated resources of this window
		TraceDatabase.removeInstance(this.getWindowConfigurer().getWindow());
	}
}
