package edu.rice.cs.hpc.traceviewer.ui;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/**A view for displaying the traceviewer.*/
//all the GUI setup for the detail view is here
public class HPCTraceView extends ViewPart
{
	
	/**The ID needed to create this view (used in plugin.xml).*/
	public static final String ID = "hpctraceview.view";
	
	/** Stores/Creates all of the data that is used in the view.*/
	private SpaceTimeData stData = null;
	
	/** Paints and displays the detail view.*/
	SpaceTimeDetailCanvas detailCanvas;
	
	/** Determines whether this view has been setup.*/
	private boolean initialized = false;
	
	/** Stores the current depth that is being displayed.
	 *  WARNING: this variable is accessible by other classes ! */
	int currentDepth;
	
	private HPCCallStackView csview;
	
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

		currentDepth = 0;

		/*************************************************************************
		 * Master Composite
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		/***************************************************
		 * Buttons
		 **************************************************/
		Group buttonGroup = new Group(master, SWT.EMBEDDED);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 9;
		buttonLayout.makeColumnsEqualWidth = true;
		
		// tighten layout -- johnmc
		buttonLayout.marginHeight = 0;
		buttonLayout.verticalSpacing = 0;
		buttonLayout.marginWidth = 0;
		buttonLayout.verticalSpacing = 0;
		buttonLayout.horizontalSpacing = 0;
	
		buttonGroup.setLayout(buttonLayout);
		buttonGroup.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		
		Button home = new Button(buttonGroup, SWT.PUSH);
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
		
		
		Button tZoomIn = new Button(buttonGroup, SWT.PUSH);
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

		Button tZoomOut = new Button(buttonGroup, SWT.PUSH);
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

		Button pZoomIn = new Button(buttonGroup, SWT.PUSH);
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

		Button pZoomOut = new Button(buttonGroup, SWT.PUSH);
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
		

		Button undo = new Button(buttonGroup, SWT.PUSH);
		ImageDescriptor undoSamp = ImageDescriptor.createFromFile(this.getClass(), "undo.png");
		Image undoArrow = undoSamp.createImage();
		undo.setImage(undoArrow);
		undo.setToolTipText("Undo the last made action");
		undo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.popUndo();
				if (detailCanvas.getDepth() != currentDepth)
				{
					csview.depthEditor.setSelection(detailCanvas.getDepth());
					detailCanvas.popUndo();
				}
			}
		});
		undo.setEnabled(false);
		
		Button redo = new Button(buttonGroup, SWT.PUSH);
		ImageDescriptor redoSamp = ImageDescriptor.createFromFile(this.getClass(), "redo.png");
		Image redoArrow = redoSamp.createImage();
		redo.setImage(redoArrow);
		redo.setToolTipText("Redo the last undo");
		redo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				detailCanvas.popRedo();
				if (detailCanvas.getDepth() != currentDepth)
				{
					csview.depthEditor.setSelection(detailCanvas.getDepth());
					detailCanvas.popRedo();
				}
			}
		});
		redo.setEnabled(false);
		
	
		
		Button save = new Button(buttonGroup, SWT.PUSH);
		ImageDescriptor saveSamp = ImageDescriptor.createFromFile(this.getClass(), "save.png");
		Image saveImage = saveSamp.createImage();
		save.setImage(saveImage);
		save.setToolTipText("Save the view configuration to a file");
		save.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				FileDialog saveDialog;
				saveDialog = new FileDialog(master.getShell(), SWT.SAVE);
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
							MessageBox msg = new MessageBox(master.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
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
		
		

		Button open = new Button(buttonGroup, SWT.PUSH);
		ImageDescriptor openSamp = ImageDescriptor.createFromFile(this.getClass(), "open.png");
		Image openImage = openSamp.createImage();
		open.setImage(openImage);
		open.setToolTipText("Open a saved view configuration");
		open.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				FileDialog openDialog;
				openDialog = new FileDialog(master.getShell(), SWT.OPEN);
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
		
		/*************************************************************************
		 * Detail View Canvas
		 ************************************************************************/
		
		detailCanvas = new SpaceTimeDetailCanvas(master); //(master, stData);
		detailCanvas.setDepth(currentDepth);
		detailCanvas.setLayout(new GridLayout());
		detailCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailCanvas.setButtons(new Button[]{home,open,save,undo,redo,tZoomIn,tZoomOut,pZoomIn,pZoomOut});
		
		/**************************************************************************
         * Process and Time dimension labels
         *************************************************************************/
		Composite labelGroup = new Composite(master, SWT.EMBEDDED);
        GridLayout labelGroupLayout = new GridLayout();
        labelGroupLayout.numColumns = 3;
        
		// tighten layout -- johnmc
        labelGroupLayout.marginHeight = 1;
        labelGroupLayout.verticalSpacing = 0;
        labelGroupLayout.verticalSpacing = 0;
	
        labelGroup.setLayout(labelGroupLayout);
        labelGroup.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

        //detailCanvas takes care of updating the labels
        detailCanvas.setLabels(labelGroup);    
	}

	
	public void updateData(SpaceTimeData _stData) {
		this.stData = _stData;
		this.detailCanvas.updateData(_stData);
	}
	
	
	/*************************************************************************
	 *	Updates/sets the depth that is displayed in the context view and 
	 *	detail view.
	 ************************************************************************/
	public void setDepth(int depth, boolean textBox)
	{
		currentDepth = depth;
		detailCanvas.setDepth(depth);
		csview.csViewer.setDepth(depth);
		this.stData.getDepthTimeCanvas().setDepth(depth);
		if(textBox)
			csview.depthEditor.setSelection(depth);
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
	
	public int getSelectedProcess()
	{
		return detailCanvas.selectedProcess;
	}
	
	public void setCSView(HPCCallStackView _csview)
	{
		csview = _csview;
		detailCanvas.csViewer = csview.csViewer;
		initialized = true;
	}
}