package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SummaryTimeCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

public class HPCSummaryView extends ViewPart implements ITracePosition
{

	public static final String ID = "hpcsummaryview.view";
	
	/**The composite that holds everything in the view*/
	Composite master;
	
	/**The canvas that actually displays this view*/
	SummaryTimeCanvas summaryCanvas;
	
	/**all gui is held here*/
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
		 * Summary View Canvas
		 */
		
		summaryCanvas = new SummaryTimeCanvas(master, traceview.detailCanvas);
		summaryCanvas.setLayout(new GridLayout());
		summaryCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summaryCanvas.setVisible(false);
		
		traceview.detailCanvas.setSummaryCanvas(summaryCanvas);
	}
	
	public void updateData(SpaceTimeData stData)
	{
		//stData.addPositionListener(this);
		summaryCanvas.setVisible(true);
		traceview.detailCanvas.rebuffer();
	}
	
	public void setFocus() 
	{
		this.summaryCanvas.setFocus();
	}

	public void setPosition(Position position) 
	{
		summaryCanvas.setPosition(position);
	}
}
