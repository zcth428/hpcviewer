package edu.rice.cs.hpc.traceviewer.util;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.data.util.IProgressReport;

public class TraceProgressReport implements IProgressReport 
{
	final private IStatusLineManager _statusMgr;
	
	public TraceProgressReport(IStatusLineManager statusMgr )
	{
		this._statusMgr = statusMgr;
	}
	
	public void begin(String title, int num_tasks) {
		_statusMgr.setMessage(title);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().update();
		_statusMgr.getProgressMonitor().beginTask("Starting: "+title, num_tasks);
	}

	public void advance() {
		_statusMgr.getProgressMonitor().worked(1);
	}

	public void end() {
		_statusMgr.getProgressMonitor().done();
	}	
}