package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.events.ITraceDepth;
import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

public class HPCDepthView extends ViewPart implements ITraceDepth, ITracePosition
{
	public static final String ID = "hpcdepthview.view";
	
	/**The composite that holds everything in the view*/
	Composite master;
	
	/**The max depth of the space time data.*/
	int maxDepth;
	
	/** Paints and displays the detail view. */
	DepthTimeCanvas depthCanvas;
	
	/** Determines whether this view has been setup.*/
	boolean initialized = false;
	
	HPCTraceView traceview;
	
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
		/*************************************************************************
		 * Master Composite
		 */
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		/*************************************************************************
		 * Depth View Canvas
		 */
		
		depthCanvas = new DepthTimeCanvas(master, traceview.detailCanvas, 0);
		depthCanvas.setLayout(new GridLayout());
		depthCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		depthCanvas.setVisible(false);
	}
	
	public void updateData(SpaceTimeData _stData) {
		this.depthCanvas.updateData(_stData);
		_stData.addDepthListener(this);
		_stData.addPositionListener(this);
		depthCanvas.setVisible(true);
	}

	public void setFocus()
	{
		if (initialized)
		{
			depthCanvas.setCSSample();
		}
	}
		
	public void setCSView(HPCCallStackView _csview)
	{
		traceview.detailCanvas.setDepthCanvas(depthCanvas);
		initialized = true;
	}

	public void setDepth(int new_depth) {
		this.depthCanvas.setDepth(new_depth);
	}

	public void setPosition(Position position) {
		this.depthCanvas.setCrossHair(position.time);
	}
}
