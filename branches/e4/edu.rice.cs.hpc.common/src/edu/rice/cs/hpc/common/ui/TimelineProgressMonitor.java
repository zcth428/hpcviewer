package edu.rice.cs.hpc.common.ui;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TimelineProgressMonitor {

	private AtomicInteger progress;
	private IStatusLineManager statusMgr;
	private IProgressMonitor monitor;
	final private Display display;

	public TimelineProgressMonitor(IStatusLineManager _statusMgr, Display display)
	{
		this.display = display;

		if (_statusMgr == null)
			return;
		
		statusMgr = _statusMgr;
		monitor = statusMgr.getProgressMonitor();
		progress = new AtomicInteger();
	}
	
	public void beginProgress(final int totalWork, final String sMessage, final String sTask, Shell shell)
	{
		if (statusMgr == null)
			return;

		progress.set(0);
		
		display.asyncExec(new Runnable() {
			public void run() {
				
				monitor.beginTask(sTask, totalWork);
				statusMgr.setMessage(sMessage);
			}
		});
		
		// quick fix to force UI to show the message.
		// we need a smarter way to do this. If the work is small, no need to refresh UI
		//shell.update();
	}
	
	public void announceProgress()
	{
		if (statusMgr == null)
			return;
		progress.getAndIncrement();
	}
	
	public void reportProgress()
	{
		if (statusMgr == null)
			return;

		final int workDone = progress.getAndSet(0);
		if (workDone > 0) {
			display.asyncExec(new Runnable() {
				public void run() {
					monitor.worked(workDone);
				}
			});
		}
	}
	
	public void endProgress()
	{
		if (statusMgr == null)
			return;

		display.asyncExec(new Runnable() {
			public void run() {
				monitor.done();
				statusMgr.setMessage(null);
			}
		});
		// shell.update();
	}

	private class UIRunnable implements Runnable
	{
		public void run() {
			
		}
	}
}
