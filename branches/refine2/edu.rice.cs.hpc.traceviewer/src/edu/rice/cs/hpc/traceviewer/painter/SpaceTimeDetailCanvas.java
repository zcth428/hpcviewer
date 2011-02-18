package edu.rice.cs.hpc.traceviewer.painter;

import java.util.Stack;

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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

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
	
	/**The selected time that is open in the csViewer.*/
	long selectedTime;
	
	/**The selected process that is open in the csViewer*/
	public int selectedProcess;
	
	/**The current depth that is selected for this canvas.*/
    int depth = 0;
    
	/**Triggers zoom back to beginning view screen.*/
	Button homeButton;
	
	/**Triggers open function to open previously saved frame.*/
	Button openButton;
	
	/**Triggers save function to save current frame to file.*/
	Button saveButton;
	
	/**Triggers undo of screen.*/
	Button undoButton;
	
	/**Triggers screen re-do.*/
	Button redoButton;
	
	/** Triggers zoom-in on the time axis.*/
	Button tZoomInButton;
	
	/** Triggers zoom-out on the time axis.*/
	Button tZoomOutButton;
	
	/** Triggers zoom-in on the process axis.*/
	Button pZoomInButton;
	
	/** Triggers zoom-out on the process axis.*/
	Button pZoomOutButton;
	
	/** The horizontal scroll bar of this Canvas.*/
	private ScrollBar hBar;
	
	/**The vertical scroll bar of this Canvas.*/
	private ScrollBar vBar;
	
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
    Label timeLabel;
   
    /**The Label with the process boundaries.*/
    Label processLabel;
    
    /**The Label with the current cross hair information.*/
    Label crossHairLabel;
    
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
		selectedTime = -20;
		selectedProcess = -1;
		selectionTopLeftX = 0;
		selectionTopLeftY = 0;
		selectionBottomRightX = 0;
		selectionBottomRightY = 0;
		
		white = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		//vertical scrollbar
		vBar = this.getVerticalBar();
		vBar.setMinimum(0);
		
		//horizontal scrollbar
		hBar = this.getHorizontalBar();
		hBar.setMinimum(0);
		
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

		vBar.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event)
			{
				int sel = vBar.getSelection();
				int thumb = vBar.getThumb();
				if((sel>endProcess-.5*numProcessDisp) || (sel<begProcess-.5*numProcessDisp))
				{
					pushUndo();
					begProcess = sel;
					endProcess = begProcess+numProcessDisp;
					assertProcessBounds();
					miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
					redraw();
				}
				else
				{
					if(sel+thumb>=vBar.getMaximum())
					{
						pushUndo();
						begProcess = vBar.getMaximum()-thumb;
						endProcess = vBar.getMaximum();
						assertProcessBounds();
						miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
						redraw();
					}
					else if (sel<=vBar.getMinimum())
					{
						pushUndo();
						begProcess = vBar.getMinimum();
						endProcess = begProcess+thumb;
						assertProcessBounds();
						miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
						redraw();
					}
				}
			}
		});

		
		hBar.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event)
			{
				int sel = hBar.getSelection();
				int thumb = hBar.getThumb();
				//user clicked outside of the scroll bar
				if ((sel>endTime-.5*numTimeUnitsDisp/* && sel+thumb<=hBar.getMaximum()*/) || (sel<begTime-.5*numTimeUnitsDisp/* && sel>=hBar.getMinimum()*/))
				{
					pushUndo();
					begTime = sel;
					endTime = begTime + numTimeUnitsDisp;
					assertTimeBounds();
					miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
					redraw();
				}
				else
				{
					if (sel+thumb>=hBar.getMaximum())
					{
						pushUndo();
						begTime = hBar.getMaximum()-thumb;
						endTime = hBar.getMaximum();
						assertTimeBounds();
						miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
						redraw();
					}
					else if (sel<=hBar.getMinimum())
					{
						pushUndo();
						begTime = hBar.getMinimum();
						endTime = begTime+thumb;
						assertTimeBounds();
						miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
						redraw();
					}
					
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

				resetHScrollBar();
				resetVScrollBar();
				
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
		
		if (numTimeUnitsDisp > MIN_TIME_UNITS_DISP)
			tZoomInButton.setEnabled(true);
		else if (numTimeUnitsDisp == MIN_TIME_UNITS_DISP)
			tZoomInButton.setEnabled(false);
		else
		{
			tZoomInButton.setEnabled(false);
			begTime += (numTimeUnitsDisp - MIN_TIME_UNITS_DISP) / 2;
			numTimeUnitsDisp = MIN_TIME_UNITS_DISP;
			endTime = begTime + numTimeUnitsDisp;
		}
		
		if (numProcessDisp > MIN_PROC_DISP)
			pZoomInButton.setEnabled(true);
		else if (numProcessDisp == MIN_PROC_DISP)
			pZoomInButton.setEnabled(false);
		else
		{
			numProcessDisp = MIN_PROC_DISP;
			pZoomInButton.setEnabled(false);
			begProcess = (int)begProcess;
			endProcess = begProcess+numProcessDisp;
		}
		
		if (begTime>0 || endTime<stData.getWidth() || begProcess>0 || endProcess<stData.getHeight())
			homeButton.setEnabled(true);
		else
			homeButton.setEnabled(false);
		
		resetHScrollBar();
		resetVScrollBar();
		vBar.setSelection((int)Math.round(begProcess));
		hBar.setSelection((int)Math.round(begTime));
		
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
			imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
			homeButton.setEnabled(false);
			homeScreen = false;
		}
		
		topLeftPixelX = Math.round(begTime * getScaleX());
		topLeftPixelY = Math.round(begProcess * getScaleY());
		miniCanvas.setBox(begTime, begProcess, endTime, endProcess);
		//if (csViewer.csview.depthview.openedView)
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
			stData.paintDetailViewport(bufferGC, this, depth, (int)begProcess, (int)Math.ceil(endProcess), begTime, endTime, viewWidth, viewHeight);
			
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
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScaleX())-10-topLeftPixelX);
		int topPixelCrossHairY = (int)(Math.round((selectedProcess+.5)*getScaleY())-10-topLeftPixelY);
		event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		event.gc.fillRectangle(topPixelCrossHairX,topPixelCrossHairY+8,20,4);
		event.gc.fillRectangle(topPixelCrossHairX+8,topPixelCrossHairY,4,20);
		System.gc();
		adjustLabels();
	}
	
	/*********************************************************************************
	 * Resets the size and position of the horizontal scrollbar to what is should be,
	 * based on the current time bounds.
	 *********************************************************************************/
	//hBar uses actual times as its coordinate system
	private void resetHScrollBar()
	{
		double maxH = stData.getWidth();
		if(maxH <= numTimeUnitsDisp)
		{
			hBar.setVisible(false);
		}
		else
		{
			hBar.setVisible(true);
			hBar.setMaximum((int)Math.ceil(maxH));
			hBar.setThumb((int)Math.round(numTimeUnitsDisp));
		}
		hBar.setIncrement((int)Math.round(.5*numTimeUnitsDisp));
	}
	
	/*********************************************************************************
	 * Resets the size and position of the vertical scrollbar to what is should be,
	 * based on the current process bounds.
	 *********************************************************************************/
	//vBar uses processes as its coordinate system
	private void resetVScrollBar()
	{
		double maxV = stData.getHeight();
		if (maxV <= numProcessDisp)
		{
			vBar.setVisible(false);
		}
		else
		{
			vBar.setVisible(true);
			vBar.setMaximum((int)Math.ceil(maxV));
			vBar.setThumb((int)Math.round(numProcessDisp));
		}
		vBar.setIncrement((int)Math.round(.5*numProcessDisp));
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
		
		if (begProcess==0 && endProcess==stData.getHeight())
			pZoomOutButton.setEnabled(false);
		else
			pZoomOutButton.setEnabled(true);
		
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
		
		if (begTime==0 && endTime==stData.getWidth())
			tZoomOutButton.setEnabled(false);
		else
			tZoomOutButton.setEnabled(true);
		
		numTimeUnitsDisp = endTime-begTime;
	}
	
	/**************************************************************************
	 * Initializes the buttons above the detail canvas.
	 **************************************************************************/
	public void setButtons(Button[] buttons)
	{
		homeButton = buttons[0];
		openButton = buttons[1];
		saveButton = buttons[2];
		undoButton = buttons[3];
		redoButton = buttons[4];
		tZoomInButton = buttons[5];
		tZoomOutButton = buttons[6];
		pZoomInButton = buttons[7];
		pZoomOutButton = buttons[8];
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
		return new Frame(begTime, endTime, begProcess, endProcess, depth, selectedTime, selectedProcess);
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
		undoStack.push(new Frame(begTime,endTime,begProcess,endProcess,depth,selectedTime,selectedProcess));
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
		Frame currentFrame = new Frame(begTime,endTime,begProcess,endProcess,depth,selectedTime,selectedProcess);
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
		Frame currentFrame = new Frame(begTime,endTime,begProcess,endProcess,depth,selectedTime,selectedProcess);
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
		depth = current.depth;
		setDetailZoom(current.begTime, current.begProcess, current.endTime, current.endProcess);
		setCrossHair(current.selectedTime, current.selectedProcess);
		if (selectedTime >= begTime && selectedProcess >= begProcess 
				&& selectedTime<=endTime && selectedProcess<=endProcess)
		{
			csViewer.setSample(selectedTime,selectedProcess,depth);
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
		
		pZoomOutButton.setEnabled(true);
		
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
			pZoomInButton.setEnabled(false);
		}
		
		homeButton.setEnabled(true);
		
		resetVScrollBar();
		vBar.setSelection((int)Math.round(begProcess));
		
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
		
		pZoomInButton.setEnabled(true);
		
		//zoom out works as follows: find mid point of times (yMid).
		//Add/Subtract 1/2 of the scaled numProcessDisp to yMid to get new endProcess and begProcess
		double yMid = (endProcess+begProcess)/2.0;
		
		endProcess = yMid+numProcessDisp*SCALE;
		begProcess = yMid-numProcessDisp*SCALE;
		
		assertProcessBounds();
		
		if(numProcessDisp <= MIN_PROC_DISP)
		{
			numProcessDisp = MIN_PROC_DISP;
			pZoomInButton.setEnabled(false);
		}
		resetVScrollBar();
		vBar.setSelection((int)Math.round(begProcess));
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
		
		tZoomOutButton.setEnabled(true);
		
		long xMid = (endTime + begTime) / 2;
		
		endTime = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		begTime = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		assertTimeBounds();
		
		if(numTimeUnitsDisp < MIN_TIME_UNITS_DISP)
		{
			numTimeUnitsDisp = MIN_TIME_UNITS_DISP;
			tZoomInButton.setEnabled(false);
		}
		
		homeButton.setEnabled(true);
		
		resetHScrollBar();
		hBar.setSelection((int)Math.round(begTime));
		redraw();
	}

	/**************************************************************************
	 * The action that gets performed when the 'time zoom out' button is pressed - 
	 * zooms out timewise with a scale of .625.
	 **************************************************************************/
	public void timeZoomOut()
	{
		pushUndo();
		final double SCALE = 0.625;
		
		tZoomInButton.setEnabled(true);
		
		//zoom out works as follows: find mid point of times (xMid).
		//Add/Subtract 1/2 of the scaled numTimeUnitsDisp to xMid to get new endTime and begTime
		long xMid = (endTime + begTime) / 2;
		
		endTime = xMid + (long)((double)numTimeUnitsDisp * SCALE);
		begTime = xMid - (long)((double)numTimeUnitsDisp * SCALE);
		
		assertTimeBounds();
		
		resetHScrollBar();
		hBar.setSelection((int)Math.round(begTime));
		redraw();
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
		if (!homeScreen)
			pushUndo();
		depth = newDepth;
		redraw();
    }
	
	/**************************************************************************
	 * Sets the location of the crosshair to (_selectedTime, _selectedProcess).
	 * Also updates the rest of the program to know that this is the selected
	 * point (so that the CallStackViewer can update, etc.).
	 **************************************************************************/
	public void setCrossHair(long _selectedTime, int _selectedProcess)
	{
		selectedTime = _selectedTime;
		selectedProcess = _selectedProcess;
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
        
        if(selectedTime==-20.0)
            crossHairLabel.setText("Select Sample For Cross Hair");
        else
        	crossHairLabel.setText("Cross Hair: (" + ((long)(selectedTime/1000))/1000.0 + "s, " + selectedProcess + ")");
        
        // johnmc
        // crossHairLabel.setSize(crossHairLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
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
    
    public void setTimeRange(long topLeftTime, long bottomRightTime)
    {
    	pushUndo();
    	setDetailZoom(topLeftTime, begProcess, bottomRightTime, endProcess);
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
    	depthCanvas.setCrossHair(closeTime, depth);
    	setCrossHair(closeTime, selectedProcess);
    	csViewer.setSample(closeTime, procIndex,depth);
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
				csViewer.csview.updateProcess();
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
	
	public int getDepth()
	{
		return depth;
	}
	
	public void setMiniCanvas(SpaceTimeMiniCanvas _miniCanvas)
	{
		miniCanvas = _miniCanvas;
	}
	
	public void setDepthCanvas(DepthTimeCanvas _depthCanvas)
	{
		depthCanvas = _depthCanvas;
	}
}