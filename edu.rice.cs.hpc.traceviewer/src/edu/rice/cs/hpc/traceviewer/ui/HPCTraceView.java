package edu.rice.cs.hpc.traceviewer.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.traceviewer.actions.OptionMidpoint;
import edu.rice.cs.hpc.traceviewer.actions.OptionRecordsDisplay;
import edu.rice.cs.hpc.traceviewer.operation.RefreshOperation;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/**A view for displaying the traceviewer.*/
//all the GUI setup for the detail view is here
public class HPCTraceView extends HPCView implements ITraceViewAction
{
	
	/**The ID needed to create this view (used in plugin.xml).*/
	public static final String ID = "hpctraceview.view";
	
	/** Stores/Creates all of the data that is used in the view.*/
	private SpaceTimeDataController stData = null;
	
	/** Paints and displays the detail view.*/
	SpaceTimeDetailCanvas detailCanvas;
	
	/*************************************************************************
	 *	Creates the view.
	 ************************************************************************/
	public void createPartControl(Composite master)
	{
		// Laksono: do NOT maximize. On Linux with some WM, you can't restore it back !
		//this.getViewSite().getShell().setMaximized(true);
		/*************************************************************************
		 * Master Composite
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createToolbar(master);

        GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(detailCanvas);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detailCanvas);
		
		addTraceViewListener();
		
		super.createPartControl(master);		
	}

	
	/*************************************************************************
	 * update new data
	 *************************************************************************/
	public void updateView(SpaceTimeDataController _stData)
	{
		this.stData = _stData;
		this.detailCanvas.updateView(_stData);
		
		detailCanvas.setVisible(true);
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
	
	public SpaceTimeDataController getData()
	{
		return stData;
	}


	/*************************************************************************
	 * method to add listener
	 *************************************************************************/
	private void addTraceViewListener() 
	{
		final ISourceProviderService service = (ISourceProviderService)getSite().getService(ISourceProviderService.class);
		final ISourceProvider yourProvider = service.getSourceProvider(DataService.DATA_UPDATE);
		yourProvider.addSourceProviderListener( new ISourceProviderListener(){

			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {	}
			public void sourceChanged(int sourcePriority, String sourceName,
					Object sourceValue) {
				// eclipse bug: even if we set a very specific source provider, eclipse still
				//	gather event from other source. we then require to put a guard to avoid this.
				if (sourceName.equals(DataService.DATA_UPDATE)) {
					boolean needRefresh = false;
					if (sourceValue instanceof Boolean) {
						Boolean refresh = (Boolean) sourceValue;
						needRefresh = refresh.booleanValue();
					}
					detailCanvas.refresh(needRefresh);
				}
			}
		});
		
		// ---------------------------------------------------------------
		// register listener to capture event in menus or commands
		// ---------------------------------------------------------------
		final ICommandService commandService = (ICommandService) this.getSite().getService(ICommandService.class);
		commandService.addExecutionListener( new IExecutionListener(){

			public void notHandled(String commandId, NotHandledException exception) {}
			public void postExecuteFailure(String commandId, ExecutionException exception) {}
			public void preExecute(String commandId, ExecutionEvent event) {}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
			 */
			public void postExecuteSuccess(String commandId, Object returnValue) 
			{
				if (stData == null)
					return;
				
				// add listener when user change the state of "Show trace record" menu
				if (commandId.equals(OptionRecordsDisplay.commandId))
				{
					// force the canvas to redraw the content
					detailCanvas.refresh(false);
				} else if (commandId.equals(OptionMidpoint.commandId)) 
				{
					// changing painting policy means changing the content
					// we should force all views to refresh
					detailCanvas.refresh(true);
					RefreshOperation r_op = new RefreshOperation("painting policy change");
					try {
						TraceOperation.getOperationHistory().execute(r_op, null, null);
					} catch (ExecutionException e) {
						e.printStackTrace();
					}					
				}
			}
		});
	}
	
	private void createToolbar(Composite parent) {
		
		final IToolBarManager tbMgr = getViewSite().getActionBars().getToolBarManager();
		
		final TraceCoolBar traceCoolBar = new TraceCoolBar(tbMgr, this, SWT.NONE);
		
		/**************************************************************************
         * Process and Time dimension labels
         *************************************************************************/
		final Composite labelGroup = new Composite(parent, SWT.NONE);
				
		/*************************************************************************
		 * Detail View Canvas
		 ************************************************************************/
		
		detailCanvas = new SpaceTimeDetailCanvas(getSite().getWorkbenchWindow(), parent); 
		
		detailCanvas.setLabels(labelGroup);
		GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(labelGroup);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(labelGroup);
		
		detailCanvas.setButtons(new Action[]{traceCoolBar.home, traceCoolBar.open, traceCoolBar.save, null,
				null, traceCoolBar.tZoomIn, traceCoolBar.tZoomOut, traceCoolBar.pZoomIn, traceCoolBar.pZoomOut,
				traceCoolBar.goEast, traceCoolBar.goNorth, traceCoolBar.goSouth, traceCoolBar.goWest});
		
		detailCanvas.setVisible(false);
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
				ObjectInputStream in = null;
				try
				{
					in = new ObjectInputStream(new FileInputStream(fileName));
					Frame current = (Frame)in.readObject();
					detailCanvas.open(current);
					validFrameFound = true;
				}
				catch (IOException e)
				{
					validFrameFound = false;
					MessageDialog.openError(getViewSite().getShell(), "Error reading the file",
							"Fail to read the file: " + fileName );
				}
				catch (ClassNotFoundException e)
				{
					validFrameFound = false;
					MessageDialog.openError(getViewSite().getShell(), "Error reading the file", 
							"File format is not recognized. Either the file is corrupted or it's an old format");
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						MessageDialog.openWarning(getViewSite().getShell(), "Error closing the file", 
								"Unable to close the file: " + fileName);
					}
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


	@Override
	protected ImageData getImageData() {
		return detailCanvas.getImageData();
	}


	@Override
	protected Control getMainControl() {
		return detailCanvas;
	}
	
}