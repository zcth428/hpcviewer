package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
/*****************************************************************************
 * 
 * The Canvas onto which the MiniMap is painted.
 * 
 ****************************************************************************/

public class SpaceTimeMiniCanvas extends SpaceTimeCanvas implements MouseListener, MouseMoveListener, PaintListener
{
	/**The detail canvas that correlates to this mini map.*/
	SpaceTimeDetailCanvas detailCanvas;
	
	/** The top-left point of the current selection box.*/
	Point selectionTopLeft;
	
	/** The bottom-right point of the current selection box.*/
	Point selectionBottomRight;
	
	/**The width in pixels of the detail view representation on the miniMap.*/
	int viewingWidth;
	
	/**The height in pixels of the detail view representation on the miniMap.*/
	int viewingHeight;
	
	/** Relates to the condition that the mouse is in.*/
	enum MouseState { ST_MOUSE_NONE, ST_MOUSE_DOWN };
	MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	Point mouseDown;
	
	/** The point at which the mouse was on.*/
	Point mousePrevious;
	
	/** The point at which the mouse was released.*/
	Point mouseUp;
	
	/**Determines whether or not the mini map has been painted yet.*/
	boolean initialized;
	
	/**Determines whether the first mouse click was inside the box or not.*/
	boolean insideBox;

