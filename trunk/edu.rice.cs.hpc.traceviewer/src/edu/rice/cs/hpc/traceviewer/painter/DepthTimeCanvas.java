package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/**A view for displaying the depthview.*/
//all the GUI setup for the depth view is here
public class DepthTimeCanvas extends Canvas implements MouseListener, MouseMoveListener, PaintListener
{
	
	int maxDepth;
	
	SpaceTimeData stData;
	
	Image imageBuffer;
	
	/**The left pixel's x location*/
	long topLeftPixelX;
	
	/**The first/last time being viewed now*/
    long begTime;
    long endTime;
	
	/** The number of time units being displayed on the Detail View.*/
	long numTimeUnitsDisp;
	
	/**The min number of time units you can zoom in*/
    private final static int MIN_TIME_UNITS_DISP = 50000;
	
	/**The selected time that is open in the csViewer.*/
	double selectedTime;
	
	/**The selected depth that is open in the csViewer.*/
	int selectedDepth;
	
	/** Relates to the condition that the mouse is in.*/
	SpaceTimeCanvas.MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	Point mouseDown;
	
	/** The point at which the mouse was released.*/
	Point mouseUp;
	
	/** The left/right point that you selected.*/
	long leftSelection;
	long rightSelection;
    
    public SpaceTimeDetailCanvas detailCanvas;
    
    public static Color white;
    public static Color black;
	
	public DepthTimeCanvas(Composite composite, SpaceTimeDetailCanvas _detailCanvas, int _process)
    {
		super(composite, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);
		detailCanvas = _detailCanvas;

		mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_INIT;

		selectedTime = -20;
		selectedDepth = -1;
		leftSelection = 0;
		rightSelection = 0;
		white = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		this.getVerticalBar().setVisible(false);
		this.getHorizontalBar().setVisible(false);
	}
	
	
	public void updateData(SpaceTimeData _stData) {
		this.stData = _stData;
		this.maxDepth = _stData.getMaxDepth();
		
		if (this.mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_INIT) {
			this.mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_NONE;
			this.addCanvasListener();
		}
		this.home();
	}
	

	public void addCanvasListener() {
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);
		
