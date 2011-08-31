package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.traceviewer.util.Constants;

public abstract class HelperCanvas extends Canvas implements MouseListener,
		MouseMoveListener {

	/** Relates to the condition that the mouse is in.*/
	private SpaceTimeCanvas.MouseState mouseState;
	/** The point at which the mouse was clicked.*/
	private Point mouseDown;
	
	/** The point at which the mouse was released.*/
	private Point mouseUp;
	
	private boolean start = false;

	public HelperCanvas(Composite parent, int style) {
		super(parent, style);
		mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_INIT;
		this.getVerticalBar().setVisible(false);
		this.getHorizontalBar().setVisible(false);
	}

	
	public void addCanvasListener() {
		addMouseListener(this);
		addMouseMoveListener(this);
	}
	
	
	public void setStart() {
		start = true;
	}
	
	
	public void paintControl(PaintEvent event)
	{
		if (!start)
			return;
				
		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;

		try
		{
			event.gc.drawImage(getImageBuffer(), 0, 0, viewWidth, viewHeight, 0, 0, viewWidth, viewHeight);
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
		if (mouseState==SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN)
		{
			//drawSelection(event.gc, mouseUp);
        }
		
		//draws cross hairs
		//drawCrossHair(event.gc);
	}
	
	
	private void drawCrossHair(GC gc) 
	{
		final int viewHeight = getClientArea().height;
		
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(mouseDown.x,0,4,viewHeight);
		gc.fillRectangle(mouseDown.x-8,getCurrentYPosition(),20,4);
		
	}
	
	private void drawSelection(GC gc, Point pEnd) 
	{
		gc.setForeground(Constants.COLOR_WHITE);
		gc.setLineWidth(2);
		final int viewHeight = this.getClientArea().height;
		gc.drawRectangle( mouseDown.x, 0, pEnd.x, viewHeight);
	}
	
	public void mouseMove(MouseEvent e) {
/*		if(mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN)
		{
			mouseUp = new Point(e.x,e.y);
			redraw();
		}*/
	}

	public void mouseDoubleClick(MouseEvent e) {}

	public void mouseDown(MouseEvent e) {
/*		if (mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_NONE)
		{
			mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
		}*/
	}

	public void mouseUp(MouseEvent e) 
	{
/*		if(mouseState == SpaceTimeCanvas.MouseState.ST_MOUSE_DOWN) {
			mouseUp = new Point(e.x,e.y);
			mouseState = SpaceTimeCanvas.MouseState.ST_MOUSE_NONE;
			redraw();
		}*/
	}

	abstract protected Image getImageBuffer();
	abstract protected int getCurrentYPosition();
}
