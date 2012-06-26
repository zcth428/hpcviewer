package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenDatabase extends AbstractHandler
{
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);

		AbstractDBOpener DBOpener;
		
		if (AbstractDBOpener.Local)//TODO: figure out how we want to switch between Remote and Local storage
			DBOpener = new LocalDBOpener();
		else
			DBOpener = new RemoteDBOpener();
		
		TraceDatabase.openDatabase(window, null, vSite.getActionBars().getStatusLineManager(), DBOpener);
		return null;
	}
}
