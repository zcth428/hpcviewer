package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

public class ZoomOperation extends TraceOperation {
	IZoomAction action;
	
	public ZoomOperation(String label, IZoomAction action, 
			long t1, long t2, int p1, int p2) {

		super(label);
		frame = new Frame(t1, t2, p1, p2);
		this.action = action;
	}
	
	public ZoomOperation(String label, IZoomAction action, Frame frame) {
		super(label, frame);
		this.action = action;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException 
			{
		action.doAction(frame);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

}
