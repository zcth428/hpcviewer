package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class RedoOperationAction extends OperationHistoryAction {

	public RedoOperationAction(ImageDescriptor img) {
		super(img);
	}


	@Override
	protected IUndoableOperation[] getHistory() {
		return TraceOperation.getRedoHistory();
	}

	@Override
	public Menu getMenu(Control parent) {
		Menu menu = getMenu();
		if (menu != null) 
			menu.dispose();
		
		menu = new Menu(parent);
		
		IUndoableOperation[] operations = getHistory();
		
		// create a list of menus of undoable operations
		for (int i=operations.length-1; i>=0; i--) {
			final IUndoableOperation op = operations[i];
			Action action = new Action(op.getLabel()) {
				public void run() {
					execute(op);
				}
			};
			addActionToMenu(menu, action);
		} 
		return menu;
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
}
