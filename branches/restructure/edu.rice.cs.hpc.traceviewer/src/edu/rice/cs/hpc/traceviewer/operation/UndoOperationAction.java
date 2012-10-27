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

	@Override
	protected void setStatus() {
		final IUndoableOperation []ops = getHistory(); 
		boolean status = (ops.length>0);
/*		if (ops.length==1) {
			status = !(ops[0].getLabel().equals(ZoomOperation.ActionHome));
		}
		if (ops.length > 0)
			System.out.println("UOA " + status + "\tl: " + ops.length +" \t[0]="+ops[0]);*/
		setEnabled(status);
	}

	@Override
	protected boolean canAct(IUndoableOperation operation, int index,
			int indexEnd) 
	{
		boolean can = true;
/*		if (index == 0) {
			// the first "home" operation is not undoable. It's the init
			if (operation instanceof ZoomOperation) {
				final String label = ((ZoomOperation)operation).getLabel();
				can = !(label.equals(ZoomOperation.ActionHome));
			}
		}*/
		return can;
	}
}
