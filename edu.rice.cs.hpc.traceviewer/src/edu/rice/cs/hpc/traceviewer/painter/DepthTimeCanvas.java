package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.operation.DepthOperation;
import edu.rice.cs.hpc.traceviewer.operation.PositionOperation;
import edu.rice.cs.hpc.traceviewer.operation.RefreshOperation;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.operation.ZoomOperation;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.ui.Frame;
import edu.rice.cs.hpc.traceviewer.util.Utility;
import edu.rice.cs.hpc.traceviewer.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;

/**A view for displaying the depthview.*/
public class DepthTimeCanvas extends AbstractTimeCanvas 
implements IOperationHistoryListener, ISpaceTimeCanvas
{
	/**The left pixel's x location*/
	long topLeftPixelX;
	
	/**The selected time that is open in the csViewer.*/
	long selectedTime;
	
	/**The selected depth that is open in the csViewer.*/
	int selectedDepth;
	
	private int currentProcess = -1;
	private SpaceTimeDataController stData;
	
	final private ExecutorService threadExecutor;

	
	public DepthTimeCanvas(Composite composite)
    {
		super(composite, SWT.NONE);

		selectedTime = -20;
		selectedDepth = -1;
		
		threadExecutor = Executors.newFixedThreadPool( Utility.getNumThreads(0) ); 
		addDisposeListener( new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();				
			}
		});
	}
	
	/****
	 * new data update
	 * @param _stData
	 */
	public void updateView(SpaceTimeDataController stData)
	{
		super.init();
		setVisible(true);
		
		if (this.stData == null) {
			// just initialize once
			TraceOperation.getOperationHistory().addOperationHistoryListener(this);
		}
		this.stData = stData; 		
		selectedDepth = 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null || imageBuffer == null)
			return;
		
		super.paintControl(event);
		
		topLeftPixelX = Math.round(stData.getAttributes().getTimeBegin()*getScalePixelsPerTime());
		
		final int viewHeight = getClientArea().height;

		//--------------------
		//draws cross hairs
		//--------------------
		
		event.gc.setBackground(Constants.COLOR_WHITE);
		event.gc.setAlpha(240);
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScalePixelsPerTime())-2-topLeftPixelX);
		event.gc.fillRectangle(topPixelCrossHairX,0,4,viewHeight);
		
		int maxDepth = stData.getPainter().getMaxDepth();
		final int width = selectedDepth*viewHeight/maxDepth+viewHeight/(2*maxDepth);
		event.gc.fillRectangle(topPixelCrossHairX-8,width-1,20,4);
	}
	
	
	/**************************************************************************
	 * Sets the location of the crosshair to (_selectedTime, _selectedProcess).
	 * Also updates the rest of the program to know that this is the selected
	 * point (so that the CallStackViewer can update, etc.).
	 **************************************************************************/
	private void setPosition(Position position)
	{
		selectedTime = position.time;
		if (currentProcess != position.process) {
			rebuffer();
			currentProcess = position.process;
		} else
			// just display a new cross
			redraw();
	}
	
	/***
	 * set new depth
	 * @param _selectedDepth
	 */
	private void setDepth(int _selectedDepth) {
		selectedDepth = _selectedDepth;
		redraw();
	}
	
    /***
     * force to refresh the content of the canvas. 
     */
    public void refresh() {
		rebuffer();
    }
    
    
	public double getScalePixelsPerTime()
	{
		final int viewWidth = getClientArea().width;

		return (double)viewWidth / (double)getNumTimeDisplayed();
	}

	public double getScalePixelsPerRank() {
		final Rectangle r = this.getClientArea();
		return Math.max(r.height/(double)stData.getPainter().getMaxDepth(), 1);
	}


	//---------------------------------------------------------------------------------------
	// PRIVATE METHODS
	//---------------------------------------------------------------------------------------

	private long getNumTimeDisplayed()
	{
		return (stData.getAttributes().getTimeInterval());
	}
	
	private void setTimeZoom(long leftTime, long rightTime)
	{
		ImageTraceAttributes attributes = stData.getAttributes();
		attributes.setTime(leftTime, rightTime);
		
		attributes.assertTimeBounds(stData.getTimeWidth());
		
		if (getNumTimeDisplayed() < Constants.MIN_TIME_UNITS_DISP)
		{
			long begTime = attributes.getTimeBegin() + 
					(getNumTimeDisplayed() - Constants.MIN_TIME_UNITS_DISP)/2;
			long endTime = attributes.getTimeBegin() + getNumTimeDisplayed();
			
			attributes.setTime(begTime, endTime);
			attributes.assertTimeBounds(stData.getTimeWidth());
		}
	}

	
    
    private void zoom(long time1, long time2)
    {
    	setTimeZoom(time1, time2);
    	adjustCrossHair(time1, time2);
    	rebuffer();
    }
    
    /******************
     * Forcing the crosshair to be always inside the region
     * 
     * @param t1: the leftmost time
     * @param t2: the rightmost time
     */
    private void adjustCrossHair(long t1, long t2) {
    	Position currentPosition = stData.getPainter().getPosition();
    	long time = currentPosition.time;
    	
    	if (time<t1 || time>t2)
    		time = (t1+t2)>>1;
		
    	Position position = new Position(time, currentPosition.process);
    	setPosition(position);
    }
    
	private void rebuffer()
	{
		if (stData == null)
			return;

		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;

		if (viewWidth>0 && viewHeight>0) {
			if (imageBuffer != null) {
				imageBuffer.dispose();
			}
			//paints the current screen
			imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		}
		GC bufferGC = new GC(imageBuffer);
		bufferGC.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
		
		final ImageTraceAttributes attributes = stData.getAttributes();
		attributes.numPixelsDepthV = viewHeight;
		
		Debugger.printDebug(1, "DTC rebuffering " + attributes);
		
		try
		{
			paintDepthViewport(bufferGC,  
					attributes.getTimeBegin(), attributes.getTimeEnd(),
					viewWidth, viewHeight);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		bufferGC.dispose();
		
		redraw();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose()
	{
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
		threadExecutor.shutdown();
		super.dispose();
	}
	/*************************************************************************
	 * Paint the depth view
	 * 
	 * @param masterGC
	 * @param canvas
	 * @param _begTime
	 * @param _endTime
	 * @param _numPixelsH
	 * @param _numPixelsV
	 *************************************************************************/
	private void paintDepthViewport(final GC masterGC, 
			long _begTime, long _endTime, int _numPixelsH, int _numPixelsV)
	{
		boolean changedBounds = true ; //!( dtProcess == currentPosition.process && attributes.sameDepth(oldAttributes));
		
		ImageTraceAttributes attributes = stData.getAttributes();
		attributes.numPixelsDepthV = _numPixelsV;
		attributes.setTime(_begTime, _endTime);
		
		BaseViewPaint depthPaint = new DepthViewPaint(Util.getActiveWindow(), masterGC, stData, attributes, changedBounds, threadExecutor);		
		depthPaint.paint(this);
	}
	


	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void historyNotification(final OperationHistoryEvent event) {
		final IUndoableOperation operation = event.getOperation();
		
		if (operation.hasContext(TraceOperation.traceContext)) {
			final TraceOperation traceOperation =  (TraceOperation) operation;
			
			switch(event.getEventType()) 
			{
			case OperationHistoryEvent.DONE:
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
				executeOperation(traceOperation);
				break;
			}
		} else if (operation.hasContext(RefreshOperation.context)) {
			if (event.getEventType() == OperationHistoryEvent.DONE)
			{
				rebuffer();
			}
		}
	}

	/****
	 * execute an operation
	 * 
	 * @param operation
	 */
	private void executeOperation(final AbstractOperation operation)
	{
		if (operation instanceof ZoomOperation) {
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					Frame frame = ((ZoomOperation)operation).getFrame();
					Debugger.printDebug(1, "DTC attributes: " + stData.getAttributes() + "\t New: " + frame);

					zoom(frame.begTime, frame.endTime);
					setPosition(frame.position);
				}
			});
		} else if (operation instanceof PositionOperation) {
			Position p = ((PositionOperation)operation).getPosition();
			setPosition(p);
		} else if (operation instanceof DepthOperation) {
			int depth = ((DepthOperation)operation).getDepth();
			setDepth(depth);
		}
	}

	@Override
	void changePosition(Point point) {
    	long closeTime = stData.getAttributes().getTimeBegin() + (long)(point.x / getScalePixelsPerTime());
    	
    	Position currentPosition = stData.getPainter().getPosition();
    	Position newPosition = new Position(closeTime, currentPosition.process);
    		
    	try {
			TraceOperation.getOperationHistory().execute(
					new PositionOperation(newPosition), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void changeRegion(int left, int right) 
	{
		final ImageTraceAttributes attributes = stData.getAttributes();

		long topLeftTime 	 = attributes.getTimeBegin() + (long)(left / getScalePixelsPerTime());
		long bottomRightTime = attributes.getTimeBegin() + (long)(right / getScalePixelsPerTime());
		
		Frame frame = new Frame(topLeftTime, bottomRightTime,
				attributes.getProcessBegin(), attributes.getProcessEnd(),
				selectedDepth, (long)selectedTime, currentProcess);
		try {
			TraceOperation.getOperationHistory().execute(
					new ZoomOperation("Time zoom out", frame), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
