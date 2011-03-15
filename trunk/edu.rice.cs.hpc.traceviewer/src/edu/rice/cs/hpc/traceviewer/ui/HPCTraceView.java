package edu.rice.cs.hpc.traceviewer.ui;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.events.ITraceDepth;
import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/**A view for displaying the traceviewer.*/
//all the GUI setup for the detail view is here
public class HPCTraceView extends ViewPart implements ITraceDepth, ITracePosition, ITraceViewAction
{
	
	/**The ID needed to create this view (used in plugin.xml).*/
	public static final String ID = "hpctraceview.view";
	
	/** Stores/Creates all of the data that is used in the view.*/
	private SpaceTimeData stData = null;
	
	/** Paints and displays the detail view.*/
	SpaceTimeDetailCanvas detailCanvas;
	
	private HPCCallStackView csview;
	
	private TraceCoolBar coolBarArea;
	
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

		//detailCanvas.setDepth(0);
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
		this.detailCanvas.setFocus();
	}
	
	public SpaceTimeData getData()
	{
		return stData;
	}
	
	public void setCSView(HPCCallStackView _csview)
	{
		csview = _csview;
		detailCanvas.csViewer = csview.csViewer;
	}

	public void setPosition(Position position) {
		this.detailCanvas.setCrossHair(position.time, position.process);
	}
	
	
	private void createToolbar(Composite parent) {
		coolBarArea = new TraceCoolBar(parent, this, SWT.NONE);

		/**************************************************************************
         * Process and Time dimension labels
         *************************************************************************/
		final Composite labelGroup = new Composite(coolBarArea, SWT.NONE);
				
		/*************************************************************************
		 * Detail View Canvas
		 ************************************************************************/
		
		detailCanvas = new SpaceTimeDetailCanvas(parent); //(master, stData);
		
		detailCanvas.setLabels(labelGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(labelGroup);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(labelGroup);
		
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(coolBarArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(coolBarArea);

		detailCanvas.setButtons(new ToolItem[]{coolBarArea.home, coolBarArea.open, coolBarArea.save, coolBarArea.undo,
				coolBarArea.redo, coolBarArea.tZoomIn, coolBarArea.tZoomOut, coolBarArea.pZoomIn, coolBarArea.pZoomOut});
		
		detailCanvas.setVisible(false);
		coolBarArea.setVisible(false);
	}


	//----------------------------------------------------------------------------------------------------
	// Implementation of ITraceAction
	//----------------------------------------------------------------------------------------------------
	
	public void home() {
		detailCanvas.home();
	}

	public void timeZoomIn() {
		detailCanvas.timeZoomIn();
	}

	public void timeZoomOut() {
		detailCanvas.timeZoomOut();
	}

	public void processZoomIn() {
		detailCanvas.processZoomIn();
	}

	public void processZoomOut() {
		detailCanvas.processZoomOut();
	}

	public void undo() {
		detailCanvas.popUndo();		
	}

	public void redo() {
		detailCanvas.popRedo();		
	}
	
	public void save() {
		FileDialog saveDialog;
		saveDialog = new FileDialog(this.getViewSite().getShell(), SWT.SAVE);
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
					MessageBox msg = new MessageBox(this.getViewSite().getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
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

	public void open() {
		FileDialog openDialog;
		openDialog = new FileDialog(this.getViewSite().getShell(), SWT.OPEN);
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

	public void goNorth() {
		detailCanvas.goNorth();
	}

	public void goSouth() {
		detailCanvas.goSouth();
	}

	public void goEast() {
		detailCanvas.goEast();
	}

	public void goWest() {
		detailCanvas.goWest();
	}
	
}