	/**Creates a SpaceTimeMiniCanvas with the given parameters.*/
	public SpaceTimeMiniCanvas(Composite _composite, SpaceTimeData _stData)
	{	
		super(_composite, _stData);
		getHorizontalBar().setVisible(false);
		getVerticalBar().setVisible(false);
		mouseState = MouseState.ST_MOUSE_NONE;
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);
		insideBox = true;
		initialized = false;
		selectionTopLeft = new Point(0,0);
		selectionBottomRight = new Point(0,0);
	}
	
	/**Sets the detail canvas.*/
	public void setDetailCanvas(SpaceTimeDetailCanvas _detailCanvas)
	{
		detailCanvas = _detailCanvas;
	}
	
	/**The painting of the miniMap.*/
	public void paintControl(PaintEvent event)
	{
		if(!initialized)
		{
			viewWidth = getClientArea().width;
			viewHeight = getClientArea().height;
			initialized = true;
		}
		
		event.gc.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		event.gc.fillRectangle(this.getClientArea());
		
		
		if (insideBox)
		{
			event.gc.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			if(viewingWidth<1) viewingWidth = 1;
			if(viewingHeight<1) viewingHeight = 1;
			event.gc.fillRectangle((int)topLeftPixelX, (int)topLeftPixelY, viewingWidth, viewingHeight);
		}
		else
		{
			event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			event.gc.drawRectangle(selectionTopLeft.x, selectionTopLeft.y,
				selectionBottomRight.x-selectionTopLeft.x, selectionBottomRight.y-selectionTopLeft.y);
		}
	}
	
	/**Sets the white box in miniCanvas to correlate to spaceTimeDetailCanvas proportionally.*/
	public void setBox(long topLeftTime, double topLeftProcess, long bottomRightTime, double bottomRightProcess)
	{
		topLeftPixelX = (int)Math.round(topLeftTime * getScaleX());
		topLeftPixelY = (int)Math.round(topLeftProcess * getScaleY());
		
		int bottomRightPixelX = (int)Math.round(bottomRightTime*getScaleX());
		int bottomRightPixelY = (int)Math.round(bottomRightProcess*getScaleY());
		
		viewingWidth = bottomRightPixelX-(int)topLeftPixelX;
		viewingHeight = bottomRightPixelY-(int)topLeftPixelY;
		
		insideBox = true;
		redraw();
	}
	
	/**Moves white box to correspond to where mouse has moved to.*/
	public void moveBox(Point mouseCurrent)
	{
		int changeX = mouseCurrent.x-mousePrevious.x;
		int changeY = mouseCurrent.y-mousePrevious.y;
		
		topLeftPixelX += changeX;
		topLeftPixelY += changeY;
		if (topLeftPixelX < 0)
			topLeftPixelX = 0;
		else if (topLeftPixelX+viewingWidth>viewWidth)
			topLeftPixelX = viewWidth-viewingWidth;
		if (topLeftPixelY < 0)
			topLeftPixelY = 0;
		else if (topLeftPixelY+viewingHeight>viewHeight)
			topLeftPixelY = viewHeight-viewingHeight;
		
		mousePrevious = mouseCurrent;
	}
	
	/**Scales coordinates and sends them to detailCanvas.*/
	public void setDetailSelection()
	{
		Point miniTopLeft = new Point((int)topLeftPixelX,(int)topLeftPixelY);
		Point miniBottomRight = new Point((int)topLeftPixelX+viewingWidth, (int)topLeftPixelY+viewingHeight);
		
		long detailTopLeftTime = (long)(miniTopLeft.x/getScaleX());
		double detailTopLeftProcess = miniTopLeft.y/getScaleY();
		
		long detailBottomRightTime = (long)((double)miniBottomRight.x / getScaleX());
		double detailBottomRightProcess = miniBottomRight.y/getScaleY();
		
		detailCanvas.pushUndo();
		detailCanvas.setDetailZoom(detailTopLeftTime, detailTopLeftProcess, detailBottomRightTime, detailBottomRightProcess);
		setBox(detailCanvas.begTime, detailCanvas.begProcess, detailCanvas.endTime, detailCanvas.endProcess);
	}
	
	/**Updates the selectionBox on the MiniMap to have corners at p1 and p2.*/
	public void adjustSelection(Point p1, Point p2)
	{
    	selectionTopLeft.x = Math.min(p1.x, p2.x);
        selectionTopLeft.y = Math.min(p1.y, p2.y);
        if (selectionTopLeft.x < 0)
        	selectionTopLeft.x = 0;
        if (selectionTopLeft.y < 0)
        	selectionTopLeft.y = 0;
        
        selectionBottomRight.x = Math.max(p1.x, p2.x);
        selectionBottomRight.y = Math.max(p1.y, p2.y);
        if (selectionBottomRight.x > viewWidth)
        	selectionBottomRight.x = viewWidth;
        if (selectionBottomRight.y > viewHeight)
        	selectionBottomRight.y = viewHeight;
    }
	
	/**********************************************************
	 * What happens when you let go of the selection box - 
	 * sets the bounds according to those of the selection box.
	 *********************************************************/
	public void setSelection(Point p1, Point p2)
	{
		adjustSelection(p1, p2);
		topLeftPixelX = selectionTopLeft.x;
		topLeftPixelY = selectionTopLeft.y;
		viewingWidth = selectionBottomRight.x-selectionTopLeft.x;
		viewingHeight = selectionBottomRight.y-selectionTopLeft.y;
		insideBox = true;
		setDetailSelection();
	}
	
	/**Gets the scale in the X-direction (pixels per time unit).*/
	public double getScaleX()
	{
		return (double)viewWidth / (double)stData.getWidth();
	}

	/**Gets the scale in the Y-direction (pixels per process).*/
	public double getScaleY()
	{
		return (double)viewHeight / (double)stData.getHeight();
	}
	
	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 ******************************************************************/

	public void mouseDoubleClick(MouseEvent e)
	{
		setSelection(new Point(0,0), new Point(viewWidth,viewHeight));
		redraw();
	}

	public void mouseDown(MouseEvent e)
	{
		if (mouseState == MouseState.ST_MOUSE_NONE)
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
			mousePrevious = new Point(e.x,e.y);
			if (mouseDown.x>=topLeftPixelX && mouseDown.x<=topLeftPixelX+viewingWidth
					&& mouseDown.y>=topLeftPixelY && mouseDown.y<=topLeftPixelY+viewingHeight)
				insideBox = true;
			else
				insideBox = false;
		}
	}

	public void mouseUp(MouseEvent e)
	{
		if (mouseState == MouseState.ST_MOUSE_DOWN)
		{
			mouseUp = new Point(e.x,e.y);
			mouseState = MouseState.ST_MOUSE_NONE;
			if (insideBox)
			{
				moveBox(mouseUp);
				setDetailSelection();
			}
			else
			{
				if(Math.abs(mouseUp.x-mouseDown.x)>3 || Math.abs(mouseUp.y-mouseDown.y)>3)
					setSelection(mouseDown, mouseUp);
				else
					setBox(detailCanvas.begTime, detailCanvas.begProcess, detailCanvas.endTime, detailCanvas.endProcess);
			}
			redraw();
		}
	}
	
	public void mouseMove(MouseEvent e)
	{
		if(mouseState == MouseState.ST_MOUSE_DOWN)
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			Point mouseCurrent = new Point(e.x,e.y);
			if (insideBox)
				moveBox(mouseCurrent);
			else
				adjustSelection(mouseDown, mouseCurrent);
			
			redraw();
		}
	}
}