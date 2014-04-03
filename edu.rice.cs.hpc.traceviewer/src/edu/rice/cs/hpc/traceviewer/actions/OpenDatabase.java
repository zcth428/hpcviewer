package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
import edu.rice.cs.hpc.traceviewer.ui.OpenDatabaseDialog;

public class OpenDatabase extends AbstractHandler
{
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);
		final IStatusLineManager status = vSite.getActionBars().getStatusLineManager();

		OpenDatabaseDialog dlg = new OpenDatabaseDialog(new Shell(), status);
		if (dlg.open()==Window.OK)
			TraceDatabase.openDatabase(window, null, status, dlg.getDBOpener());
		
		
		return null;
	}
}
