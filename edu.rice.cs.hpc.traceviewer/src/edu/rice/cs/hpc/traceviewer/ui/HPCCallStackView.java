package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.traceviewer.events.ITraceDepth;
import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeMiniCanvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/**A view for displaying the call path viewer and minimap.*/
//all the GUI setup for the call path and minimap are here//
public class HPCCallStackView extends ViewPart implements ISizeProvider, ITraceDepth, ITracePosition
{
	
	public static final String ID = "hpccallstackview.view";
	
	SpaceTimeData stData;
	
	Composite master;
	
	CallStackViewer csViewer;
	
	/** Paints and displays the miniMap.*/
	SpaceTimeMiniCanvas miniCanvas;
	
	Spinner depthEditor;
	
	public HPCTraceView traceview;
	
	public HPCDepthView depthview;

	public void createPartControl(Composite _master) 
	{
		master = _master;
		try 
		{
			depthview = (HPCDepthView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCDepthView.ID);
			traceview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
		}
		catch (PartInitException e) 
		{
			depthview = null;
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
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
		
		/*************************************************************************
		 * Depth View Spinner (the thing with the text box and little arrow buttons)
		 ************************************************************************/
		final HPCCallStackView csview = this;
		depthEditor = new Spinner(master, SWT.EMBEDDED);
		depthEditor.setMinimum(0);
		depthEditor.setLayout(new GridLayout());
		GridData depthData = new GridData(SWT.CENTER, SWT.TOP, true, false);
		depthData.widthHint = 140;
		depthEditor.setLayoutData(depthData);
		depthEditor.setVisible(false);
		depthEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				String string = depthEditor.getText();
				int value;
				if (string.length()<1)
					// be careful: on linux/GTK, any change in the spinner will consists of two steps:
					//  1) empty the string
					//  2) set with the specified value
					// therefore, we consider any empty string to be illegal
					return;
				else
					value = Integer.valueOf(string);
				int maximum = depthEditor.getMaximum();
				int minimum = 0;
				if (value > maximum)
					value = maximum;
				if (value < minimum)
					value = minimum;
				if(stData.getDepth() != value)
				{
					//traceview.setDepth(value, false);
					//csViewer.fixSample();
					stData.updateDepth(value, csview);
				}
			}
		});
		
		/*************************************************************************
		 * CallStackViewer
		 ************************************************************************/
		csViewer = new CallStackViewer(master, this);
		
		/*************************************************************************
		 * MiniMap
		 ************************************************************************/
		
		Label l = new Label(master, SWT.SINGLE);
		l.setText("Mini Map");
		miniCanvas = new SpaceTimeMiniCanvas(master, stData);
		miniCanvas.setLayout(new GridLayout());
		GridData miniCanvasData = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
		miniCanvasData.heightHint = 100;
		miniCanvasData.widthHint = 140;
		miniCanvas.setLayoutData(miniCanvasData);
		miniCanvas.setDetailCanvas(traceview.detailCanvas);
		traceview.detailCanvas.setMiniCanvas(miniCanvas);
		
		traceview.setCSView(this);
		depthview.setCSView(this);
		
		miniCanvas.setVisible(false);
	}
	
	
	public void updateData(SpaceTimeData _stData) 
	{
		this.stData = _stData;
		
		depthEditor.setMaximum(stData.getMaxDepth());
		depthEditor.setSelection(0);
		depthEditor.setVisible(true);

		this.csViewer.updateData(_stData);
		this.miniCanvas.updateData(_stData);
		
		stData.addDepthListener(this);
		stData.addPositionListener(this);
		miniCanvas.setVisible(true);
	}

	public void setFocus() 
	{
		
	}

	public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular, int preferredSize) 
	{
		return width ? 170 : availablePerpendicular;
	}

	public int getSizeFlags(boolean width) 
	{
		return width ? SWT.MAX : 0;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.events.ITraceDepth#setDepth(int)
	 */
	public void setDepth(int new_depth) {
		this.depthEditor.setSelection(new_depth);
		this.csViewer.setDepth(new_depth);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.events.ITracePosition#setPosition(edu.rice.cs.hpc.traceviewer.painter.Position)
	 */
	public void setPosition(Position position) {
		
		this.csViewer.setSample(position, stData.getDepth());
	}
}
