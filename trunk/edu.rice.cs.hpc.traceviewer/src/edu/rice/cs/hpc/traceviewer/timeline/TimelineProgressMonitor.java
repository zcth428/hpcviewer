package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;

public class TimelineProgressMonitor {

	private AtomicInteger progress;
	private IStatusLineManager statusMgr;
	private IProgressMonitor monitor;
	

	public TimelineProgressMonitor(IStatusLineManager _statusMgr)
	{
		statusMgr = _statusMgr;
		monitor = statusMgr.getProgressMonitor();
		progress = new AtomicInteger();
	}
	
	public void beginProgress(int totalWork, String sMessage, String sTask)
	{
		progress.set(0);
		statusMgr.setMessage(sMessage);
		// shell.update();
		monitor.beginTask(sTask, totalWork);
	}
	
	public void announceProgress()
	{
		progress.getAndIncrement();
	}
	
	public void reportProgress()
	{
		int workDone = progress.getAndSet(0);
		if (workDone > 0)
			monitor.worked(workDone);
	}
	
	public void endProgress()
	{
		monitor.done();
		statusMgr.setMessage(null);
		// shell.update();
	}

}
