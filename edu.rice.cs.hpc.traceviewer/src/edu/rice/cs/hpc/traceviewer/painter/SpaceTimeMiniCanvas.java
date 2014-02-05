package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;
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
	private int processBegin, processEnd;

	/**Creates a SpaceTimeMiniCanvas with the given parameters.*/
	public SpaceTimeMiniCanvas(Composite _composite)
	{	
		super(_composite);
		
		mouseState = MouseState.ST_MOUSE_INIT;
		insideBox = true;
		
		//selection = new Rectangle(0,0,0,0);
	}


	/**********
	 * update the content of the view due to a new database
	 * 
	 * @param _stData : the new databse
	 */
	public void updateView(SpaceTimeDataController _stData) 
	{
		setSpaceTimeData(_stData);

		if (this.mouseState == MouseState.ST_MOUSE_INIT) {
			this.mouseState = MouseState.ST_MOUSE_NONE;

			addMouseListener(this);
			addMouseMoveListener(this);
			addPaintListener(this);
			
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		}
		Rectangle r = this.getClientArea();
		view.x = 0;
		view.y = 0;
		view.height = r.height;
		view.width  = r.width;
		
		processBegin = _stData.getAttributes().getProcessBegin();
        processEnd = _stData.getAttributes().getProcessEnd();

		redraw();
	}
	
	/******
	 * update the view when a filtering event occurs
	 * in this case, we need to reset the content of view with the attribute
	 */
	public void updateView() 
	{
		final Frame frame = stData.getAttributes().getFrame();		
		IBaseData baseData = stData.getBaseData();

		int p1 = (int) Math.round( (frame.begProcess+baseData.getFirstIncluded()) * getScaleY() );
		int p2 = (int) Math.round( (frame.endProcess+baseData.getFirstIncluded()) * getScaleY() );
		
		int t1 = (int) Math.round( frame.begTime * getScaleX() );
		int t2 = (int) Math.round( frame.endTime * getScaleX() );
		
		int dp = Math.max(p2-p1, 1);
		int dt = Math.max(t2-t1, 1);

		view.x = t1;
		view.y = p1;
		view.width  = dt; 
		view.height = dp;
	}
	
	/**The painting of the miniMap.*/
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null)
			return;
		
		final Rectangle clientArea = getClientArea();
		
		// paint the background with black color
		
		event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		event.gc.fillRectangle(clientArea);
		
		// paint the current view
		
		final Frame frame = stData.getAttributes().getFrame();		
		IBaseData baseData = stData.getBaseData();

		int p1 = (int) Math.round( (frame.begProcess+baseData.getFirstIncluded()) * getScaleY() );
		int p2 = (int) Math.round( (frame.endProcess+baseData.getFirstIncluded()) * getScaleY() );
		
		int t1 = (int) Math.round( frame.begTime * getScaleX() );
		int t2 = (int) Math.round( frame.endTime * getScaleX() );
		
		int dp = Math.max(p2-p1, 1);
		int dt = Math.max(t2-t1, 1);

		// original box
		event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		event.gc.fillRectangle(t1, p1, dt, dp);

		if (insideBox) {
			// when we move the box
			event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			event.gc.fillRectangle(view.x, view.y, dt, dp);
		} else {
			// when we want to create a new box
			event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
			event.gc.drawRectangle(view);
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
		
		view.width  = bottomRightPixelX-view.x;
		view.height = bottomRightPixelY-view.y;
		
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
		final Rectangle area = getClientArea();
		
		if (view.x+view.width > area.width)
			view.x = area.width-view.width;		

		int y = view.y+view.height; 
		if (y>area.height)
			view.y = area.height-view.height;
		
		int maxY = getHighestY();
		
		if (y > maxY) {
			view.y = maxY - view.height;
		}
		mousePrevious = mouseCurrent;
	}
	

	/**Scales coordinates and sends them to detailCanvas.*/
	private void setDetailSelection()
	{
		Point miniTopLeft = new Point( view.x, view.y);
		Point miniBottomRight = new Point( view.x+view.width, view.y+view.height);
		
		long detailTopLeftTime = (long)(miniTopLeft.x/getScaleX());
		int detailTopLeftProcess = (int) (miniTopLeft.y/getScaleY()) - processBegin;
		
		long detailBottomRightTime = (long)(miniBottomRight.x / getScaleX());
		int detailBottomRightProcess = (int) (miniBottomRight.y/getScaleY()) - processBegin;
				
		Frame frame = new Frame( attributes.getFrame() );
		frame.set(detailTopLeftTime, detailBottomRightTime, detailTopLeftProcess, detailBottomRightProcess);

		final IBaseData data = stData.getBaseData();

		// ------------------------------------------------------
		// make sure the area is within the allowed range
		// ------------------------------------------------------
		int highestRank = data.getLastIncluded();
		if (frame.endProcess > highestRank) {
			int dp = frame.endProcess - frame.begProcess;
			frame.endProcess = highestRank;
			frame.begProcess = highestRank - dp;
		}
		frame.begProcess = Math.max(data.getFirstIncluded(), frame.begProcess);

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
	
	/****
	 * retrieve the highest Y pixels based on the number of visible ranks
	 * 
	 * @return
	 */
	private int getHighestY() {
		
		final IBaseData baseData = stData.getBaseData();
		int highestRank = stData.getTotalTraceCount() + baseData.getFirstIncluded();
		return (int) Math.round(highestRank * getScaleY());
	}
	
	/****
	 * retrieve the minimum Y pixel based on the number of visible ranks
	 * In case of filters, the lowest rank can be other than zero
	 * 
	 * @return
	 */
	private int getLowestY() {
		final IBaseData baseData = stData.getBaseData();
		final int lowestRank = baseData.getFirstIncluded();
		return (int) Math.round(lowestRank * getScaleY());
	}
	
	/**Updates the selectionBox on the MiniMap to have corners at p1 and p2.*/
	private void adjustSelection(Point p1, Point p2)
	{
    	// ---------------------------------------------------------
		// get the region of the selection
    	// ---------------------------------------------------------
		view.x = Math.max(0, Math.min(p1.x, p2.x) );
		view.y = Math.max(0, Math.min(p1.y, p2.y) );
    	
		view.width = Math.abs( p1.x - p2.x );
		view.height = Math.abs( p1.y - p2.y );
    	
    	// ---------------------------------------------------------
    	// make sure that the selected box is within the range
    	// ---------------------------------------------------------
		final Rectangle area = getClientArea();

		// check if the width is within the range
    	if ( view.x + view.width > area.width )
    		view.width = area.width - view.x;
    	
    	// check if the height is within the range
    	int y = view.y + view.height; 
    	if ( y > area.height )
    		view.height = area.height - view.y;
    	
    	int maxY = getHighestY();
    	if (y > maxY) {
    		view.y = maxY - view.height;
    	}
		
    	view.y = Math.max(view.y, getLowestY() );
    }
	
	/**********************************************************
	 * What happens when you let go of the selection box - 
	 * sets the bounds according to those of the selection box.
	 *********************************************************/
	private void setSelection(Point p1, Point p2)
	{
		adjustSelection(p1, p2);
		setDetailSelection();
	}
	
	/**Gets the scale in the X-direction (pixels per time unit).*/
	public double getScaleX()
	{
		return (double)getClientArea().width / (double)stData.getTimeWidth();
	}

	/**Gets the scale in the Y-direction (pixels per process).*/
	public double getScaleY()
	{
		return (double)getClientArea().height / (processEnd-processBegin);
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
					mouseDown.x<=view.x+view.width && 
					mouseDown.y>=view.y &&  
					mouseDown.y<=view.y+view.height );
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
					Debugger.printDebug(1, "STMC: " + attributes + "\t New: " + frame);
					setBox(frame.begTime, frame.begProcess, frame.endTime, frame.endProcess);
				}
			}
		}
	}
}