		addListener(SWT.Resize, new Listener(){
			public void handleEvent(Event event)
			{
				//init();
				final int viewWidth = getClientArea().width;
				final int viewHeight = getClientArea().height;

				assertTimeBounds();
				
				if (viewWidth > 0 && viewHeight > 0) {
					getDisplay().asyncExec(new ResizeThread( new DepthBufferPaint()));
				}
			}
		});

	}
	
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null || imageBuffer == null)
			return;
		
		topLeftPixelX = Math.round(begTime*getScaleX());
		
		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;

		try {
			event.gc.drawImage(imageBuffer, 0, 0, viewWidth, viewHeight, 0, 0, viewWidth, viewHeight);
		} catch (Exception e) {
			// An exception "Illegal argument" will be raised if the resize method is not "fast" enough to create the image
			//		buffer before the painting is called. Thus, it causes inconsistency between the size of the image buffer
			//		and the size of client area. 
			//		If this happens, either we wait for the creation of image buffer, or do nothing. 
			//		I prefer to do nothing for the scalability reason.
			return;
		}
 		//paints the selection currently being made
		if (mouseState==SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN)
		{
        	event.gc.setForeground(white);
    		event.gc.setLineWidth(2);
    		event.gc.drawRectangle((int)(leftSelection-topLeftPixelX), 0, (int)(rightSelection-leftSelection), viewHeight);
        }
		
		//draws cross hairs
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScaleX())-2-topLeftPixelX);
		event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		event.gc.fillRectangle(topPixelCrossHairX,0,4,viewHeight);
		event.gc.fillRectangle(topPixelCrossHairX-8,selectedDepth*viewHeight/maxDepth+viewHeight/(2*maxDepth)-1,20,4);
		
		System.gc();
	}
	
	public void home()
	{
		setTimeZoom(0, (long)stData.getWidth());
	}
	
	private int currentProcess = -1;
	/**************************************************************************
	 * Sets the location of the crosshair to (_selectedTime, _selectedProcess).
	 * Also updates the rest of the program to know that this is the selected
	 * point (so that the CallStackViewer can update, etc.).
	 **************************************************************************/
	public void setPosition(Position position)
	{
		selectedTime = (double)position.time;
		if (currentProcess != position.process) {
			rebuffer();
			currentProcess = position.process;
		} else
			// just display a new cross
			redraw();
	}
	

	public void setDepth(int _selectedDepth) {
		selectedDepth = _selectedDepth;
		//rebuffer();
	}
	
	public void adjustSelection(Point p1, Point p2)
	{
		final int viewWidth = getClientArea().width;

    	leftSelection = topLeftPixelX + Math.max(Math.min(p1.x, p2.x), 0);
        rightSelection = topLeftPixelX + Math.min(Math.max(p1.x, p2.x), viewWidth-1);
    }
    
	/*****
	 * Refresh the content of the depth canvas based on the given time range.
	 * If the old time range is different from the new one, we will create a 
	 * new image buffer, otherwise just repaint the canvas
	 * @param _begTime
	 * @param _endTime
	 */
    public void refresh(long _begTime, long _endTime)
    {
    	if (stData == null)
    		return;
    	
    	//------------------------------------------------------
    	// check if the new time range is the same as the existing time range
    	// if it is the same then repaint the canvas, otherwise we have to 
    	//	create a new image buffer
    	//------------------------------------------------------
    	if (begTime != _begTime || endTime != _endTime)
    		// different time range. Needs to create a new image buffer
    		setTimeZoom(_begTime, _endTime);
    	else
    		this.redraw();
    }
    
    public void setCSSample()
    {
    	if(mouseDown == null)
    		return;
 /*   	int selectedProcess;
    	int procIndex;

    	//need to do different things if there are more traces to paint than pixels
    	if(detailCanvas.viewHeight > detailCanvas.endProcess-detailCanvas.begProcess)
    	{
    		selectedProcess = (int)(detailCanvas.begProcess+mouseDown.y/detailCanvas.getScaleY());
    		procIndex = (int)(mouseDown.y/detailCanvas.getScaleY());
    	}
    	else
    	{
    		selectedProcess = (int)(detailCanvas.begProcess+
    				(mouseDown.y*(detailCanvas.endProcess-detailCanvas.begProcess))/detailCanvas.viewHeight);
    		procIndex = mouseDown.y;
    	}*/
    	long closeTime = begTime + (long)((double)mouseDown.x / getScaleX());
    	
    	Position currentPosition = stData.getPosition();
    	Position position = new Position(closeTime, currentPosition.process);
    	position.processInCS = currentPosition.processInCS;
    	
    	this.stData.updatePosition(position);
    }

	public double getScaleX()
	{
		final int viewWidth = getClientArea().width;

		return (double)viewWidth / (double)numTimeUnitsDisp;
	}
	
	private class DepthBufferPaint implements BufferPaint {
		public void rebuffering() {
			rebuffer();
		}
	}
	//---------------------------------------------------------------------------------------
	// PRIVATE METHODS
	//---------------------------------------------------------------------------------------

	private void setTimeZoom(long leftTime, long rightTime)
	{
		begTime = leftTime;
		endTime = rightTime;
		
		assertTimeBounds();
		
		if (numTimeUnitsDisp < MIN_TIME_UNITS_DISP)
		{
			begTime += (numTimeUnitsDisp - MIN_TIME_UNITS_DISP)/2;
			numTimeUnitsDisp = MIN_TIME_UNITS_DISP;
			endTime = begTime + numTimeUnitsDisp;
			assertTimeBounds();
		}
		
		rebuffer();
	}

	private void assertTimeBounds()
	{
		if (begTime < 0)
			begTime = 0;
		if (endTime > (long)stData.getWidth())
			endTime = (long)stData.getWidth();
		
		numTimeUnitsDisp = endTime - begTime;
	}
    
    private void setDetail()
    {
		long topLeftTime = (long)((double)leftSelection / getScaleX());
		long bottomRightTime = (long)((double)rightSelection / getScaleX());
		setTimeZoom(topLeftTime, bottomRightTime);
		detailCanvas.setTimeRange(topLeftTime, bottomRightTime);
    }
	
	void rebuffer()
	{
		if (stData == null)
			return;
		
		//System.out.print("DTC rebuffer ");
		//this.traceCalls();
		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;

		//paints the current screen
		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		GC bufferGC = new GC(imageBuffer);
		bufferGC.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
		try
		{
			stData.paintDepthViewport(bufferGC, this, begTime, endTime, viewWidth, viewHeight);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		bufferGC.dispose();
		
		redraw();
	}

	private void traceCalls() {
		Throwable t = new Throwable();
		for (int i=2; i<10; i++) {
			System.out.println("\t" + t.getStackTrace()[i]);
		}
	}
	/******************************************************************
	 *		
	 *	MouseListener and MouseMoveListener interface Implementation
	 *      
	 ******************************************************************/

	public void mouseDoubleClick(MouseEvent e) { }

	public void mouseDown(MouseEvent e)
	{
		if (mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_NONE)
		{
			mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
		}
	}

	public void mouseUp(MouseEvent e)
	{
		if (mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN)
		{
			mouseUp = new Point(e.x,e.y);
			mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_NONE;
			
			//difference in mouse movement < 3 constitutes a "single click"
			if(Math.abs(mouseUp.x-mouseDown.x)<3 && Math.abs(mouseUp.y-mouseDown.y)<3)
			{
				setCSSample();
			}
			else
			{
				//If we're zoomed in all the way don't do anything
				if(numTimeUnitsDisp > MIN_TIME_UNITS_DISP)
				{
					//pushUndo();
					adjustSelection(mouseDown,mouseUp);
					setDetail();
				}
			}
		}
	}
	
	public void mouseMove(MouseEvent e)
	{
		if(mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN)
		{
			Point mouseTemp = new Point(e.x,e.y);
			adjustSelection(mouseDown,mouseTemp);
			redraw();
		}
	}
}
