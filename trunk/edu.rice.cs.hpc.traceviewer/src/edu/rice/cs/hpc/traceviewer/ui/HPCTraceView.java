package edu.rice.cs.hpc.traceviewer.ui;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.events.ITraceDepth;
import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/**A view for displaying the traceviewer.*/
//all the GUI setup for the detail view is here
public class HPCTraceView extends ViewPart implements ITraceDepth, ITracePosition
{
	
	/**The ID needed to create this view (used in plugin.xml).*/
	public static final String ID = "hpctraceview.view";
	
	/** Stores/Creates all of the data that is used in the view.*/
	private SpaceTimeData stData = null;
	
	/** Paints and displays the detail view.*/
	SpaceTimeDetailCanvas detailCanvas;
	
	/** Determines whether this view has been setup.*/
	private boolean initialized = false;
	
	private HPCCallStackView csview;
	
	private Composite coolBarArea;
	
	/*************************************************************************
	 *	Creates the view.
	 ************************************************************************/
	//TO THE NEXT SCHLIMAZEL THAT WORKS ON THIS - THIS METHOD IS WHERE EVERYTHING STARTS
	public void createPartControl(Composite master)
	{
		setupEverything(master);
	}
	
	/**************************************************************************
	 * Sets up everything that is to be displayed on the view.
	 *************************************************************************/
	public void setupEverything(final Composite master)
	{

		/*************************************************************************
		 * Master Composite
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.createToolbar(master);

        GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(detailCanvas);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detailCanvas);

		detailCanvas.setDepth(0);
	}

	/**
	 * update new data
	 */
	public void updateData(SpaceTimeData _stData) {
		this.stData = _stData;
		this.detailCanvas.updateData(_stData);
		
		this.stData.addDepthListener(this);
		this.stData.addPositionListener(this);
		detailCanvas.setVisible(true);
		coolBarArea.setVisible(true);
	}
	
	
	/*************************************************************************
	 *	Updates/sets the depth that is displayed in the context view and 
	 *	detail view.
	 ************************************************************************/
	public void setDepth(int depth)
	{
		detailCanvas.setDepth(depth);
	}

	/**Required in order to extend ViewPart.*/
	public void setFocus()
	{
		if (initialized)
			detailCanvas.setCSSample();
	}
	
	public SpaceTimeData getData()
	{
		return stData;
	}
	
	public void setCSView(HPCCallStackView _csview)
	{
		csview = _csview;
		detailCanvas.csViewer = csview.csViewer;
		initialized = true;
	}

	public void setPosition(Position position) {
		this.detailCanvas.setCrossHair(position.time, position.process);
	}
	
	
	private void createToolbar(Composite parent) {
		coolBarArea = new Composite(parent, SWT.NONE);
		final CoolBar coolBar = new CoolBar(coolBarArea, SWT.NONE);
		final ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT);
		
		/***************************************************
		 * Buttons
		 **************************************************/

		final ToolItem home = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor homeSamp = ImageDescriptor.createFromFile(this.getClass(), "home-screen.png");
		Image homeScreen = homeSamp.createImage();
		home.setImage(homeScreen);
		home.setToolTipText("Resets the view to the home screen");
		home.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.home();
			}
		});
		home.setEnabled(true);
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem tZoomIn = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomInTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-time.png");
		Image zoomInTime = zoomInTim.createImage();
		tZoomIn.setImage(zoomInTime);
		tZoomIn.setToolTipText("Zoom in on the time axis");
		tZoomIn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.timeZoomIn();
			}
		});
		tZoomIn.setEnabled(true);

		final ToolItem tZoomOut = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomOutTim = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-time.png");
		Image zoomOutTime = zoomOutTim.createImage();
		tZoomOut.setImage(zoomOutTime);
		tZoomOut.setToolTipText("Zoom out on the time axis");
		tZoomOut.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.timeZoomOut();
			}
		});
		tZoomOut.setEnabled(false);

		final ToolItem pZoomIn = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomInProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-in-process.png");
		Image zoomInProcess = zoomInProc.createImage();
		pZoomIn.setImage(zoomInProcess);
		pZoomIn.setToolTipText("Zoom in on the process axis");
		pZoomIn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.processZoomIn();
			}
		});
		pZoomIn.setEnabled(true);

		final ToolItem pZoomOut = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor zoomOutProc = ImageDescriptor.createFromFile(this.getClass(), "zoom-out-process.png");
		Image zoomOutProcess = zoomOutProc.createImage();
		pZoomOut.setImage(zoomOutProcess);
		pZoomOut.setToolTipText("Zoom out on the process axis");
		pZoomOut.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.processZoomOut();
			}
		});
		pZoomOut.setEnabled(false);
				
		new ToolItem(toolBar, SWT.SEPARATOR);

		final ToolItem undo = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor undoSamp = ImageDescriptor.createFromFile(this.getClass(), "undo.png");
		Image undoArrow = undoSamp.createImage();
		undo.setImage(undoArrow);
		undo.setToolTipText("Undo the last made action");
		undo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.popUndo();
				if (detailCanvas.getDepth() != stData.getDepth())
				{
					csview.depthEditor.setSelection(detailCanvas.getDepth());
					detailCanvas.popUndo();
				}
			}
		});
		undo.setEnabled(false);
		
		final ToolItem redo = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor redoSamp = ImageDescriptor.createFromFile(this.getClass(), "redo.png");
		Image redoArrow = redoSamp.createImage();
		redo.setImage(redoArrow);
		redo.setToolTipText("Redo the last undo");
		redo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.popRedo();
				if (detailCanvas.getDepth() != stData.getDepth())
				{
					csview.depthEditor.setSelection(detailCanvas.getDepth());
					detailCanvas.popRedo();
				}
			}
		});
		redo.setEnabled(false);
				
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem save = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor saveSamp = ImageDescriptor.createFromFile(this.getClass(), "save.png");
		Image saveImage = saveSamp.createImage();
		save.setImage(saveImage);
		save.setToolTipText("Save the view configuration to a file");
		save.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				FileDialog saveDialog;
				saveDialog = new FileDialog(coolBar.getShell(), SWT.SAVE);
				saveDialog.setText("Save View Configuration");
				String fileName = "";
				boolean validSaveFileFound = false;
				while(!validSaveFileFound)
				{
					Frame toSave = detailCanvas.save();
					saveDialog.setFileName((int)toSave.begTime+"-"+(int)toSave.endTime+", "
						+(int)toSave.begProcess+"-"+(int)toSave.endProcess+".bin");
					fileName = saveDialog.open();
					
					if (fileName == null)
						return;
					else
					{
						if (!new File(fileName).exists())
							validSaveFileFound = true;
						else
						{
							MessageBox msg = new MessageBox(coolBar.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
							msg.setText("File Exists");
							msg.setMessage("This file path already exists.\nDo you want to overwrite this save file?");
							int selectionChoice = msg.open();
							if (selectionChoice==SWT.YES)
								validSaveFileFound = true;
							else
								validSaveFileFound = false;
							//open message box confirming whether or not they want to overwrite saved file
							//if they select yes, validSaveFileFound = true;
							//if they selct no, validSaveFileFound = false;
						}
					}
				}
				
				try
				{
					ObjectOutputStream out = null;
					try
					{
						out = new ObjectOutputStream(new FileOutputStream(fileName));
						out.writeObject(detailCanvas.save());
					}
					finally
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		save.setEnabled(true);

		final ToolItem open = new ToolItem(toolBar, SWT.PUSH);
		ImageDescriptor openSamp = ImageDescriptor.createFromFile(this.getClass(), "open.png");
		Image openImage = openSamp.createImage();
		open.setImage(openImage);
		open.setToolTipText("Open a saved view configuration");
		open.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				FileDialog openDialog;
				openDialog = new FileDialog(coolBar.getShell(), SWT.OPEN);
				openDialog.setText("Open View Configuration");
				String fileName = "";
				boolean validFrameFound = false;
				while(!validFrameFound)
				{
					fileName = openDialog.open();
					
					if (fileName == null) return;
					File binFile = new File(fileName);
					
					if (binFile.exists())
					{
						try
						{
							ObjectInputStream in = null;
							try
							{
								in = new ObjectInputStream(new FileInputStream(fileName));
								Frame current = (Frame)in.readObject();
								detailCanvas.open(current);
								validFrameFound = true;
							}
							finally
							{
								in.close();
							}
						}
						catch (IOException e)
						{
							validFrameFound = false;
						}
						catch (ClassNotFoundException e)
						{
							validFrameFound = false;
						}
					}
				}
			}
		});
		open.setEnabled(true);
		
		final ToolItem goEast = new ToolItem(toolBar, SWT.PUSH);
		goEast.setText("<-");
		goEast.setToolTipText("Go to the left");
		goEast.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				detailCanvas.goEast();
			}
			
		});
		
		final ToolItem goWest = new ToolItem(toolBar, SWT.PUSH);
		goWest.setText("->");
		goWest.setToolTipText("Go to the right");
		goWest.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				detailCanvas.goWest();
			}
			
		});
		
		final ToolItem goNorth = new ToolItem(toolBar, SWT.PUSH);
		goNorth.setText("/\\");
		goNorth.setToolTipText("Go up");
		goNorth.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				detailCanvas.goNorth();
			}
			
		});
		
		final ToolItem goSouth = new ToolItem(toolBar, SWT.PUSH);
		goSouth.setText("\\/");
		goSouth.setToolTipText("Go down");
		goSouth.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event) {
				detailCanvas.goSouth();
			}
			
		});
		
		GridLayoutFactory.fillDefaults().extendedMargins(5, 0, 0, 0).numColumns(1).generateLayout(coolBar);
		GridDataFactory.fillDefaults().applyTo(coolBar);
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		toolBar.pack();
		Point size = toolBar.getSize();
		
		final CoolItem buttonItems = new CoolItem(coolBar, SWT.NONE);
		buttonItems.setControl(toolBar);
		Point preferred = buttonItems.computeSize(size.x, size.y);
		buttonItems.setPreferredSize(preferred);
		
		/**************************************************************************
         * Process and Time dimension labels
         *************************************************************************/
		Composite labelGroup = new Composite(coolBarArea, SWT.NONE);
		
		/*************************************************************************
		 * Detail View Canvas
		 ************************************************************************/
		
		detailCanvas = new SpaceTimeDetailCanvas(parent); //(master, stData);
		
		detailCanvas.setLabels(labelGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(labelGroup);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(labelGroup);
		
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(coolBarArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(coolBarArea);

		detailCanvas.setButtons(new ToolItem[]{home,open,save,undo,redo,tZoomIn,tZoomOut,pZoomIn,pZoomOut});
		
		detailCanvas.setVisible(false);
		coolBarArea.setVisible(false);
	}
}