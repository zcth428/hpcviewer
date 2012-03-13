package edu.rice.cs.hpc.common.ui;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.part.EditorPart;

public class Util {
	
	
	public static IStatusLineManager getActiveStatusLineManager() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		final IWorkbenchPartSite site = window.getActivePage().getActivePart().getSite();
		
		IStatusLineManager statusLine = null;
		
		// --------------------------------------------------------------
		// the current active site can be either editor or view
		// if none of them is active, then we have nothing
		//
		//	@TODO ugly code: this is a hack version to identify the current "site"
		//		   status bar is hosted in a view site or editor site. Other than that, we are doomed.
		// --------------------------------------------------------------
		if (site instanceof IViewSite)
			statusLine = ((IViewSite)site).getActionBars().getStatusLineManager();
		else if (site instanceof IEditorPart)
			statusLine = ((IEditorSite)site).getActionBars().getStatusLineManager();
		else if (site instanceof EditorSite)
			statusLine = ((EditorSite)site).getActionBars().getStatusLineManager();
		else if (site instanceof EditorPart){
			statusLine = ((EditorPart) site).getEditorSite().getActionBars().getStatusLineManager();
		} else {
			System.out.println("unknown active site: " + site + " \t class: " + site.getClass());
		}

		return statusLine;
	}

	
	static public Shell getActiveShell() {
		final IWorkbenchWindow window = getActiveWindow();
		return window.getShell();
	}
	
	static public IWorkbenchWindow getActiveWindow() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return window;
	}
}
