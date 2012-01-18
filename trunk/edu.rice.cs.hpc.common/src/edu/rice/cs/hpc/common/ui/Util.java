package edu.rice.cs.hpc.common.ui;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Util {
	
	
	public static IStatusLineManager getActiveStatusLineManager() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		final IWorkbenchPartSite site = window.getActivePage().getActivePart().getSite();
		
		IStatusLineManager statusLine = null;
		
		// --------------------------------------------------------------
		// the current active site can be either editor or view
		// if none of them is active, then we have nothing
		// --------------------------------------------------------------
		if (site instanceof IViewSite)
			statusLine = ((IViewSite)site).getActionBars().getStatusLineManager();
		else if (site instanceof IEditorPart)
			statusLine = ((IEditorSite)site).getActionBars().getStatusLineManager();

		return statusLine;
	}

	
	static public Shell getActiveShell() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return window.getShell();
	}
}
