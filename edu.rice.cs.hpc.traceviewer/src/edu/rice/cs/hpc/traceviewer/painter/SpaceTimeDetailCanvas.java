package edu.rice.cs.hpc.traceviewer.painter;

import java.util.Stack;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.ui.Frame;
import edu.rice.cs.hpc.traceviewer.util.Constants;


/*************************************************************************
 * 
 *	Canvas onto which the detail view is painted. Also takes care of
 *	zooming responsibilities of the detail view.
 *
 ************************************************************************/
public class SpaceTimeDetailCanvas extends SpaceTimeCanvas implements MouseListener, MouseMoveListener, PaintListener
{
	
	/**an enum used for deciding what type of data we have*/
	public enum DataType {Uninitialized, ProcessOnly, ThreadsOnly, ProcessAndThreads};
	
	/**The buffer image that is copied onto the actual canvas.*/
	Image imageBuffer;
	
	/** The number of time units being displayed on the Detail View.*/
	long numTimeUnitsDisp;
	
	/** The number of processes being displayed on the Detail View.*/
	double numProcessDisp;
	
	/**Triggers zoom back to beginning view screen.*/
	Action homeButton;
	
	/**Triggers open function to open previously saved frame.*/
	Action openButton;
	
	/**Triggers save function to save current frame to file.*/
	Action saveButton;
	
	/**Triggers undo of screen.*/
	Action undoButton;
	
	/**Triggers screen re-do.*/
	Action redoButton;
	
	/** Triggers zoom-in on the time axis.*/
	Action tZoomInButton;
	
	/** Triggers zoom-out on the time axis.*/
	Action tZoomOutButton;
	
	/** Triggers zoom-in on the process axis.*/
	Action pZoomInButton;
	
	/** Triggers zoom-out on the process axis.*/
	Action pZoomOutButton;

	Action goEastButton, goNorthButton, goWestButton, goSouthButton;
	
	/** The SpaceTimeMiniCanvas that is changed by the detailCanvas.*/
	SpaceTimeMiniCanvas miniCanvas;
	
	/** The DepthTimeCanvas that is changed by the detailCanvas.*/
	DepthTimeCanvas depthCanvas = null;
	
	/** The SummaryTimeCanvas that is changed by the detailCanva.*/
	SummaryTimeCanvas summaryCanvas = null;
	
	/** Relates to the condition that the mouse is in.*/
	MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	Point mouseDown;
	
	/** The point at which the mouse was released.*/
	Point mouseUp;
	
	/** The top-left point that you selected.*/
	long selectionTopLeftX;
	long selectionTopLeftY;
	
	/** The bottom-right point that you selected.*/
	long selectionBottomRightX;
	long selectionBottomRightY;
	
	/**The stack holding all the frames previously done.*/
	Stack<Frame> undoStack;
	
	/**The stack holding all the frames previously undone.*/
	Stack<Frame> redoStack;
	
	/**The Group containing the labels. labelGroup.redraw() is called from the Detail Canvas.*/
    Composite labelGroup;
   
    /**The Label with the time boundaries.*/
    public Label timeLabel;
   
    /**The Label with the process boundaries.*/
    public Label processLabel;
    
    /**The Label with the current cross hair information.*/
    public Label crossHairLabel;
        
    /**The min number of process units you can zoom in.*/
    private final static int MIN_PROC_DISP = 1;
    
    /**Creates a SpaceTimeDetailCanvas with the given parameters*/
	public SpaceTimeDetailCanvas(Composite _composite)
	{
		super(_composite );
		
		//homeScreen = true;
		undoStack = new Stack<Frame>();
		redoStack = new Stack<Frame>();
		mouseState = MouseState.ST_MOUSE_INIT;

		selectionTopLeftX = 0;
		selectionTopLeftY = 0;
		selectionBottomRightX = 0;
		selectionBottomRightY = 0;
				
		if (this.stData != null) {
			this.addCanvasListener();
		}
		
	}


	/*****
	 * set new database and refresh the screen
	 * @param _stData
	 */
	public void updateData(SpaceTimeData _stData) {
		this.setSpaceTimeData(_stData);
		
		if (mouseState == MouseState.ST_MOUSE_INIT)
		{
			mouseState = MouseState.ST_MOUSE_NONE;
			this.addCanvasListener();
		}
		
		long rangeX = this.stData.getWidth();
		int rangeY = this.stData.getHeight();
		
		this.home();

		stData.setDepth(0);
		
		Position position = new Position(rangeX>>1, rangeY>>1);
		stData.updatePosition(position);
		
		// forcing the depth canvas to update the position
		this.depthCanvas.setPosition(position);

		// clear undo button
		this.undoStack.clear();
		this.undoButton.setEnabled(false);
		
		this.saveButton.setEnabled(true);
		this.openButton.setEnabled(true);
	}
	
