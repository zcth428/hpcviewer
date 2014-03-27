package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.traceviewer.data.util.Constants;



/**********************************************************************************
 * 
 * abstract class for helper information (located on the bottom of the window)
 *
 **********************************************************************************/
public abstract class AbstractTimeCanvas
extends Canvas
implements ITraceCanvas, PaintListener
{
	/** Relates to the condition that the mouse is in.*/
	private ITraceCanvas.MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	private Point mouseDown;
	
	/** The left/right point that you selected.*/
	private int leftSelection;
	private int rightSelection;

	protected 	Image imageBuffer;
	
	
	/****************
	 * Constructs a new instance of this class given its parent and a style value describing its behavior and appearance.
	 *
	 * @param composite
	 * @param style
	 ****************/
	public AbstractTimeCanvas(Composite composite, int style) {
		super(composite, style);
		mouseState = ITraceCanvas.MouseState.ST_MOUSE_INIT;
	}
	
	
	public void init() 
	{
		if (mouseState == ITraceCanvas.MouseState.ST_MOUSE_INIT) 
		{
			addMouseListener(this);
			addMouseMoveListener(this);
			addPaintListener(this);
			addFocusListener(new FocusAdapter() {
				/*
				 * (non-Javadoc)
				 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
				 */
				public void focusLost(FocusEvent e) {
					AbstractTimeCanvas.this.initMouseSelection();
				}
			});
		}
		initMouseSelection();
	}
	
	/*****
	 * initialize variables 
	 */
	private void initMouseSelection()
	{
		leftSelection = 0;
		rightSelection = 0;
		mouseState = ITraceCanvas.MouseState.ST_MOUSE_NONE;
	}
	
	@Override
	public void mouseMove(MouseEvent e) 
	{
		if (mouseState == ITraceCanvas.MouseState.ST_MOUSE_DOWN)
		{
			Point pos = new Point(e.x, e.y);
			adjustPosition(mouseDown, pos);
			
			//notifyRegionChange(mouseDown, pos);
			redraw();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {}

	@Override
	public void mouseDown(MouseEvent e) 
	{
		if (mouseState == ITraceCanvas.MouseState.ST_MOUSE_NONE)
		{
			mouseState = ITraceCanvas.MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
		}
	}

	@Override
	public void mouseUp(MouseEvent e) 
	{
		if (mouseState == ITraceCanvas.MouseState.ST_MOUSE_DOWN)
		{
			Point mouseUp = new Point(e.x,e.y);
			mouseState = ITraceCanvas.MouseState.ST_MOUSE_NONE;
			
			//difference in mouse movement < 3 constitutes a "single click"
			if(Math.abs(mouseUp.x-mouseDown.x)<3 && Math.abs(mouseUp.y-mouseDown.y)<3)
			{
				changePosition(mouseDown);
			}
			else
			{
				adjustPosition(mouseDown, mouseUp);
				changeRegion(leftSelection, rightSelection);
			}
			redraw();
		}
	}

	@Override
	public void paintControl(PaintEvent event) 
	{
		if (imageBuffer == null)
			return;
		
		final Rectangle bounds = imageBuffer.getBounds();
		final Rectangle area   = getClientArea();

		try 
		{
			event.gc.drawImage(imageBuffer, 0, 0, bounds.width, bounds.height, 
											0, 0, area.width, area.height);
		}
		catch (Exception e)
		{
			// An exception "Illegal argument" will be raised if the resize method is not "fast" enough to create the image
			//		buffer before the painting is called. Thus, it causes inconsistency between the size of the image buffer
			//		and the size of client area. 
			//		If this happens, either we wait for the creation of image buffer, or do nothing. 
			//		I prefer to do nothing because of scalability concerns.
			return;
		}
		
 		//paints the selection currently being made
		
		if (mouseState==ITraceCanvas.MouseState.ST_MOUSE_DOWN)
		{
        	event.gc.setBackground(Constants.COLOR_WHITE);
    		event.gc.setAlpha(100);
    		event.gc.fillRectangle( leftSelection, 0, (rightSelection-leftSelection), area.height);
    		
    		event.gc.setLineWidth(2);
    		event.gc.setAlpha(240);
    		event.gc.setForeground(Constants.COLOR_BLACK);
    		event.gc.drawRectangle(leftSelection, 0, rightSelection-leftSelection, area.height);
        }
	}
	
	
	private void adjustPosition(Point p1, Point p2) 
	{
		leftSelection = Math.min(p1.x, p2.x);
		rightSelection = Math.max(p1.x, p2.x);
	}
	
	/*************************
	 * function called when there's a change of mouse click position
	 * 
	 * @param point
	 *************************/
	abstract void changePosition(Point point);
	
	
	/***************************
	 * function called when there's a change of selected region
	 * 
	 * @param left
	 * @param right
	 ***************************/
	abstract void changeRegion(int left, int right);
}
