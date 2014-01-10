package edu.rice.cs.hpc.traceviewer.operation;

import org.eclipse.core.commands.ExecutionException;

import edu.rice.cs.hpc.traceviewer.ui.Frame;

public class TraceOperationTest {

	public TraceOperationTest() {

		UndoOperationAction undo = new UndoOperationAction(null);
		RedoOperationAction redo = new RedoOperationAction(null);
		
		Frame frame = new Frame(0, 0, 0, 0, 0, 0, 0);
		TraceAction action = new TraceAction();
		
		ZoomOperation zoom = new ZoomOperation("zoom1", frame, action);
		try {
			zoom.execute(null, null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		undo.execute();
		redo.execute();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TraceOperationTest test = new TraceOperationTest();
		
	}

	private class TraceAction implements ITraceAction 
	{
		@Override
		public void doAction(Frame frame) {
			System.out.println("action " + frame);
		}		
	}
}
