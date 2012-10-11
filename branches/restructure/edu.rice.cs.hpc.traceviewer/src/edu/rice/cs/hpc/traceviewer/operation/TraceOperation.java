package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

public abstract class TraceOperation extends AbstractOperation {
	
	final static public IUndoContext context = new TraceOperationContext();
	static public enum OperationType {SpaceTime, DepthTime, Callstack, Mini};

	protected OperationItem item;
	
	public TraceOperation(String label) {
		super(label);
		item = new OperationItem();
		addContext(context);
	}
	
	public TraceOperation(String label, OperationType source) {
		this(label);
		item.source = source;
	}

	public TraceOperation(String label, Frame frame, OperationType source) {
		super(label);
		item = new OperationItem();
		item.frame = frame;
		item.source = source;
	}

	public OperationType getType() {
		return item.source;
	}
	
	public Frame getFrame() {
		return item.frame;
	}
	
	static public IOperationHistory getOperationHistory() {
		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}
	
	protected class OperationItem 
	{
		Frame frame;
		OperationType source;
	}
	
}
