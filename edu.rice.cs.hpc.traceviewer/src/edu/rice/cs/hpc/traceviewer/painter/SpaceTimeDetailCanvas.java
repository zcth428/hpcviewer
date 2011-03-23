package edu.rice.cs.hpc.traceviewer.painter;

import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.ui.CallStackViewer;
import edu.rice.cs.hpc.traceviewer.ui.Frame;

/*************************************************************************
 * 
 *	Canvas onto which the detail view is painted. Also takes care of
 *	zooming responsibilities of the detail view.
 *
 ************************************************************************/
public class SpaceTimeDetailCanvas extends SpaceTimeCanvas implements MouseListener, MouseMoveListener, PaintListener
{
	
	private static final long serialVersionUID = 1L;
	
	/**The buffer image that is copied onto the actual canvas.*/
	Image imageBuffer;
	
	/** Stores whether the Detail Panel has changed screens from the first frame or not.*/
	boolean homeScreen;
	
	/** Stores whether or not the Detail Panel's background has just changed or not (used to determine when to rebuffer).*/
	boolean rebuffer;
	
	/** The CallStackViewer that is controlled by this SpaceTimeDetailCanvas.*/
	public CallStackViewer csViewer;
	
	/** The number of time units being displayed on the Detail View.*/
	long numTimeUnitsDisp;
	
	/** The number of processes being displayed on the Detail View.*/
	double numProcessDisp;
	
	/**Triggers zoom back to beginning view screen.*/
	ToolItem homeButton;
	
	/**Triggers open function to open previously saved frame.*/
	ToolItem openButton;
	
	/**Triggers save function to save current frame to file.*/
	ToolItem saveButton;
	
	/**Triggers undo of screen.*/
	ToolItem undoButton;
	
	/**Triggers screen re-do.*/
	ToolItem redoButton;
	
	/** Triggers zoom-in on the time axis.*/
	ToolItem tZoomInButton;
	
	/** Triggers zoom-out on the time axis.*/
	ToolItem tZoomOutButton;
	
	/** Triggers zoom-in on the process axis.*/
	ToolItem pZoomInButton;
	
	/** Triggers zoom-out on the process axis.*/
	ToolItem pZoomOutButton;

	ToolItem goEastButton, goNorthButton, goWestButton, goSouthButton;
	
	/** The SpaceTimeMiniCanvas that is changed by the detailCanvas.*/
	SpaceTimeMiniCanvas miniCanvas;
	
	DepthTimeCanvas depthCanvas = null;
	
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
    
    /**The SWT Color for white.*/
    public static Color white;
    /**The SWT Color for black.*/
    public static Color black;
    
    /**The min number of time units you can zoom in.*/
    private final static long MIN_TIME_UNITS_DISP = 50000;
    
    /**The min number of process units you can zoom in.*/
    private final static int MIN_PROC_DISP = 1;
	
