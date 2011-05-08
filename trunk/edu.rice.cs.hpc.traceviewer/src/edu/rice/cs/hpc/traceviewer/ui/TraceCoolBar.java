package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class TraceCoolBar extends Composite {

	final ToolItem home;
	final ToolItem tZoomIn;
	final ToolItem tZoomOut;
	final ToolItem pZoomIn;
	final ToolItem pZoomOut;
	final ToolItem undo;
	final ToolItem redo;
	final ToolItem save;
	final ToolItem open;
	final ToolItem goEast;
	final ToolItem goWest;
	final ToolItem goNorth;
	final ToolItem goSouth;
	
	public TraceCoolBar(Composite parent, final ITraceViewAction action, int style) {
		super(parent, style);

		final CoolBar coolBar = new CoolBar(this, SWT.NONE);

		final ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT);
		
		/***************************************************
		 * Buttons
		 **************************************************/

		
		home = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor homeSamp = ImageDescriptor.createFromFile(this.getClass(), "home-screen.png");
		Image homeScreen = homeSamp.createImage();
		home.setImage(homeScreen);
		home.setToolTipText("Reset the view to display the whole trace");
		home.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.home();
			}
		});
		home.setEnabled(true);
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		tZoomIn = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomInTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-time.png");
		Image zoomInTime = zoomInTim.createImage();
		tZoomIn.setImage(zoomInTime);
		tZoomIn.setToolTipText("Zoom in along the time axis");
		tZoomIn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.timeZoomIn();
			}
		});
		tZoomIn.setEnabled(true);

		tZoomOut = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomOutTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-time.png");
		Image zoomOutTime = zoomOutTim.createImage();
		tZoomOut.setImage(zoomOutTime);
		tZoomOut.setToolTipText("Zoom out along the time axis");
		tZoomOut.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.timeZoomOut();
			}
		});
		tZoomOut.setEnabled(false);

		pZoomIn = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomInProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-process.png");
		Image zoomInProcess = zoomInProc.createImage();
		pZoomIn.setImage(zoomInProcess);
		pZoomIn.setToolTipText("Zoom in along the process axis");
		pZoomIn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.processZoomIn();
			}
		});
		pZoomIn.setEnabled(true);

		pZoomOut = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomOutProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-process.png");
		Image zoomOutProcess = zoomOutProc.createImage();
		pZoomOut.setImage(zoomOutProcess);
		pZoomOut.setToolTipText("Zoom out along the process axis");
		pZoomOut.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.processZoomOut();
			}
		});
		pZoomOut.setEnabled(false);
				
		new ToolItem(toolBar, SWT.SEPARATOR);


		goEast = new ToolItem(toolBar, SWT.PUSH);
		final ImageDescriptor eastDesc = ImageDescriptor.createFromFile(getClass(), "go-east.png");
		goEast.setImage(eastDesc.createImage());
		goEast.setToolTipText("Scroll left one step along the time axis");
		goEast.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				action.goEast();
			}
			
		});
		
		goWest = new ToolItem(toolBar, SWT.PUSH);
		final ImageDescriptor westDesc = ImageDescriptor.createFromFile(getClass(), "go-west.png");
		goWest.setImage(westDesc.createImage());
		goWest.setToolTipText("Scroll right one step along the time axis");
		goWest.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				action.goWest();
			}
			
		});
		
		goNorth = new ToolItem(toolBar, SWT.PUSH);
		final ImageDescriptor northDesc = ImageDescriptor.createFromFile(getClass(), "go-north.png");
		goNorth.setImage(northDesc.createImage());
		goNorth.setToolTipText("Scroll up one step along the process axis");
		goNorth.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				action.goNorth();
			}
			
		});
		
		goSouth = new ToolItem(toolBar, SWT.PUSH);
		final ImageDescriptor southDesc = ImageDescriptor.createFromFile(getClass(), "go-south.png");
		goSouth.setImage(southDesc.createImage());
		goSouth.setToolTipText("Scroll down one step along the process axis");
		goSouth.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				action.goSouth();
			}
			
		});
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		undo = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor undoSamp = ImageDescriptor.createFromFile(this.getClass(), "undo.png");
		Image undoArrow = undoSamp.createImage();
		undo.setImage(undoArrow);
		undo.setToolTipText("Undo the last action");
		undo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.undo();
			}
		});
		undo.setEnabled(false);
		
		redo = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor redoSamp = ImageDescriptor.createFromFile(this.getClass(), "redo.png");
		Image redoArrow = redoSamp.createImage();
		redo.setImage(redoArrow);
		redo.setToolTipText("Redo the last undo");
		redo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.redo();
			}
		});
		redo.setEnabled(false);
				
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		save = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor saveSamp = ImageDescriptor.createFromFile(this.getClass(), "save.png");
		Image saveImage = saveSamp.createImage();
		save.setImage(saveImage);
		save.setToolTipText("Save the current view configuration to a file");
		save.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.save();
			}
		});
		save.setEnabled(true);

		open = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor openSamp = ImageDescriptor.createFromFile(this.getClass(), "open.png");
		Image openImage = openSamp.createImage();
		open.setImage(openImage);
		open.setToolTipText("Open a saved view configuration");
		open.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				action.open();
			}
		});
		open.setEnabled(true);

		
		GridLayoutFactory.fillDefaults().extendedMargins(5, 0, 0, 0).numColumns(1).generateLayout(coolBar);
		GridDataFactory.fillDefaults().applyTo(coolBar);
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		toolBar.pack();
		Point size = toolBar.getSize();
		
		final CoolItem buttonItems = new CoolItem(coolBar, SWT.NONE);
		buttonItems.setControl(toolBar);
		Point preferred = buttonItems.computeSize(size.x, size.y);
		buttonItems.setPreferredSize(preferred);

	}
}