	/***
	 * add listeners to the canvas 
	 * caution: this method can only be called at most once ! 
	 */
	private void addCanvasListener() {
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);
		
		addKeyListener( new KeyListener(){

			public void keyPressed(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {
				switch (e.keyCode) {
				
				case SWT.ARROW_DOWN:
					if (canGoSouth())
						goSouth();
					break;
				case SWT.ARROW_UP:
					if (canGoNorth())
						goNorth();
					break;
				case SWT.ARROW_LEFT:
					if (canGoEast())
						goEast();
					break;
				case SWT.ARROW_RIGHT:
					if (canGoWest())
						goWest();
					break;				
				}
			}
			
		});
				
		// ------------------------------------------------------------------------------------
		// A listener for resizing the the window.
		// In order to get the last resize position, we will use timer to check if the current
		//  resize event is invoked "long" enough to the first resize event.
		// If this is the case, then run rebuffering, otherwise just no-op.
		// ------------------------------------------------------------------------------------
		addListener(SWT.Resize, new Listener(){
			//private long lastTime = 0;
			
			// subjectively the difference between the first event and the current one
			// please modify this constant if you think it is too long or too short 
			//final static private long TIME_DIFF = 400;
			
			// subjectively the difference between the new size and the old size
			// if the difference is within the range, we scale. Otherwise recompute
			final static private float SIZE_DIFF = (float) 0.08;
			
			static final int MIN_WIDTH = 2;
			static final int MIN_HEIGHT = 2;

			public void handleEvent(Event event)
			{	
				final Rectangle r = getClientArea();

				if (!needToRebuffer(r))
				{	// no need to rebuffer, just scaling
					rescaling(r);
					return;
					
				} else {
					// resize to bigger region: needs to recompute the data
					viewWidth = r.width;
					viewHeight = r.height;
					getDisplay().asyncExec(new ResizeThread(new DetailBufferPaint()));
				}				
			}
			
			private boolean needToRebuffer(Rectangle r ) {
				final ImageData imgData = imageBuffer.getImageData();
				
				// we just scale the image if the current size is smaller than the original one
				if (r.width<=imgData.width && r.height<=imgData.height)
					return false; // no need to rebuffer
				
				// we scale the image if the difference is relatively "small" between
				// the original and the current image
				
				final float diffx = (float)Math.abs(imgData.width-r.width) / (float)Math.max(r.width, imgData.width);
				final float diffy = (float)Math.abs(imgData.height-r.height) / (float)Math.max(r.height, imgData.height);

				return (diffx>SIZE_DIFF || diffy>SIZE_DIFF);
			}
			
			private void rescaling(Rectangle r) {
				
				final ImageData imgData = imageBuffer.getImageData();
				
				// ------------------------------------------------------------------
				// quick hack: do not rescaling if we try to minimize the image
				// An image is "accidently" minimized, if another view is maximized
				// ------------------------------------------------------------------
				
				if (r.width<MIN_WIDTH && r.height < MIN_HEIGHT)
					return; // just do nothing to preserve the image

				ImageData scaledImage = imgData.scaledTo(r.width, r.height);
				imageBuffer = new Image(getDisplay(), scaledImage);

				viewWidth = r.width;
				viewHeight = r.height;
				redraw();
			}
		});
	}

	private class DetailBufferPaint implements BufferPaint {
		public void rebuffering() {
			rebuffer();
		}
	}

	
	/*************************************************************************
	 * Sets the bounds of the data displayed on the detail canvas to be those 
	 * specified by the zoom operation and adjusts everything accordingly.
	 *************************************************************************/
	public void setDetailZoom(long _topLeftTime, double _topLeftProcess, long _bottomRightTime, double _bottomRightProcess)
	{
		begTime = _topLeftTime;
		begProcess = _topLeftProcess;
		endTime = _bottomRightTime;
		endProcess = _bottomRightProcess;
		
		assertProcessBounds();
		assertTimeBounds();
		
		if (numTimeUnitsDisp < Constants.MIN_TIME_UNITS_DISP)
		{
			begTime += (numTimeUnitsDisp - Constants.MIN_TIME_UNITS_DISP) / 2;
			numTimeUnitsDisp = Constants.MIN_TIME_UNITS_DISP;
			endTime = begTime + numTimeUnitsDisp;
		}
		
		if (numProcessDisp < MIN_PROC_DISP)
		{
			numProcessDisp = MIN_PROC_DISP;
			begProcess = (int)begProcess;
			endProcess = begProcess+numProcessDisp;
		}
		
		this.updateButtonStates();
		this.rebuffer();
		
		//----------------------------------------------------------------------------
		// we have new region. Check if the cross hair is still within the new region
		//----------------------------------------------------------------------------
		this.adjustCrossHair(_topLeftTime, _topLeftProcess, _bottomRightTime, _bottomRightProcess);

	}
	
