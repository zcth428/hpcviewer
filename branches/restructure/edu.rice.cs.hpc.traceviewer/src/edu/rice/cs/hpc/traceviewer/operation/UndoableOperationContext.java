package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.operations.IUndoContext;

public class UndoableOperationContext implements IUndoContext {

	private final static String label = "PositionOperationContext";

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean matches(IUndoContext context) {
		return context.getLabel() == label;
	}

}
