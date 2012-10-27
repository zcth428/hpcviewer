package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;

public class RedoOperationAction extends OperationHistoryAction {

	public RedoOperationAction(ImageDescriptor img) {
		super(img);
	}


	@Override
	protected IUndoableOperation[] getHistory() {
		return TraceOperation.getRedoHistory();
	}

	@Override
	protected void execute() {
		try {
			IStatus status = TraceOperation.getOperationHistory().
					redo(TraceOperation.undoableContext, null, null);
			if (!status.isOK()) {
				System.err.println("Cannot redo: " + status.getMessage());
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void execute(IUndoableOperation operation) {
		try {
			TraceOperation.getOperationHistory().redoOperation(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void setStatus() {
		final IUndoableOperation []ops = getHistory(); 
		setEnabled(ops.length>0);
	}


	@Override
	protected boolean canAct(IUndoableOperation operation, int index,
			int indexEnd) {
		return true;
	}

}
