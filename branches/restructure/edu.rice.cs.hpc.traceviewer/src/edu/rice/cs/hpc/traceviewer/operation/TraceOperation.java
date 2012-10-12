package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

public abstract class TraceOperation extends AbstractOperation {
	
	final static public IUndoContext context = new TraceOperationContext();
	static public enum OperationType {SpaceTime, DepthTime, Callstack, Mini};

	protected Frame frame;
	
	public TraceOperation(String label) {
		super(label);
		addContext(context);
	}
	
	public TraceOperation(String label, Frame frame) {
		super(label);
		this.frame = frame;
	}

	public Frame getFrame() {
		return frame;
	}
	
	static public IOperationHistory getOperationHistory() {
		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}	
}