    /**Creates a SpaceTimeDetailCanvas with the given parameters*/
	public SpaceTimeDetailCanvas(Composite _composite)
	{
		super(_composite );
		
		homeScreen = true;
		rebuffer = true;
		undoStack = new Stack<Frame>();
		redoStack = new Stack<Frame>();
		mouseState = MouseState.ST_MOUSE_INIT;

		selectionTopLeftX = 0;
		selectionTopLeftY = 0;
		selectionBottomRightX = 0;
		selectionBottomRightY = 0;
		
		white = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
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
		
		if (mouseState == MouseState.ST_MOUSE_INIT) {
			mouseState = MouseState.ST_MOUSE_NONE;
			this.addCanvasListener();
		}
		
		this.home();
		this.setDepth(0);

		// clear undo button
		this.undoStack.clear();
		this.undoButton.setEnabled(false);
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

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

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
		
		//A listener for resizing the the window.		
		//FIXME: Every time the window is resized just a tiny bit, the program rebuffers
		//(goes out and gets the data again). It would be better to have a listener for
		//when the user stops resizing before rebuffering or something...
		addListener(SWT.Resize, new Listener(){
			public void handleEvent(Event event)
			{
				viewWidth = getClientArea().width;
				viewHeight = getClientArea().height;

				if(homeScreen)
					imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
				
				if(viewWidth > 0 && viewHeight > 0)
					rebuffer = true;

				assertProcessBounds();
				assertTimeBounds();
			}
		});
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
		
		if (numTimeUnitsDisp < MIN_TIME_UNITS_DISP)
		{
			begTime += (numTimeUnitsDisp - MIN_TIME_UNITS_DISP) / 2;
			numTimeUnitsDisp = MIN_TIME_UNITS_DISP;
			endTime = begTime + numTimeUnitsDisp;
		}
		
		if (numProcessDisp < MIN_PROC_DISP)
		{
			numProcessDisp = MIN_PROC_DISP;
			begProcess = (int)begProcess;
			endProcess = begProcess+numProcessDisp;
		}
		
		this.updateButtonStates();
		redraw();
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
		
		if(homeScreen)
		{
			//if this is the first time painting,
			//some stuff needs to get initialized
			topLeftPixelX = 0;
			topLeftPixelY = 0;
			
			begTime = 0;
			endTime = stData.getWidth();
			
			begProcess = 0;
			endProcess = stData.getHeight();
			setDetailZoom(begTime, begProcess, endTime, endProcess);
			viewWidth = this.getClientArea().width;
			viewHeight = this.getClientArea().height;
			if (viewWidth <= 0)
				viewWidth = 1;
			if (viewHeight <= 0)
				viewHeight = 1;
			imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
			homeScreen = false;
		}
		
		topLeftPixelX = Math.round(begTime * getScaleX());
		topLeftPixelY = Math.round(begProcess * getScaleY());
		miniCanvas.setBox(begTime, begProcess, endTime, endProcess);

		depthCanvas.setTimeRange(begTime, endTime);
		
		//if something has changed the bounds, you need to go get the data again
		if (rebuffer)
		{
			//Okay, so here's how this works. In order to draw to an Image (the Eclipse kind)
			//you need to draw to its GC. So, we have this bufferImage that we draw to, so
			//we get its GC (bufferGC), and then pass that GC to paintViewport, which draws
			//everything to it. Then the image is copied to the canvas on the screen with that
			//event.gc.drawImage call down there below the 'if' block - this is called "double buffering," 
			//and it's useful because it prevent the screen from flickering (if you draw directly then
			//you would see each sample as it was getting drawn very quickly, which you would
			//interpret as flickering. This way, you finish the puzzle before you put it on the
			//table).
			
			imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
			GC bufferGC = new GC(imageBuffer);
			bufferGC.setBackground(white);
			bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
			stData.paintDetailViewport(bufferGC, this, this.stData.getDepth(), 
					(int)begProcess, (int)Math.ceil(endProcess), begTime, endTime, viewWidth, viewHeight);
			
			bufferGC.dispose();
			rebuffer = false;
		}
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
	public void setButtons(ToolItem[] toolItems)
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
		rebuffer = true;
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
		rebuffer = true;
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
		rebuffer = true;
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
		}
		
