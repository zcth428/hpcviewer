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
	final private Rectangle selection;
	
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
	private int processRange;
	
	private Color COMPLETELY_FILTERED_OUT_COLOR;
	private Color NOT_FILTERED_OUT_COLOR;
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
		
		// initialize colors
		COMPLETELY_FILTERED_OUT_COLOR = new Color(this.getDisplay(), 50,50,50);
		NOT_FILTERED_OUT_COLOR = new Color(this.getDisplay(), 128,128,128);
		
		// initialize pattern for filtered ranks
		PARTIALLY_FILTERED_PATTERN = createStripePattern();
		
		selection = new Rectangle(0,0,0,0);
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
		selection.height = r.height;
		selection.width = r.width;
		
		final int begProcess = _stData.getAttributes().getProcessBegin();
		processRange = _stData.getAttributes().getProcessEnd() - begProcess;
		
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
						
		view.width = getClientArea().width;
		view.height = getClientArea().height;
		
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
			event.gc.fillRectangle(view.x, view.y, selection.width, selection.height);
		}
		else
		{
			event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			event.gc.drawRectangle(selection.x, selection.y,
				selection.width, selection.height);
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
		
		view.x = (int)Math.round(topLeftTime * getScaleX());
		view.y = (int)Math.round(compensatedFirstProcess * getScaleY());
		
		int bottomRightPixelX = (int)Math.round(bottomRightTime*getScaleX());
		int bottomRightPixelY = (int)Math.round(compensatedLastProcess*getScaleY());
		
		selection.width = bottomRightPixelX-(int)view.x;
		selection.height = bottomRightPixelY-(int)view.y;
		
		insideBox = true;
		redraw();
	}
	
	/**Moves white box to correspond to where mouse has moved to.*/
	private void moveBox(Point mouseCurrent)
	{
		// compute the different cursor movement 
		int changeX = mouseCurrent.x-mousePrevious.x;
		int changeY = mouseCurrent.y-mousePrevious.y;
		
		// update the values of the view based on the different cursor movement
		view.x += changeX;
		view.y += changeY;
		
		// make sure that the view is not out of range
		
		view.x = Math.max(view.x, 0);
		view.y = Math.max(view.y, 0);

		// make sure that the view is not out of range 
		
		if (view.x+selection.width > view.width)
			view.x = view.width-selection.width;		

		if (view.y+selection.height>view.height)
			view.y = view.height-selection.height;
		
		mousePrevious = mouseCurrent;
	}
	

	/**Scales coordinates and sends them to detailCanvas.*/
	private void setDetailSelection()
	{
		Point miniTopLeft = new Point( view.x, view.y);
		Point miniBottomRight = new Point( view.x+selection.width, view.y+selection.height);
		
		long detailTopLeftTime = (long)(miniTopLeft.x/getScaleX());
		int detailTopLeftProcess = (int) (miniTopLeft.y/getScaleY());
		
		long detailBottomRightTime = (long)(miniBottomRight.x / getScaleX());
		int detailBottomRightProcess = (int) (miniBottomRight.y/getScaleY());

		ImageTraceAttributes attributes = stData.getAttributes();
		
		Frame frame = new Frame( attributes.getFrame() );
		frame.set(detailTopLeftTime, detailBottomRightTime, detailTopLeftProcess, detailBottomRightProcess);

		notifyRegionChangeOperation(frame);
	}
	
	/****
	 * notify to other views that we have changed the region to view
	 * 
	 * @param frame
	 */
	private void notifyRegionChangeOperation( Frame frame )
	{
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
    	selection.x = Math.max(0, Math.min(p1.x, p2.x) );
    	selection.y = Math.max(0, Math.min(p1.y, p2.y) );
    	
    	selection.width = Math.abs( p1.x - p2.x );
    	selection.height = Math.abs( p1.y - p2.y );
    	
    	// make sure that the selected box is within the range
    	
    	if ( selection.x + selection.width > view.width )
    		selection.width = view.width - selection.x;
    	
    	if ( selection.y + selection.height > view.height )
    		selection.height = view.height - selection.y;
    }
	
	/**********************************************************
	 * What happens when you let go of the selection box - 
	 * sets the bounds according to those of the selection box.
	 *********************************************************/
	private void setSelection(Point p1, Point p2)
	{
		adjustSelection(p1, p2);
		
		view.x = selection.x;
		view.y = selection.y;

		setDetailSelection();
	}
	
	/**Gets the scale in the X-direction (pixels per time unit).*/
	public double getScaleX()
	{
		return (double)view.width / (double)stData.getTimeWidth();
	}

	/**Gets the scale in the Y-direction (pixels per process).*/
	public double getScaleY()
	{
		return (double)view.height / processRange;
	}
	
	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 ******************************************************************/

	public void mouseDown(MouseEvent e)
	{
		if (mouseState == MouseState.ST_MOUSE_NONE)
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
			mousePrevious = new Point(e.x,e.y);
			
			insideBox = ( mouseDown.x>=view.x && 
					mouseDown.x<=view.x+selection.width && 
					mouseDown.y>=view.y &&  
					mouseDown.y<=view.y+selection.height );
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
	public void mouseDoubleClick(MouseEvent e) {	}
	
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