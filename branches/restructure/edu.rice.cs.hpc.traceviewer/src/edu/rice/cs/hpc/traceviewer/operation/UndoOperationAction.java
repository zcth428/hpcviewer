package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;

public class UndoOperationAction extends OperationHistoryAction {

	public UndoOperationAction(ImageDescriptor img) {
		super(img);
	}

	@Override
	protected IUndoableOperation[] getHistory() {
		return TraceOperation.getUndoHistory();
	}

	@Override
	protected void execute() {
		try {
			IStatus status = TraceOperation.getOperationHistory().
					undo(TraceOperation.undoableContext, null, null);
			if (!status.isOK()) {
				System.err.println("Cannot undo: " + status.getMessage());
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void execute(IUndoableOperation operation) {
		try {
			TraceOperation.getOperationHistory().undoOperation(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
