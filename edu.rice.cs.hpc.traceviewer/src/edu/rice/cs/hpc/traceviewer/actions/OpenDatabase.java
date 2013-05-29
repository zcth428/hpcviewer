package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenDatabase extends AbstractHandler
{
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);

		TraceDatabase.openDatabase(window, null, vSite.getActionBars().getStatusLineManager(), new LocalDBOpener());
		return null;
	}
}
