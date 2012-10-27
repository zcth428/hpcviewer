package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

/********************************************
 * 
 * zoom operation
 *
 ********************************************/
public class ZoomOperation extends TraceOperation {
	
	static final public String ActionHome = "Home";
	
	ITraceAction action;
	
	public ZoomOperation(String label, Frame frame, ITraceAction action) {
		super(label, frame);
		this.action = action;
		addContext(undoableContext);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException 
			{
		if (action != null)
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
		if (action != null)
			action.doAction(frame);
		return Status.OK_STATUS;
	}

}
