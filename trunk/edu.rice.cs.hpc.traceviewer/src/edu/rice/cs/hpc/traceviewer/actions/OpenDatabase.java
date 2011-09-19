package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenDatabase extends AbstractHandler
{
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final Shell shell = window.getShell();
		final IWorkbenchPartSite site = window.getActivePage().getActivePart().getSite();
		final IViewSite vSite = ( IViewSite ) site;

		TraceDatabase trace_db = new TraceDatabase(null);
		trace_db.openDatabase(shell, vSite.getActionBars().getStatusLineManager());
		return null;
	}
}
