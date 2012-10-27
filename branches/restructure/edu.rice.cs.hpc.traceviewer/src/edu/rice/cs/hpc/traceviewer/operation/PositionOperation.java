package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.ui.Frame;

/**********************************
 * 
 * Operation for changes of cursor position
 * @see getPosition 
 * 	method to retrieve the current position
 **********************************/
public class PositionOperation extends TraceOperation 
{
	final private ITraceAction action;
	
	public PositionOperation(Position position, ITraceAction action) {
		super("Set cursor: " + position, new Frame(0, 0, 0, 0, 0, position.time, position.process));
		this.action = action;
		// hack frame position: by default, frame will adjust the position to be within the range
		// however, for position operation, we don't care with ROI and we don't have this information
		this.frame.position = position;
	}
	
	public Position getPosition() {
		return frame.position;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (action != null) {
			action.doAction(frame);
		}
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
		return execute(monitor, info);
	}

}
