package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

/********************************************
 * 
 * generic trace operation
 *
 ********************************************/
public abstract class TraceOperation extends AbstractOperation {
	
	final static public IUndoContext context = new TraceOperationContext();

	protected Frame frame;
	
	public TraceOperation(String label) {
		this(label,null);
	}
	
	public TraceOperation(String label, Frame frame) {
		super(label);
		addContext(context);
		this.frame = frame;
	}

	public Frame getFrame() {
		return frame;
	}
	
	static public IOperationHistory getOperationHistory() {
		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}
	
	public static IUndoableOperation[] getUndoHistory()
	{
		return getOperationHistory().getUndoHistory(context);
	}
	
	public static IUndoableOperation[] getRedoHistory()
	{
		return getOperationHistory().getRedoHistory(context);
	}
}
