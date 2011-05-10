package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenDatabase extends AbstractHandler {


	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		TraceDatabase trace_db = new TraceDatabase(null);
		trace_db.openDatabase(shell);
		
		return null;
	}


}
