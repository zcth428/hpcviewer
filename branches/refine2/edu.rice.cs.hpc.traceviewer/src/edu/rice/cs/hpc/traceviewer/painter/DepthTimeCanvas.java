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
	
	/**The width/height of the current screen in this canvas*/
    int viewWidth;
    int viewHeight;
	
	/**The first/last time being viewed now*/
    long begTime;
    long endTime;
	
	/** Stores whether the Detail Panel has changed screens from the first frame or not. */
	boolean homeScreen;
	
	/** Stores whether or not the Detail Panel's background has just changed or not (used to determine when to rebuffer)*/
	boolean rebuffer;
	
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

		homeScreen = true;
		rebuffer = true;
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
		this.init();
		this.redraw();
	}
	
	private void init() {
		viewWidth = getClientArea().width;
		viewHeight = getClientArea().height;
	}
	
	public void addCanvasListener() {
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);
		
		addListener(SWT.Resize, new Listener(){
			public void handleEvent(Event event)
			{
				init();
				if (homeScreen)
					imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
				
				if (viewWidth > 0 && viewHeight > 0)
					rebuffer = true;

				assertTimeBounds();
			}
		});

	}
	
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null)
			return;
		
		if (homeScreen)
		{
			topLeftPixelX = 0;
			setTimeZoom(0, (long)stData.getWidth());
			homeScreen = false;
		}
		else
		{
			topLeftPixelX = Math.round(begTime*getScaleX());
		}
		
		if (rebuffer)
		{
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
			rebuffer = false;
		}
		
		event.gc.drawImage(imageBuffer, 0, 0, viewWidth, viewHeight, 0, 0, viewWidth, viewHeight);

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
	
	/**************************************************************************
	 * Sets the location of the crosshair to (_selectedTime, _selectedProcess).
	 * Also updates the rest of the program to know that this is the selected
	 * point (so that the CallStackViewer can update, etc.).
	 **************************************************************************/
	public void setCrossHair(double _selectedTime)
	{
		selectedTime = _selectedTime;
		redraw();
	}
	
	public void setDepth(int _selectedDepth) {
		selectedDepth = _selectedDepth;
		redraw();
	}
	
	public void adjustSelection(Point p1, Point p2)
	{
    	leftSelection = topLeftPixelX + Math.max(Math.min(p1.x, p2.x), 0);
        rightSelection = topLeftPixelX + Math.min(Math.max(p1.x, p2.x), viewWidth-1);
    }
    
    public void setTimeRange(long begTime, long endTime)
    {
    	rebuffer();
    	setTimeZoom(begTime, endTime);
    }
    
    public void setCSSample()
    {
    	if (mouseDown == null)
    		return;
    	long closeTime = begTime + (long)((double)mouseDown.x / getScaleX());

    	stData.updatePosition(new Position(closeTime, stData.getPosition().process));
    }

	public double getScaleX()
	{
		return (double)viewWidth / (double)numTimeUnitsDisp;
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
		
		redraw();
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
	
	private void rebuffer()
	{
		rebuffer = true;
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
			redraw();
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
