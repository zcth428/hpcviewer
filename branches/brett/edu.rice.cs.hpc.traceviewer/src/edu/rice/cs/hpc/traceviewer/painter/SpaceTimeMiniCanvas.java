package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.operation.ZoomOperation;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.ui.Frame;
/*****************************************************************************
 * 
 * The Canvas onto which the MiniMap is painted.
 * 
 ****************************************************************************/

public class SpaceTimeMiniCanvas extends SpaceTimeCanvas 
	implements MouseListener, MouseMoveListener, PaintListener, IOperationHistoryListener
{
	/** The top-left point of the current selection box.*/
	private Point selectionTopLeft;
	
	/** The bottom-right point of the current selection box.*/
	private Point selectionBottomRight;
	
	/**The width in pixels of the detail view representation on the miniMap.*/
	private int viewingWidth;
	
	/**The height in pixels of the detail view representation on the miniMap.*/
	private int viewingHeight;
	
	/** Relates to the condition that the mouse is in.*/
	private MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	private Point mouseDown;
	
	/** The point at which the mouse was on.*/
	private Point mousePrevious;
	
	/** The point at which the mouse was released.*/
	private Point mouseUp;
	
	/**Determines whether the first mouse click was inside the box or not.*/
	private boolean insideBox;
	
	/** We store the ones from the beginning so that we can display correctly even with filtering*/
	private int begProcess;
	private int endProcess;

	
	private final Color COMPLETELY_FILTERED_OUT_COLOR = new Color(this.getDisplay(), 50,50,50);
	private final Color NOT_FILTERED_OUT_COLOR = new Color(this.getDisplay(), 128,128,128);
	/**
	 * The pattern that we draw when we want to show that some ranks in the
	 * region aren't shown because of filtering
	 */
	private final Pattern PARTIALLY_FILTERED_PATTERN; 
	
	/**Creates a SpaceTimeMiniCanvas with the given parameters.*/
	public SpaceTimeMiniCanvas(Composite _composite)
	{	
		super(_composite);
		
		mouseState = MouseState.ST_MOUSE_INIT;
		insideBox = true;
		selectionTopLeft = new Point(0,0);
		selectionBottomRight = new Point(0,0);
		PARTIALLY_FILTERED_PATTERN = createStripePattern();
	}
	
	private Pattern createStripePattern() {
		Image image = new Image(getDisplay(), 15, 15);
		GC gc = new GC(image);
		gc.setBackground(NOT_FILTERED_OUT_COLOR);
		gc.fillRectangle(image.getBounds());
		gc.setForeground(COMPLETELY_FILTERED_OUT_COLOR);
		// Oddly enough, drawing from points outside of the image makes the
		// lines look a lot better when the pattern is tiled.
		for (int i = 5; i < 15; i+= 5) {
			gc.drawLine(-5, i+5, i+5, -5);
			gc.drawLine(i-5, 20, 20, i-5);
		}
		gc.dispose();
		return new Pattern(getDisplay(), image);
	}

	public void updateView(SpaceTimeDataController _stData) {
		this.setSpaceTimeData(_stData);

		if (this.mouseState == MouseState.ST_MOUSE_INIT) {
			this.mouseState = MouseState.ST_MOUSE_NONE;

			addMouseListener(this);
			addMouseMoveListener(this);
			addPaintListener(this);
			
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		}
		Rectangle r = this.getClientArea();
		this.viewingHeight = r.height;
		this.viewingWidth = r.width;
		
		begProcess = _stData.getAttributes().getProcessBegin();
		endProcess = _stData.getAttributes().getProcessEnd();
		
		this.redraw();
	}
	
	public void updateView()
	{
		ImageTraceAttributes attributes = stData.getAttributes();
		setBox(attributes.getTimeBegin(), attributes.getProcessBegin(), 
				attributes.getTimeEnd(), attributes.getProcessEnd());
	}
	
	/**The painting of the miniMap.*/
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null)
			return;
			
			
		viewWidth = getClientArea().width;
		viewHeight = getClientArea().height;
		
		
		event.gc.setBackground(COMPLETELY_FILTERED_OUT_COLOR);
		event.gc.fillRectangle(this.getClientArea());
		
		IBaseData basedata = stData.getBaseData();
		
		// This is of the middle region, that is either partially filtered away
		// or not filtered at all
		int topPx = (int) (basedata.getFirstIncluded()*getScaleY());
		//We need the +1 because we want to paint the last included as well
		int botPx = (int) ((basedata.getLastIncluded()+1)*getScaleY());
		
		if (basedata.isDenseBetweenFirstAndLast()){
			event.gc.setBackground(NOT_FILTERED_OUT_COLOR);
		} else {
			try{
			event.gc.setBackgroundPattern(PARTIALLY_FILTERED_PATTERN);
			}
			catch (SWTException e){
				System.out.println("Advanced graphics not supported");
				event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
			}
		}
		// The width of the region is always the same as the width of the
		// minimap because you can't filter by time
		event.gc.fillRectangle(0, topPx, getClientArea().width, botPx-topPx);
		
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
	public void setBox(long topLeftTime, int topLeftProcess, long bottomRightTime, int bottomRightProcess)
	{
		
		if (this.stData == null)
			return;

		
		//Compensating for filtering
		int compensatedFirstProcess = stData.getBaseData().getFirstIncluded() + topLeftProcess;
		int compensatedLastProcess = stData.getBaseData().getFirstIncluded() + bottomRightProcess;
		
		topLeftPixelX = (int)Math.round(topLeftTime * getScaleX());
		topLeftPixelY = (int)Math.round(compensatedFirstProcess * getScaleY());
		
		int bottomRightPixelX = (int)Math.round(bottomRightTime*getScaleX());
		int bottomRightPixelY = (int)Math.round(compensatedLastProcess*getScaleY());
		
		viewingWidth = bottomRightPixelX-(int)topLeftPixelX;
		viewingHeight = bottomRightPixelY-(int)topLeftPixelY;
		
		insideBox = true;
		redraw();
	}
	
	/**Moves white box to correspond to where mouse has moved to.*/
	private void moveBox(Point mouseCurrent)
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
	private void setDetailSelection()
	{
		Point miniTopLeft = new Point((int)topLeftPixelX,(int)topLeftPixelY);
		Point miniBottomRight = new Point((int)topLeftPixelX+viewingWidth, (int)topLeftPixelY+viewingHeight);
		
		long detailTopLeftTime = (long)(miniTopLeft.x/getScaleX());
		int detailTopLeftProcess = (int) (miniTopLeft.y/getScaleY());
		
		long detailBottomRightTime = (long)(miniBottomRight.x / getScaleX());
		int detailBottomRightProcess = (int) (miniBottomRight.y/getScaleY());

		ImageTraceAttributes attributes = stData.getAttributes();
		attributes.setProcess(detailTopLeftProcess, detailBottomRightProcess);
		attributes.setTime(detailTopLeftTime, detailBottomRightTime);
		
		stData.setTraceAttributes(attributes);
		final Position position = attributes.getPosition();
		
		Frame frame = new Frame(attributes.getTimeBegin(), attributes.getTimeEnd(), 
				attributes.getProcessBegin(), attributes.getProcessEnd(), 
				painter.getMaxDepth(), position.time, position.process);
				
		try {
			TraceOperation.getOperationHistory().execute(
					new ZoomOperation("Change region", frame),
					null, null);
		} catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**Updates the selectionBox on the MiniMap to have corners at p1 and p2.*/
	private void adjustSelection(Point p1, Point p2)
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
	private void setSelection(Point p1, Point p2)
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
		return (double)viewWidth / (double)stData.getTimeWidth();
	}

	/**Gets the scale in the Y-direction (pixels per process).*/
	public double getScaleY()
	{
		return (double)viewHeight / (endProcess - begProcess);
	}
	
	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 ******************************************************************/

	public void mouseDoubleClick(MouseEvent e)
	{
		//setSelection(new Point(0,0), new Point(viewWidth,viewHeight));
		//redraw();
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
				// If the user draws a very small region to zoom in on, we are
				// going to assume it was a mistake and not draw anything.
				if(Math.abs(mouseUp.x-mouseDown.x)>3 || Math.abs(mouseUp.y-mouseDown.y)>3)
					setSelection(mouseDown, mouseUp);	
				else //Set the selection box back to what it was because we didn't zoom
					setBox(attributes.getTimeBegin(), attributes.getProcessBegin(), 
							attributes.getTimeEnd(), attributes.getProcessEnd());
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
				if (traceOperation instanceof ZoomOperation) {
					Frame frame = traceOperation.getFrame();
					setBox(frame.begTime, frame.begProcess, frame.endTime, frame.endProcess);
				}
			}
		}
	}
	

}