package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

public class HPCDepthView extends ViewPart
{
	public static final String ID = "hpcdepthview.view";
	
	/**The composite that holds everything in the view*/
	Composite master;
	
	/**The max depth of the space time data.*/
	int maxDepth;
	
	/**The selectedProcess from the detail canvas/view*/
	int process;
	
	/** Paints and displays the detail view. */
	DepthTimeCanvas depthCanvas;
	
	/** Determines whether this view has been setup.*/
	boolean initialized = false;
	
	/** Determines whether this view has been setup.*/
	//public boolean openedView = false;
	
	HPCTraceView traceview;
	
	HPCCallStackView csview;
	
	public void createPartControl(Composite _master)
	{
		master = _master;
		
		try 
		{
			traceview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
		}
		catch (PartInitException e) 
		{
			traceview = null;
			e.printStackTrace();
			System.exit(0);
		}
		
		setupEverything();
	}
	
	public void setupEverything()
	{
		process = traceview.getSelectedProcess();
		if(process==-1)
			process = 0;
		
		/*************************************************************************
		 * Master Composite
		 */
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		/*************************************************************************
		 * Depth View Canvas
		 */
		
		depthCanvas = new DepthTimeCanvas(master, traceview.detailCanvas, process);
		depthCanvas.setLayout(new GridLayout());
		depthCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
	}
	
	public void updateData(SpaceTimeData _stData) {
		this.depthCanvas.updateData(_stData);
	}

	public void setFocus()
	{
		if (initialized)
		{
			//openedView = true;
			depthCanvas.setCSSample();
		}
	}
	
	public void updateProcess()
	{
		process = traceview.getSelectedProcess();
		if (process == -1)
			process = 0;
		if (process!=depthCanvas.process)
		{
			depthCanvas.process = process;
			depthCanvas.home();
		}
	}
	
	public void setCSView(HPCCallStackView _csview)
	{
		csview = _csview;
		depthCanvas.csViewer = csview.csViewer;
		traceview.detailCanvas.setDepthCanvas(depthCanvas);
		initialized = true;
	}
}