	/*******************************************************************************
	 * Actually does the repainting of the canvas when a PaintEvent is sent to it
	 * (basically when anything at all is changed anywhere on the application 
	 * OR when redraw() is called).
	 ******************************************************************************/
	public void paintControl(PaintEvent event)
	{
		if (this.stData == null)
			return;
		
		topLeftPixelX = Math.round(begTime * getScaleX());
		topLeftPixelY = Math.round(begProcess * getScaleY());
		
		Rectangle region = imageBuffer.getBounds();
		if (region.width != viewWidth || region.height != viewHeight)
			return;
		
		//if something has changed the bounds, you need to go get the data again
		event.gc.drawImage(imageBuffer, 0, 0, viewWidth, viewHeight, 0, 0, viewWidth, viewHeight);
    	
		//paints the selection currently being made (the little white box that appears
		//when you click and drag
		if(mouseState==MouseState.ST_MOUSE_DOWN)
		{
        	event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    		event.gc.setLineWidth(2);
    		event.gc.drawRectangle((int)(selectionTopLeftX-topLeftPixelX), (int)(selectionTopLeftY-topLeftPixelY), (int)(selectionBottomRightX-selectionTopLeftX),
            		(int)(selectionBottomRightY-selectionTopLeftY));
        }
		
		//draws cross hairs
		long selectedTime = stData.getPosition().time;
		int selectedProcess = stData.getPosition().process;
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScaleX())-10-topLeftPixelX);
		int topPixelCrossHairY = (int)(Math.round((selectedProcess+.5)*getScaleY())-10-topLeftPixelY);
		event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		event.gc.fillRectangle(topPixelCrossHairX,topPixelCrossHairY+8,20,4);
		event.gc.fillRectangle(topPixelCrossHairX+8,topPixelCrossHairY,4,20);
		System.gc();
		adjustLabels();
	}

	/*************************************************************************
	 * Asserts the process bounds to make sure they're within the actual
	 * bounds of the database, are integers, and adjusts the process zoom 
	 * button accordingly.
	 *************************************************************************/
	private void assertProcessBounds()
	{
		begProcess = (int)begProcess;
		endProcess = Math.ceil(endProcess);
		
		if (begProcess < 0)
			begProcess = 0;
		if (endProcess > stData.getHeight())
			endProcess = stData.getHeight();
		
		numProcessDisp = endProcess-begProcess;
	}
	
	/**************************************************************************
	 * Asserts the time bounds to make sure they're within the actual
	 * bounds of the database and adjusts the time zoom button accordingly.
	 *************************************************************************/
	private void assertTimeBounds()
	{
		if (begTime < 0)
			begTime = 0;
		if (endTime > stData.getWidth())
			endTime = stData.getWidth();
		
		numTimeUnitsDisp = endTime-begTime;
	}
	
	/**************************************************************************
	 * Initializes the buttons above the detail canvas.
	 **************************************************************************/
	public void setButtons(Action[] toolItems)
	{
		homeButton = toolItems[0];
		openButton = toolItems[1];
		saveButton = toolItems[2];
		undoButton = toolItems[3];
		redoButton = toolItems[4];
		tZoomInButton = toolItems[5];
		tZoomOutButton = toolItems[6];
		pZoomInButton = toolItems[7];
		pZoomOutButton = toolItems[8];
		
		goEastButton = toolItems[9];
		goNorthButton = toolItems[10];
		goSouthButton = toolItems[11];
		goWestButton = toolItems[12];
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'home' button is pressed - 
	 * the bounds are reset so that the viewer is zoomed all the way out on the
	 * image.
	 **************************************************************************/
	public void home()
	{
		pushUndo();
		
		//if this is the first time painting,
		//some stuff needs to get initialized
		topLeftPixelX = 0;
		topLeftPixelY = 0;
		
		begTime = 0;
		endTime = stData.getWidth();
		
		begProcess = 0;
		endProcess = stData.getHeight();

		viewWidth = this.getClientArea().width;
		viewHeight = this.getClientArea().height;
		if (viewWidth <= 0)
			viewWidth = 1;
		if (viewHeight <= 0)
			viewHeight = 1;
		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);

		setDetailZoom(0, 0, stData.getWidth(), stData.getHeight());
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'open' button is pressed - 
	 * sets everything to the data stored in the Frame toBeOpened.
	 **************************************************************************/
	public void open(Frame toBeOpened)
	{
		pushUndo();
		setFrame(toBeOpened);
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'save' button is pressed - 
	 * it stores all the relevant data to this current configuration to a new 
	 * Frame.
	 **************************************************************************/
	public Frame save()
	{
		long selectedTime = stData.getPosition().time;
		int selectedProcess = stData.getPosition().process;

		return new Frame(begTime, endTime, begProcess, endProcess, stData.getDepth(), selectedTime, selectedProcess);
	}
	
	/**************************************************************************
	 * Whenever something happens that the user might want to undo at some point,
	 * the relevant data to the current configuration gets stored in a new frame
	 * that then gets pushed onto the undo stack. The undo and redo buttons then
	 * get adjusted accordingly.
	 **************************************************************************/
	public void pushUndo()
	{
		redoStack.clear();
		redoStack = new Stack<Frame>();
		redoButton.setEnabled(false);
		long selectedTime = stData.getPosition().time;
		int selectedProcess = stData.getPosition().process;
		undoStack.push(new Frame(begTime,endTime,begProcess,endProcess,stData.getDepth(),selectedTime,selectedProcess));
		undoButton.setEnabled(true);
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'undo' button is pressed - 
	 * pops a Frame from the undo stack and sets everything to the data stored
	 * in that Frame, then pushes that Frame onto the redo stack.
	 **************************************************************************/
	public void popUndo()
	{
		Frame nextFrame = undoStack.pop();
		long selectedTime = stData.getPosition().time;
		int selectedProcess = stData.getPosition().process;
		Frame currentFrame = new Frame(begTime,endTime,begProcess,endProcess,stData.getDepth(),selectedTime,selectedProcess);
		redoStack.push(currentFrame);
		redoButton.setEnabled(true);
		if (undoStack.isEmpty()) undoButton.setEnabled(false);
		setFrame(nextFrame);
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'redo' button is pressed - 
	 * pops a Frame from the redo stack and sets everything to the data stored
	 * in that Frame, then pushes that Frame onto the undo stack.
	 **************************************************************************/
	public void popRedo()
	{
		Frame nextFrame = redoStack.pop();
		long selectedTime = stData.getPosition().time;
		int selectedProcess = stData.getPosition().process;
		Frame currentFrame = new Frame(begTime,endTime,begProcess,endProcess,stData.getDepth(),selectedTime,selectedProcess);
		undoStack.push(currentFrame);
		undoButton.setEnabled(true);
		if (redoStack.isEmpty()) redoButton.setEnabled(false);
		setFrame(nextFrame);
	}
	
	
	/**************************************************************************
	 * Sets everything to the data stored in the Frame 'current.'
	 **************************************************************************/
	public void setFrame(Frame current)
	{
		if (current.begTime == stData.getViewTimeBegin() && current.endTime == stData.getViewTimeEnd() 
				&& current.begProcess == stData.getBegProcess() && current.endProcess == stData.getEndProcess()) {
			
		} else {
			setDetailZoom(current.begTime, current.begProcess, current.endTime, current.endProcess);	
			return;
		}
		
		if (current.depth != stData.getDepth()) {
			// we have change of depth
			stData.updateDepth(current.depth, this);
		}
		
		if (!current.position.isEqual(stData.getPosition())) {
			stData.updatePosition(current.position);
		}
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'process zoom in' button is pressed - 
	 * zooms in processwise with a scale of .4.
	 **************************************************************************/
	public void processZoomIn()
	{
		pushUndo();
		final double SCALE = .4;
		
		double yMid = (endProcess+begProcess)/2.0;
		
		double p2 = Math.ceil( yMid+numProcessDisp*SCALE );
		double p1 = Math.floor( yMid-numProcessDisp*SCALE );
		
		assertProcessBounds();
		
		if(p2 == endProcess && p1 == begProcess)
		{
			if(numProcessDisp == 2)
				p2--;
			else if(numProcessDisp > 2)
			{
				p2--;
				p1++;
			}
		}
		
		this.setDetailZoom(this.begTime, p1, this.endTime, p2);
	}

	/**************************************************************************
	 * The action that gets performed when the 'process zoom out' button is pressed - 
	 * zooms out processwise with a scale of .625.
	 **************************************************************************/
	public void processZoomOut()
	{
		pushUndo();
		final double SCALE = .625;
		
		//zoom out works as follows: find mid point of times (yMid).
		//Add/Subtract 1/2 of the scaled numProcessDisp to yMid to get new endProcess and begProcess
		double yMid = (endProcess+begProcess)/2.0;
		
		double p2 = Math.ceil( yMid+numProcessDisp*SCALE );
		double p1 = Math.floor( yMid-numProcessDisp*SCALE );
		
		this.setDetailZoom(begTime, p1, endTime, p2);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'time zoom in' button is pressed - 
	 * zooms in timewise with a scale of .4.
	 **************************************************************************/
	public void timeZoomIn()
	{
		pushUndo();
		final double SCALE = .4;
		
		long xMid = (endTime + begTime) / 2;
		
		long t2 = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		long t1 = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		this.setDetailZoom(t1, this.begProcess, t2, this.endProcess);
	}

	/**************************************************************************
	 * The action that gets performed when the 'time zoom out' button is pressed - 
	 * zooms out timewise with a scale of .625.
	 **************************************************************************/
	public void timeZoomOut()
	{
		pushUndo();
		final double SCALE = 0.625;
		
		//zoom out works as follows: find mid point of times (xMid).
		//Add/Subtract 1/2 of the scaled numTimeUnitsDisp to xMid to get new endTime and begTime
		long xMid = (endTime + begTime) / 2;
		
		long t2 = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		long t1 = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		this.setDetailZoom(t1, this.begProcess, t2, this.endProcess);
	}
	
	/**************************************************************************
	 * Gets the scale along the x-axis (pixels per time unit).
	 **************************************************************************/
	public double getScaleX()
	{
		return (double)viewWidth / (double)numTimeUnitsDisp;
	}
	
	/**************************************************************************
	 * Gets the scale along the y-axis (pixels per process).
	 **************************************************************************/
	public double getScaleY()
	{
		return (double)viewHeight / numProcessDisp;
	}
	
	/**************************************************************************
	 * Sets the depth to newDepth.
	 **************************************************************************/
	public void setDepth(int newDepth)
	{
		stData.setDepth(newDepth);
		rebuffer();
    }
	
	/**************************************************************************
	 * Sets the location of the crosshair to (_selectedTime, _selectedProcess).
	 * Also updates the rest of the program to know that this is the selected
	 * point (so that the CallStackViewer can update, etc.).
	 **************************************************************************/
	public void setCrossHair(long _selectedTime, int _selectedProcess)
	{
		redraw();
	}
	
	/**************************************************************************
	 * Sets up the labels (the ones below the detail canvas).
	 **************************************************************************/
	public void setLabels(Composite _labelGroup)
    {
		labelGroup = _labelGroup;
        
        timeLabel = new Label(labelGroup, SWT.LEFT);
        processLabel = new Label(labelGroup, SWT.CENTER);
        crossHairLabel = new Label(labelGroup, SWT.RIGHT);
    }
   
	/**************************************************************************
	 * Updates what the labels display to the viewer's current state.
	 **************************************************************************/
	private void adjustLabels()
    {
        timeLabel.setText("Time Range: [" + ((long)(begTime/1000))/1000.0 + "s ," + ((long)endTime/1000)/1000.0 +  "s]");
        timeLabel.setSize(timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        final BaseDataFile traceData = this.stData.getTraceData();
        
        final String processes[] = traceData.getValuesX();
        processLabel.setText("Process Range: [" + processes[0] + "," + processes[processes.length-1]+"]");
        processLabel.setSize(processLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        if(stData == null)
            crossHairLabel.setText("Select Sample For Cross Hair");
        else
        {
    		long selectedTime = stData.getPosition().time;
    		String selectedProcessLabel = "";
    		
            if (traceData.isMultiProcess())
            {
            	selectedProcessLabel = String.valueOf(stData.depthTrace.processID);
            	
            } else if (traceData.isMultiThreading())
            {
            	selectedProcessLabel = String.valueOf(stData.depthTrace.threadID);
            	
            } else if (traceData.isHybrid())
            {
            	selectedProcessLabel = stData.depthTrace.processID+"."+stData.depthTrace.threadID;
            	
            } else 
            {
            	System.out.println("PANIC - DATATYPE IS NOT AVAILABLE!");
            }
            
        	crossHairLabel.setText("Cross Hair: (" + ((long)(selectedTime/1000))/1000.0 + "s, " + selectedProcessLabel + ")");
        }
        
        labelGroup.setSize(labelGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
	
    /**************************************************************************
	 * Updates what the position of the selected box is.
	 **************************************************************************/
    private void adjustSelection(Point p1, Point p2)
	{
    	selectionTopLeftX = topLeftPixelX + Math.max(Math.min(p1.x, p2.x), 0);
        selectionTopLeftY = topLeftPixelY + Math.max(Math.min(p1.y, p2.y), 0);
        
        selectionBottomRightX = topLeftPixelX + Math.min(Math.max(p1.x, p2.x), viewWidth-1);
        selectionBottomRightY = topLeftPixelY + Math.min(Math.max(p1.y, p2.y), viewHeight-1);
    }
    
	/**************************************************************************
	 * create a new region of trace view, and check if the cross hair is inside
	 * 	the new region or not. If this is not the case, we force the position
	 * 	of crosshair to be inside the region 
	 **************************************************************************/
	private void setDetail()
    {
    	double topLeftProcess = (selectionTopLeftY / getScaleY());
		long topLeftTime = (long)((double)selectionTopLeftX / getScaleX());
		double bottomRightProcess = (selectionBottomRightY / getScaleY());
		long bottomRightTime = (long)((double)selectionBottomRightX / getScaleX());
		setDetailZoom(topLeftTime, topLeftProcess, bottomRightTime, bottomRightProcess);
    }
    
    /******
     * If necessary adjust the position of cross hair with the view region
     * If the cross hair is outside the region, we force to position it within the region
     * 	in order to avoid exposed bugs and confusion that the depth and the call path are not
     * 	consistent with the trace view
     * 
     * @param t1
     * @param p1
     * @param t2
     * @param p2
     */
    private void adjustCrossHair(long t1, double p1, long t2, double p2) {
    	
    	Position current = this.stData.getPosition();
    	int process = (current.process);
    	long time = current.time;
    	
    	if (process < p1 || process > p2) {
    		process = (int) ((long) p1+p2) >> 1;
		
			// if the new location is bigger than the max proc, set it to the min proc
			// this situation only happens when there is only 1 proc to display
			if (process >= (int)p2)
				process = (int)p1;
    	}
    	if (time < t1 || time > t2) {
    		time = (t1+t2)>>1;
    	}
    	Position newPosition = new Position(time,process);
    	newPosition = this.getAdjustedProcess(newPosition);
    	
    	// tell other views that we have new position 
    	this.stData.updatePosition(newPosition);
    }

    /****
     * Adjust the position of cross hair depending of the availability of traces
     * 
     * @param position
     * @return adjusted position
     */
    private Position getAdjustedProcess(Position position) {
    	
    	double numDisplayedProcess = stData.getNumberOfDisplayedProcesses();
    	int estimatedProcess = (int) (position.process - this.begProcess);			
    	double scaleProcess = numDisplayedProcess/(double)this.numProcessDisp;
    	
    	//---------------------------------------------------------------------------------------
    	// computing the relative process rank: 
    	//	the relative rank is adjusted based on the number of displayed process
    	//	for instance, if the mouse click computes that the position of process rank is 100
    	//		from range 50 to 500 (so the range is 450), but the number of displayed process
    	//		is only 200, then we need to adjust the relative position of the process into
    	//		200/450 * (100-50)
    	//---------------------------------------------------------------------------------------
    	int relativeProcess = (int) (scaleProcess * estimatedProcess);
    	
    	// generalization of case where there is only one single process to display
    	if (relativeProcess>=numDisplayedProcess)
    		relativeProcess = (int) (numDisplayedProcess - 1);
    	
    	position.processInCS = relativeProcess;
    	if (estimatedProcess != relativeProcess) {
        	//---------------------------------------------------------------------------------------
        	// if there is any change between the estimated process by mouse click and the
    		//	estimated process by the array of displayed process, we need to adjust
    		//	the absolute process
        	//---------------------------------------------------------------------------------------
        	position.process = (int) (relativeProcess/scaleProcess + this.begProcess);
    	}
    	return position;
    	
    }
    
    private boolean canGoEast() {
    	return (this.begTime > 0);
    }
    
    private boolean canGoWest() {
    	return (this.endTime< this.stData.getWidth());
    }
    
    private boolean canGoNorth() {
    	return (this.begProcess>0);
    }
    
    private boolean canGoSouth() {
    	return (this.endProcess<this.stData.getHeight());
    }
    /**********
     * check the status of all buttons
     */
    private void updateButtonStates() {
    	
		this.undoButton.setEnabled( this.undoStack.size()>0 );
		this.redoButton.setEnabled( this.redoStack.size()>0 );
		
		this.tZoomInButton.setEnabled( numTimeUnitsDisp > Constants.MIN_TIME_UNITS_DISP );
		this.tZoomOutButton.setEnabled(this.begTime>0 || this.endTime<stData.getWidth() );
		
		this.pZoomInButton.setEnabled( numProcessDisp > MIN_PROC_DISP );
		this.pZoomOutButton.setEnabled( this.begProcess>0 || this.endProcess<stData.getHeight());
		
		this.goEastButton.setEnabled( canGoEast() );
		this.goWestButton.setEnabled( canGoWest() );
		this.goNorthButton.setEnabled( canGoNorth() );
		this.goSouthButton.setEnabled( canGoSouth() );
		
		homeButton.setEnabled( begTime>0 || endTime<stData.getWidth() || begProcess>0 || endProcess<stData.getHeight() );

    }
    
	final static private double SCALE_MOVE = 0.20;
    
	/***
	 * go to the left one step
	 */
    public void goEast()
    {
    	long topLeftTime = stData.getViewTimeBegin();
		long bottomRightTime = stData.getViewTimeEnd();
		
		long deltaTime = bottomRightTime - topLeftTime;
		final long moveTime = (long)java.lang.Math.ceil(deltaTime * SCALE_MOVE);
		topLeftTime = topLeftTime - moveTime;
		
		if (topLeftTime < 0) {
			topLeftTime = 0;
		}
		bottomRightTime = topLeftTime + deltaTime;
		
		setTimeRange(topLeftTime, bottomRightTime);
		
		updateButtonStates();
    }
    
    /***
     * go to the right one step
     */
    public void goWest()
    {
    	long topLeftTime = stData.getViewTimeBegin();
		long bottomRightTime = stData.getViewTimeEnd();
		
		long deltaTime = bottomRightTime - topLeftTime;
		final long moveTime = (long)java.lang.Math.ceil(deltaTime * SCALE_MOVE);
		bottomRightTime = bottomRightTime + moveTime;
		
		if (bottomRightTime > stData.getWidth()) {
			bottomRightTime = stData.getWidth();
		}
		topLeftTime = bottomRightTime - deltaTime;
		
		setTimeRange(topLeftTime, bottomRightTime);
		
		this.updateButtonStates();
    }
    
    /***
     * set a new range of X-axis
     * @param topLeftTime
     * @param bottomRightTime
     */
    public void setTimeRange(long topLeftTime, long bottomRightTime)
    {
    	pushUndo();
    	setDetailZoom(topLeftTime, begProcess, bottomRightTime, endProcess);
    }

    /*******
     * go north one step
     */
    public void goNorth() {
    	double proc_begin = stData.getBegProcess();
    	double proc_end = stData.getEndProcess();
    	final double delta = proc_end - proc_begin;
    	final double move = java.lang.Math.ceil((double)delta * SCALE_MOVE);
    	proc_begin = proc_begin - move;
    	
    	if (proc_begin < 0) {
    		proc_begin = 0;
    	}
    	proc_end = proc_begin + delta;
    	this.setProcessRange(proc_begin, proc_end);
		this.updateButtonStates();
    }

    /*******
     * go south one step
     */
    public void goSouth() {
    	double proc_begin = stData.getBegProcess();
    	double proc_end = stData.getEndProcess();
    	final double delta = proc_end - proc_begin;
    	final double move = java.lang.Math.ceil((double)delta * SCALE_MOVE);
    	proc_end = proc_end + move;
    	
    	if (proc_end > stData.getHeight()) {
    		proc_end = stData.getHeight();
    	}
    	proc_begin = proc_end - delta;
    	this.setProcessRange(proc_begin, proc_end);
		this.updateButtonStates();
    }
    
    /***
     * set a new range for Y-axis
     * @param pBegin: the top position
     * @param pEnd: the bottom position
     */
	private void setProcessRange(double pBegin, double pEnd) {
		pushUndo();
		this.setDetailZoom(stData.getViewTimeBegin(), pBegin, stData.getViewTimeEnd(), pEnd);
	}

	private void setCSSample()
    {
    	if(mouseDown == null)
    		return;
    	int selectedProcess;
    	int procIndex;

    	//need to do different things if there are more traces to paint than pixels
    	if(viewHeight > endProcess-begProcess)
    	{
    		selectedProcess = (int)(begProcess+mouseDown.y/getScaleY());
    		procIndex = (int)(mouseDown.y/getScaleY());
    	}
    	else
    	{
    		selectedProcess = (int)(begProcess+(mouseDown.y*(endProcess-begProcess))/viewHeight);
    		procIndex = mouseDown.y;
    	}
    	long closeTime = begTime + (long)((double)mouseDown.x / getScaleX());
    	if (closeTime > endTime) {
    		System.err.println("ERR STDC SCSSample time: " + closeTime +" max time: " + endTime + "\t new: " + ((begTime + endTime) >> 1));
    		closeTime = (begTime + endTime) >> 1;
    		
    	}
    	
    	Position position = new Position(closeTime, selectedProcess);
    	position.processInCS = procIndex;
    	
    	this.stData.updatePosition(position);
    }

	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 * *****************************************************************/

	public void mouseDoubleClick(MouseEvent e) { }

	public void mouseDown(MouseEvent e)
	{
		// take into account ONLY when the button-1 is clicked and it's never been clicked before
		if (e.button == 1 && mouseState == MouseState.ST_MOUSE_NONE)
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
		}
	}

	public void mouseUp(MouseEvent e)
	{
		if (mouseState == MouseState.ST_MOUSE_DOWN)
		{
			mouseUp = new Point(e.x,e.y);
			mouseState = MouseState.ST_MOUSE_NONE;
			//difference in mouse movement < 3 constitutes a "single click"
			if(Math.abs(mouseUp.x-mouseDown.x)<3 && Math.abs(mouseUp.y-mouseDown.y)<3)
			{
				setCSSample();
			}
			else
			{
				//If we're zoomed in all the way don't do anything
				if(numTimeUnitsDisp == Constants.MIN_TIME_UNITS_DISP)
				{
					if(numProcessDisp > MIN_PROC_DISP)
					{
						mouseDown.x = 0;
						mouseUp.x = viewWidth;
						pushUndo();
						adjustSelection(mouseDown,mouseUp);
						setDetail();
					}
				}
				else
				{
					pushUndo();
					adjustSelection(mouseDown,mouseUp);
					setDetail();
				}
				redraw();
			}
			//don't draw the selection if you're not selecting anything
			//adjustSelection(new Point(-1,-1),new Point(-1,-1));
		}
	}
	
	public void mouseMove(MouseEvent e)
	{
		if(mouseState == MouseState.ST_MOUSE_DOWN)
		{
			Point mouseTemp = new Point(e.x,e.y);
			adjustSelection(mouseDown,mouseTemp);
			redraw();
		}
	}
	
	public void setMiniCanvas(SpaceTimeMiniCanvas _miniCanvas)
	{
		miniCanvas = _miniCanvas;
	}
	
	public void setDepthCanvas(DepthTimeCanvas _depthCanvas)
	{
		depthCanvas = _depthCanvas;
	}
	
	public void setSummaryCanvas(SummaryTimeCanvas _summaryCanvas)
	{
		summaryCanvas = _summaryCanvas;
	}

	/***********************************************************************************
	 * Forcing to create image buffer
	 * Attention: this method will take some time to generate an image buffer, so
	 * 	please do not call this if not necessary
	 ***********************************************************************************/
	public void rebuffer() {
		//Debugger.printTrace("STDC rebuffer");
		//Okay, so here's how this works. In order to draw to an Image (the Eclipse kind)
		//you need to draw to its GC. So, we have this bufferImage that we draw to, so
		//we get its GC (bufferGC), and then pass that GC to paintViewport, which draws
		//everything to it. Then the image is copied to the canvas on the screen with that
		//event.gc.drawImage call down there below the 'if' block - this is called "double buffering," 
		//and it's useful because it prevent the screen from flickering (if you draw directly then
		//you would see each sample as it was getting drawn very quickly, which you would
		//interpret as flickering. This way, you finish the puzzle before you put it on the
		//table).

		if (viewWidth==0 && viewHeight==0) {
			viewWidth = this.getClientArea().width;
			viewHeight = this.getClientArea().height;
		}
		
		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		GC bufferGC = new GC(imageBuffer);
		bufferGC.setBackground(Constants.COLOR_WHITE);
		bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
		stData.paintDetailViewport(bufferGC, this, (int)begProcess, (int)Math.ceil(endProcess), begTime, endTime, viewWidth, viewHeight);
		
		bufferGC.dispose();
		redraw();
		
		// forces all other views to refresh with the new region
		depthCanvas.refresh(begTime, endTime);
		miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
		if (summaryCanvas != null)	
			summaryCanvas.refresh(imageBuffer.getImageData());
	}
}