		if (current.depth != stData.getDepth()) {
			// we have change of depth
			stData.updateDepth(current.depth);
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
		
		double oldEnd = endProcess;
		double oldBeg = begProcess;
		
		endProcess = yMid+numProcessDisp*SCALE;
		begProcess = yMid-numProcessDisp*SCALE;
		
		assertProcessBounds();
		
		if(oldEnd == endProcess && oldBeg == begProcess)
		{
			if(numProcessDisp == 2)
				endProcess--;
			else if(numProcessDisp > 2)
			{
				endProcess--;
				begProcess++;
			}
		}
		
		assertProcessBounds();
		
		if (numProcessDisp<=MIN_PROC_DISP)
		{
			numProcessDisp = MIN_PROC_DISP;
			begProcess = (int)begProcess;
			endProcess = begProcess+MIN_PROC_DISP;
		}
		this.updateButtonStates();
		
		redraw();
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
		
		endProcess = yMid+numProcessDisp*SCALE;
		begProcess = yMid-numProcessDisp*SCALE;
		
		assertProcessBounds();
		
		if(numProcessDisp <= MIN_PROC_DISP)
		{
			numProcessDisp = MIN_PROC_DISP;
		}
		this.updateButtonStates();
		redraw();
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
		
		endTime = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		begTime = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		assertTimeBounds();
		
		if(numTimeUnitsDisp < MIN_TIME_UNITS_DISP)
		{
			numTimeUnitsDisp = MIN_TIME_UNITS_DISP;
		}
		
		redraw();
		
		this.updateButtonStates();
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
		
		endTime = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		begTime = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		assertTimeBounds();
		
		redraw();
		this.updateButtonStates();
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
		rebuffer = true;
		redraw();
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
    public void adjustLabels()
    {
        timeLabel.setText("Time Range: " + ((long)(begTime/1000))/1000.0 + "s |" + ((long)endTime/1000)/1000.0 +  "s");
        timeLabel.setSize(timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
       
        processLabel.setText("Process Range: " + ((long)(begProcess*1000))/1000.0 + "|"+((long)(endProcess*1000))/1000.0);
        processLabel.setSize(processLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        if(stData == null)
            crossHairLabel.setText("Select Sample For Cross Hair");
        else {
    		long selectedTime = stData.getPosition().time;
    		int selectedProcess = stData.getPosition().process;
        	crossHairLabel.setText("Cross Hair: (" + ((long)(selectedTime/1000))/1000.0 + "s, " + selectedProcess + ")");
        }
        
        labelGroup.setSize(labelGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
	
    /**************************************************************************
	 * Updates what the position of the selected box is.
	 **************************************************************************/
	public void adjustSelection(Point p1, Point p2)
	{
    	selectionTopLeftX = topLeftPixelX + Math.max(Math.min(p1.x, p2.x), 0);
        selectionTopLeftY = topLeftPixelY + Math.max(Math.min(p1.y, p2.y), 0);
        
        selectionBottomRightX = topLeftPixelX + Math.min(Math.max(p1.x, p2.x), viewWidth-1);
        selectionBottomRightY = topLeftPixelY + Math.min(Math.max(p1.y, p2.y), viewHeight-1);
    }
    
    public void setDetail()
    {
    	double topLeftProcess = (selectionTopLeftY / getScaleY());
		long topLeftTime = (long)((double)selectionTopLeftX / getScaleX());
		double bottomRightProcess = (selectionBottomRightY / getScaleY());
		long bottomRightTime = (long)((double)selectionBottomRightX / getScaleX());
		setDetailZoom(topLeftTime, topLeftProcess, bottomRightTime, bottomRightProcess);
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
		
		this.tZoomInButton.setEnabled( numTimeUnitsDisp > MIN_TIME_UNITS_DISP );
		this.tZoomOutButton.setEnabled(this.begTime>0 || this.endTime<stData.getWidth() );
		
		this.pZoomInButton.setEnabled( numProcessDisp > MIN_PROC_DISP );
		this.pZoomOutButton.setEnabled( this.begProcess>0 || this.endProcess<stData.getHeight());
		
		this.goEastButton.setEnabled( canGoEast() );
		this.goWestButton.setEnabled( canGoWest() );
		this.goNorthButton.setEnabled( canGoNorth() );
		this.goSouthButton.setEnabled( canGoSouth() );
		
		homeButton.setEnabled( begTime>0 || endTime<stData.getWidth() || 
					begProcess>0 || endProcess<stData.getHeight() );

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

    public void setCSSample()
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
		if (mouseState == MouseState.ST_MOUSE_NONE)
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
				if(numTimeUnitsDisp == MIN_TIME_UNITS_DISP)
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
			adjustSelection(new Point(-1,-1),new Point(-1,-1));
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
	/*
	public int getDepth()
	{
		return depth;
	}*/
	
	public void setMiniCanvas(SpaceTimeMiniCanvas _miniCanvas)
	{
		miniCanvas = _miniCanvas;
	}
	
	public void setDepthCanvas(DepthTimeCanvas _depthCanvas)
	{
		depthCanvas = _depthCanvas;
	}
	
}