package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;

import edu.rice.cs.hpc.traceviewer.operation.RedoOperationAction;
import edu.rice.cs.hpc.traceviewer.operation.UndoOperationAction;

public class TraceCoolBar {

	final Action home;
	final Action tZoomIn;
	final Action tZoomOut;
	final Action pZoomIn;
	final Action pZoomOut;
	final Action save;
	final Action open;
	final Action goEast;
	final Action goWest;
	final Action goNorth;
	final Action goSouth;
	
	
	public TraceCoolBar(final IToolBarManager toolbar, final ITraceViewAction action, int style) {
		
		/***************************************************
		 * Buttons
		 **************************************************/

		
		final ImageDescriptor homeSamp = ImageDescriptor.createFromFile(this.getClass(), "home-screen.png");
		home = new Action(null, homeSamp) {
			public void run() {
				action.home();
			}
		};
		this.createAction(toolbar, home, "Reset the view to display the whole trace");
		
		toolbar.add(new Separator());
		
		final ImageDescriptor zoomInTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-time.png");
		tZoomIn = new Action(null, zoomInTim) {
			public void run() {
				action.timeZoomIn();
			}		
		};
		this.createAction(toolbar, tZoomIn, "Zoom in along the time axis");
		
		final ImageDescriptor zoomOutTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-time.png");
		tZoomOut = new Action(null, zoomOutTim) {
			public void run() {
				action.timeZoomOut();
			}		
		};
		this.createAction(toolbar, tZoomOut, "Zoom out along the time axis");
		
		ImageDescriptor zoomInProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-process.png");
		pZoomIn = new Action(null, zoomInProc) {
			public void run() {
				action.processZoomIn();
			}		
		};
		this.createAction(toolbar, pZoomIn, "Zoom in along the process axis");
		
		ImageDescriptor zoomOutProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-process.png");
		pZoomOut = new Action(null, zoomOutProc) {
			public void run() {
				action.processZoomOut();
			}		
		};
		this.createAction(toolbar, pZoomOut, "Zoom out along the process axis");


		toolbar.add(new Separator());

		
		final ImageDescriptor eastDesc = ImageDescriptor.createFromFile(getClass(), "go-east.png");
		goEast = new Action(null, eastDesc) {
			public void run() {
				action.goEast();
			}		
		};
		this.createAction(toolbar, goEast, "Scroll left one step along the time axis");

		final ImageDescriptor westDesc = ImageDescriptor.createFromFile(getClass(), "go-west.png");
		goWest = new Action(null, westDesc) {
			public void run() {
				action.goWest();
			}		
		};
		this.createAction(toolbar, goWest, "Scroll right one step along the time axis");
		

		final ImageDescriptor northDesc = ImageDescriptor.createFromFile(getClass(), "go-north.png");
		goNorth = new Action(null, northDesc) {
			public void run() {
				action.goNorth();
			}		
		};
		this.createAction(toolbar, goNorth, "Scroll up one step along the process axis");

		
		final ImageDescriptor southDesc = ImageDescriptor.createFromFile(getClass(), "go-south.png");
		goSouth = new Action(null, southDesc) {
			public void run() {
				action.goSouth();
			}		
		};
		this.createAction(toolbar, goSouth, "Scroll down one step along the process axis");
		
		toolbar.add(new Separator());
		
		ImageDescriptor undoSamp = ImageDescriptor.createFromFile(this.getClass(), "undo.png");
		UndoOperationAction undo = new UndoOperationAction(undoSamp);
		this.createAction(toolbar, undo, "Undo the last action");
				
		ImageDescriptor redoSamp = ImageDescriptor.createFromFile(this.getClass(), "redo.png");
		RedoOperationAction redo = new RedoOperationAction(redoSamp);
		this.createAction(toolbar, redo, "Redo the last undo");
				
		toolbar.add(new Separator());
		
		ImageDescriptor saveSamp = ImageDescriptor.createFromFile(this.getClass(), "save.png");
		save = new Action(null, saveSamp) {
			public void run() {
				action.save();
			}		
		};
		this.createAction(toolbar, save, "Save the current view configuration to a file");
				
		ImageDescriptor openSamp = ImageDescriptor.createFromFile(this.getClass(), "open.png");
		open = new Action(null, openSamp) {
			public void run() {
				action.open();
			}		
		};
		this.createAction(toolbar, open, "Open a saved view configuration");
	}

	/*****
	 * Finalize an action by setting the tooltop and insert it into the toolbar
	 * 
	 * @param toolbar
	 * @param action
	 * @param sDesc
	 */
	private void createAction(IToolBarManager toolbar, Action action, String sDesc) {
		action.setToolTipText(sDesc);
		action.setEnabled(false);
		toolbar.add(action);
	}
}
