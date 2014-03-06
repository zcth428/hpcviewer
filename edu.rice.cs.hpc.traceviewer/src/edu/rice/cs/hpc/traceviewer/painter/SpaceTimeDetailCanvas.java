package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.traceviewer.operation.BufferRefreshOperation;
import edu.rice.cs.hpc.traceviewer.operation.DepthOperation;
import edu.rice.cs.hpc.traceviewer.operation.PositionOperation;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.operation.ZoomOperation;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.ui.Frame;
import edu.rice.cs.hpc.traceviewer.util.Utility;
import edu.rice.cs.hpc.traceviewer.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;


/*************************************************************************
 * 
 *	Canvas onto which the detail view is painted. Also takes care of
 *	zooming responsibilities of the detail view.
 *
 ************************************************************************/
public class SpaceTimeDetailCanvas extends SpaceTimeCanvas 
	implements MouseListener, MouseMoveListener, PaintListener, IOperationHistoryListener
{
	
	/**The buffer image that is copied onto the actual canvas.*/
	private Image imageBuffer;
	
	/**Triggers zoom back to beginning view screen.*/
	private Action homeButton;
	
	/**Triggers open function to open previously saved frame.*/
	private Action openButton;
	
	/**Triggers save function to save current frame to file.*/
	private Action saveButton;
	
	/** Triggers zoom-in on the time axis.*/
	private Action tZoomInButton;
	
	/** Triggers zoom-out on the time axis.*/
	private Action tZoomOutButton;
	
	/** Triggers zoom-in on the process axis.*/
	private Action pZoomInButton;
	
	/** Triggers zoom-out on the process axis.*/
	private Action pZoomOutButton;

	private Action goEastButton, goNorthButton, goWestButton, goSouthButton;
		
	/** Relates to the condition that the mouse is in.*/
	private MouseState mouseState;
	
	/** The point at which the mouse was clicked.*/
	private Point mouseDown;
	
	/** The point at which the mouse was released.*/
	private Point mouseUp;
	
	/** The top-left point that you selected.*/
	private long selectionTopLeftX;
	private long selectionTopLeftY;
	
	/** The bottom-right point that you selected.*/
	private long selectionBottomRightX;
	private long selectionBottomRightY;
	
	/**The Group containing the labels. labelGroup.redraw() is called from the Detail Canvas.*/
	private Composite labelGroup;
   
    /**The Label with the time boundaries.*/
	private Label timeLabel;
   
    /**The Label with the process boundaries.*/
	private Label processLabel;
    
    /**The Label with the current cross hair information.*/
	private Label crossHairLabel;
        
    /**The min number of process units you can zoom in.*/
    private final static int MIN_PROC_DISP = 1;
	
	final private ImageTraceAttributes oldAttributes;
	
	final private ProcessTimelineService ptlService;
	
	final IWorkbenchWindow window;

	final private ExecutorService threadExecutor;
	
    /**Creates a SpaceTimeDetailCanvas with the given parameters*/
	public SpaceTimeDetailCanvas(IWorkbenchWindow window, Composite _composite)
	{
		super(_composite );
		oldAttributes = new ImageTraceAttributes();

		mouseState = MouseState.ST_MOUSE_INIT;

		selectionTopLeftX = 0;
		selectionTopLeftY = 0;
		selectionBottomRightX = 0;
		selectionBottomRightY = 0;
				
		if (this.stData != null) {
			this.addCanvasListener();
		}
		ISourceProviderService service = (ISourceProviderService)window.
				getService(ISourceProviderService.class);
		ptlService = (ProcessTimelineService) service.
				getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER);
		
		this.window = window;
		
		// set the number of maximum threads in the pool to the number of hardware threads
		threadExecutor = Executors.newFixedThreadPool( Utility.getNumThreads(0) ); 
		
		addDisposeListener( new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				// TODO Auto-generated method stub
				dispose();
			}
		});
	}


	/*****
	 * set new database and refresh the screen
	 * @param dataTraces
	 */

	public void updateView(SpaceTimeDataController _stData) {
		this.setSpaceTimeData(_stData);

		
		if (mouseState == MouseState.ST_MOUSE_INIT)
		{
			mouseState = MouseState.ST_MOUSE_NONE;
			this.addCanvasListener();
			OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		}
		// reinitialize the selection rectangle
		initSelectionRectangle();

		// init configuration
		Position p = new Position(-1, -1);
		painter.setPosition(p);
		painter.setDepth(0);
		
		this.home();

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
					view.width = r.width;
					view.height = r.height;
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

				view.width = r.width;
				view.height = r.height;
				redraw();
			}
		});
	}

	private class DetailBufferPaint implements BufferPaint {
		public void rebuffering() {
			// force the paint to refresh the data
			ImageTraceAttributes attr = attributes;
			notifyChanges("Resize", attr.getTimeBegin(), attr.getProcessBegin(),
					attr.getTimeEnd(), attr.getProcessEnd() );
		}
	}

	
	/*************************************************************************
	 * Sets the bounds of the data displayed on the detail canvas to be those 
	 * specified by the zoom operation and adjusts everything accordingly.
	 *************************************************************************/
	public void zoom(long _topLeftTime, int _topLeftProcess, long _bottomRightTime, int _bottomRightProcess)
	{
		attributes.setTime(_topLeftTime, _bottomRightTime);
		attributes.assertTimeBounds(stData.getTimeWidth());
		
		attributes.setProcess(_topLeftProcess, _bottomRightProcess);
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		
		final long numTimeDisplayed = this.getNumTimeUnitDisplayed();
		if (numTimeDisplayed < Constants.MIN_TIME_UNITS_DISP)
		{
			long begTime = _topLeftTime + (numTimeDisplayed - Constants.MIN_TIME_UNITS_DISP) / 2;
			long endTime = _topLeftTime + Constants.MIN_TIME_UNITS_DISP;
			attributes.setTime(begTime, endTime);
		}
		
		final double numProcessDisp = this.getNumProcessesDisplayed();
		if (numProcessDisp < MIN_PROC_DISP)
		{
			int endProcess = _topLeftProcess + MIN_PROC_DISP;
			attributes.setProcess(_topLeftProcess, endProcess );
		}

		updateButtonStates();
    	
		// ----------------------------------------------------------------------------
		// hack solution: we need to gather the data first, then we ask other views 
		//	to update their contents
		// ----------------------------------------------------------------------------
		refresh(true);
	}
	
	/*******************************************************************************
	 * Initialize attributes of selection rectangle
	 *******************************************************************************/
	private void initSelectionRectangle() 
	{
		selectionTopLeftX = 0;
		selectionTopLeftY = 0;
		selectionBottomRightX = 0;
		selectionBottomRightY = 0;
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
		
		view.x = (int) Math.round(attributes.getTimeBegin() * getScalePixelsPerTime());
		view.y = (int) Math.round(attributes.getProcessBegin() * getScalePixelsPerRank());
		
		Rectangle region = imageBuffer.getBounds();
		if (region.width != view.width || region.height != view.height)
			return;
		
		//if something has changed the bounds, you need to go get the data again
		event.gc.drawImage(imageBuffer, 0, 0, view.width, view.height, 0, 0, view.width, view.height);
    	
		//paints the selection currently being made (the little white box that appears
		//when you click and drag
		if(mouseState==MouseState.ST_MOUSE_DOWN)
		{
        	event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    		event.gc.setLineWidth(2);
    		event.gc.drawRectangle((int)(selectionTopLeftX-view.x), (int)(selectionTopLeftY-view.y), (int)(selectionBottomRightX-selectionTopLeftX),
            		(int)(selectionBottomRightY-selectionTopLeftY));
        }
		
		//draws cross hairs
		long selectedTime = painter.getPosition().time;
		int selectedProcess = painter.getPosition().process;
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScalePixelsPerTime())-10-view.x);
		int topPixelCrossHairY = (int)(Math.round((selectedProcess+.5)*getScalePixelsPerRank())-10-view.y);
		event.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		event.gc.fillRectangle(topPixelCrossHairX,topPixelCrossHairY+8,20,4);
		event.gc.fillRectangle(topPixelCrossHairX+8,topPixelCrossHairY,4,20);
		System.gc();
		adjustLabels();
	}

	/**************************************************************************
	 * Initializes the buttons above the detail canvas.
	 **************************************************************************/
	public void setButtons(Action[] toolItems)
	{
		homeButton = toolItems[0];
		openButton = toolItems[1];
		saveButton = toolItems[2];
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
		//if this is the first time painting,
		//some stuff needs to get initialized
		view.x = 0;
		view.y = 0;
		
		view.width = this.getClientArea().width;
		view.height = this.getClientArea().height;
		
		if (view.width <= 0)
			view.width = 1;
		if (view.height <= 0)
			view.height = 1;
		
		// laksono 2012.03.07: this following line causes white paint when changing
		//					   from home to a zoom (or another area) and vice-versa
		//imageBuffer = new Image(getDisplay(), view.width, view.height);


		notifyChanges(ZoomOperation.ActionHome, 0, 0, stData.getTimeWidth(), stData.getTotalTraceCount());
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'open' button is pressed - 
	 * sets everything to the data stored in the Frame toBeOpened.
	 **************************************************************************/
	public void open(Frame toBeOpened)
	{
		if (toBeOpened.begTime == painter.getViewTimeBegin() && toBeOpened.endTime == painter.getViewTimeEnd() 
				&& toBeOpened.begProcess == painter.getBegProcess() && toBeOpened.endProcess == painter.getEndProcess()) {
			
		} else {
			notifyChanges("Frame", toBeOpened.begTime, toBeOpened.begProcess, 
					toBeOpened.endTime, toBeOpened.endProcess);	
			return;
		}
		
		if (toBeOpened.depth != painter.getDepth()) {
			// we have change of depth

			painter.setDepth(toBeOpened.depth);
		}
		
		if (!toBeOpened.position.isEqual(painter.getPosition())) {
	    	notifyChangePosition(toBeOpened.position);
		}
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'save' button is pressed - 
	 * it stores all the relevant data to this current configuration to a new 
	 * Frame.
	 **************************************************************************/
	public Frame save()
	{
		return attributes.getFrame();
	}
	
	
	
	/**************************************************************************
	 * The action that gets performed when the 'process zoom in' button is pressed - 
	 * zooms in processwise with a scale of .4.
	 **************************************************************************/
	public void processZoomIn()
	{
		final double SCALE = .4;
		
		double yMid = (attributes.getProcessEnd()+attributes.getProcessBegin())/2.0;
		
		final double numProcessDisp = attributes.getProcessInterval();
		
		int p2 = (int) Math.ceil( yMid+numProcessDisp*SCALE );
		int p1 = (int) Math.floor( yMid-numProcessDisp*SCALE );
		
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		
		if(p2 == attributes.getProcessEnd() && p1 == attributes.getProcessBegin())
		{
			if(numProcessDisp == 2)
				p2--;
			else if(numProcessDisp > 2)
			{
				p2--;
				p1++;
			}
		}
		
		notifyChanges("Zoom-in ranks", stData.getAttributes().getTimeBegin(), 
				p1, stData.getAttributes().getTimeEnd(), p2);

	}

	/**************************************************************************
	 * The action that gets performed when the 'process zoom out' button is pressed - 
	 * zooms out processwise with a scale of .625.
	 **************************************************************************/
	public void processZoomOut()
	{
		final double SCALE = .625;
		
		//zoom out works as follows: find mid point of times (yMid).
		//Add/Subtract 1/2 of the scaled numProcessDisp to yMid to get new endProcess and begProcess
		double yMid = ((double)attributes.getProcessEnd() + (double)attributes.getProcessBegin())/2.0;
		
		final double numProcessDisp = attributes.getProcessInterval();
		

		int p2 = (int) Math.min( stData.getTotalTraceCount(), Math.ceil( yMid+numProcessDisp*SCALE ) );
		int p1 = (int) Math.max( 0, Math.floor( yMid-numProcessDisp*SCALE ) );
		
		if(p2 == attributes.getProcessEnd() && p1 == attributes.getProcessBegin())
		{
			if(numProcessDisp == 2)
				p2++;
			else if(numProcessDisp > 2)
			{
				p2++;
				p1--;
			}
		}
		notifyChanges("Zoom-out ranks", stData.getAttributes().getTimeBegin(), 
				p1, stData.getAttributes().getTimeEnd(), p2);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'time zoom in' button is pressed - 
	 * zooms in timewise with a scale of .4.
	 **************************************************************************/
	public void timeZoomIn()
	{
		final double SCALE = .4;
		
		long xMid = (attributes.getTimeEnd() + attributes.getTimeBegin()) / 2;
		
		final long numTimeUnitsDisp = attributes.getTimeInterval();
		
		long t2 = xMid + (long)(numTimeUnitsDisp * SCALE);
		long t1 = xMid - (long)(numTimeUnitsDisp * SCALE);
		
		notifyChanges("Zoom-in time", t1, stData.getAttributes().getProcessBegin(),
				t2, stData.getAttributes().getProcessEnd());
	}

	/**************************************************************************
	 * The action that gets performed when the 'time zoom out' button is pressed - 
	 * zooms out timewise with a scale of .625.
	 **************************************************************************/
	public void timeZoomOut()
	{
		final double SCALE = 0.625;
		
		//zoom out works as follows: find mid point of times (xMid).
		//Add/Subtract 1/2 of the scaled numTimeUnitsDisp to xMid to get new endTime and begTime
		long xMid = (attributes.getTimeEnd() + attributes.getTimeBegin()) / 2;
		
		final long td2 = (long)(this.getNumTimeUnitDisplayed() * SCALE); 
		long t2 = Math.min( stData.getTimeWidth(), xMid + td2);
		final long td1 = (long)(this.getNumTimeUnitDisplayed() * SCALE);
		long t1 = Math.max(0, xMid - td1);
		
		notifyChanges("Zoom-out time", t1, stData.getAttributes().getProcessBegin(),
				t2, stData.getAttributes().getProcessEnd());
	}
	
	/**************************************************************************
	 * Gets the scale along the x-axis (pixels per time unit).
	 **************************************************************************/
	public double getScalePixelsPerTime()
	{
		return (double)view.width / (double)this.getNumTimeUnitDisplayed();
	}
	
	/**************************************************************************
	 * Gets the scale along the y-axis (pixels per process).
	 **************************************************************************/
	public double getScalePixelsPerRank()
	{
		return view.height / this.getNumProcessesDisplayed();
	}
	
	/**************************************************************************
	 * Sets the depth to newDepth.
	 **************************************************************************/
	public void setDepth(int newDepth)
	{
		painter.setDepth(newDepth);
		refresh(false);
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
        timeLabel.setText("Time Range: [" + (painter.getViewTimeBegin()/1000)/1000.0 + "s, "
        					+ (painter.getViewTimeEnd()/1000)/1000.0 +  "s]");
        timeLabel.setSize(timeLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        

        final IBaseData traceData = stData.getBaseData();
        if (traceData == null) {
        	// we don't want to throw an exception here, so just do nothing
        	System.out.println("Data null, skipping the rest.");
        	return;
        }
        attributes.assertProcessBounds(traceData.getNumberOfRanks());

        final String processes[] = traceData.getListOfRanks();

        int proc_start = painter.getBegProcess();
        if (proc_start < 0 || proc_start >= processes.length)
        	proc_start = 0;
        
        // -------------------------------------------------------------------------------------------------
        // bug fix: since the end of the process is the ceiling of the selected region,
        //			and the range of process rendering is based on inclusive min and exclusive max, then
        //			we need to decrement the value of max process (in the range).
        // WARN: the display of process range should be then between inclusive min and inclusive max
        //
        // TODO: we should fix the rendering to inclusive min and inclusive max, otherwise it is too much
        //		 headache to maintain
        // -------------------------------------------------------------------------------------------------
        int proc_end   = painter.getEndProcess() - 1;
        if (proc_end>=processes.length)
        	proc_end = processes.length-1;
        
        processLabel.setText("Rank Range: [" + processes[proc_start] + "," + processes[proc_end]+"]");
        processLabel.setSize(processLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        if(stData == null)
            crossHairLabel.setText("Select Sample For Cross Hair");
        else
        {
    		final long selectedTime = painter.getPosition().time;
    		final int rank = this.painter.getPosition().process;
    		
    		if ( rank >= 0 && rank < processes.length ) {               
            	crossHairLabel.setText("Cross Hair: (" + (selectedTime/1000)/1000.0 + "s, " + processes[rank] + ")");
    		} else {
    			// in case of incorrect filtering where user may have empty ranks or incorrect filters, we don't display the rank
    			crossHairLabel.setText("Cross Hair: (" + (selectedTime/1000)/1000.0 + "s, ?)");
    		}
        }
        
        labelGroup.setSize(labelGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
	
    /**************************************************************************
	 * Updates what the position of the selected box is.
	 **************************************************************************/
    private void adjustSelection(Point p1, Point p2)
	{
    	selectionTopLeftX = view.x + Math.max(Math.min(p1.x, p2.x), 0);
        selectionTopLeftY = view.y + Math.max(Math.min(p1.y, p2.y), 0);
        
        selectionBottomRightX = view.x + Math.min(Math.max(p1.x, p2.x), view.width-1);
        selectionBottomRightY = view.y + Math.min(Math.max(p1.y, p2.y), view.height-1);
    }
    
	/**************************************************************************
	 * create a new region of trace view, and check if the cross hair is inside
	 * 	the new region or not. If this is not the case, we force the position
	 * 	of crosshair to be inside the region 
	 **************************************************************************/
	private void setDetail()
    {
		int topLeftProcess = (int) (selectionTopLeftY / getScalePixelsPerRank());
		long topLeftTime = (long)(selectionTopLeftX / getScalePixelsPerTime());
		
		// ---------------------------------------------------------------------------------------
		// we should include the partial selection of a time or a process
		// for instance if the user selects processes where the max process is between
		// 	10 and 11, we should include process 11 (just like keynote selection)
		// ---------------------------------------------------------------------------------------
		int bottomRightProcess = (int) Math.ceil( (selectionBottomRightY / getScalePixelsPerRank()) );
		long bottomRightTime = (long)Math.ceil( (selectionBottomRightX / getScalePixelsPerTime()) );
		
		notifyChanges("Zoom", topLeftTime, topLeftProcess, bottomRightTime, bottomRightProcess);
    }
    
  
    private boolean canGoEast() {
    	return (attributes.getTimeBegin() > 0);
    }
    
    private boolean canGoWest() {
    	return (attributes.getTimeEnd()< this.stData.getTimeWidth());
    }
    
    private boolean canGoNorth() {
    	return (attributes.getProcessBegin()>0);
    }
    
    private boolean canGoSouth() {
    	return (attributes.getProcessEnd()<this.stData.getTotalTraceCount());
    }
    /**********
     * check the status of all buttons
     */
    private void updateButtonStates() 
    {
		this.tZoomInButton.setEnabled( this.getNumTimeUnitDisplayed() > Constants.MIN_TIME_UNITS_DISP );
		this.tZoomOutButton.setEnabled(attributes.getTimeBegin()>0 || attributes.getTimeEnd()<stData.getTimeWidth() );
		
		this.pZoomInButton.setEnabled( getNumProcessesDisplayed() > MIN_PROC_DISP );
		this.pZoomOutButton.setEnabled( attributes.getProcessBegin()>0 || attributes.getProcessEnd()<stData.getTotalTraceCount());
		
		this.goEastButton.setEnabled( canGoEast() );
		this.goWestButton.setEnabled( canGoWest() );
		this.goNorthButton.setEnabled( canGoNorth() );
		this.goSouthButton.setEnabled( canGoSouth() );
		
		homeButton.setEnabled( attributes.getTimeBegin()>0 || attributes.getTimeEnd()<stData.getTimeWidth()
				|| attributes.getProcessBegin()>0 || attributes.getProcessEnd()<stData.getTotalTraceCount() );

    }
    
	final static private double SCALE_MOVE = 0.20;
    
	/***
	 * go to the left one step
	 */
    public void goEast()
    {
    	long topLeftTime = painter.getViewTimeBegin();
		long bottomRightTime = painter.getViewTimeEnd();
		
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
    	long topLeftTime = painter.getViewTimeBegin();
		long bottomRightTime = painter.getViewTimeEnd();
		
		long deltaTime = bottomRightTime - topLeftTime;
		final long moveTime = (long)java.lang.Math.ceil(deltaTime * SCALE_MOVE);
		bottomRightTime = bottomRightTime + moveTime;
		
		if (bottomRightTime > stData.getTimeWidth()) {
			bottomRightTime = stData.getTimeWidth();
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
    	notifyChanges("Zoom H", topLeftTime, stData.getAttributes().getProcessBegin(), 
    			bottomRightTime, stData.getAttributes().getProcessEnd());
    }

    /*******
     * go north one step
     */
    public void goNorth() {
    	int proc_begin = painter.getBegProcess();
    	int proc_end = painter.getEndProcess();
    	final int delta = proc_end - proc_begin;
    	final int move = (int) java.lang.Math.ceil(delta * SCALE_MOVE);

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
    	int proc_begin = painter.getBegProcess();
    	int proc_end = painter.getEndProcess();
    	final int delta = proc_end - proc_begin;
    	final int move = (int) java.lang.Math.ceil(delta * SCALE_MOVE);

    	proc_end = proc_end + move;
    	
    	if (proc_end > stData.getTotalTraceCount()) {
    		proc_end = stData.getTotalTraceCount();
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
	private void setProcessRange(int pBegin, int pEnd) 
	{
		notifyChanges("Zoom V", painter.getViewTimeBegin(), pBegin, 
				painter.getViewTimeEnd(), pEnd);
	}

	private Position updatePosition()
	{
    	int selectedProcess;

    	//need to do different things if there are more traces to paint than pixels
    	if(view.height > getNumProcessesDisplayed())
    	{
    		selectedProcess = (int)(attributes.getProcessBegin()+mouseDown.y/getScalePixelsPerRank());
    	}
    	else
    	{
    		selectedProcess = (int)(attributes.getProcessBegin()+(mouseDown.y*(getNumProcessesDisplayed()))/view.height);
    	}
    	long closeTime = attributes.getTimeBegin() + (long)(mouseDown.x / getScalePixelsPerTime());
    	
    	if (closeTime > attributes.getTimeEnd()) {
    		System.err.println("ERR STDC SCSSample time: " + closeTime +" max time: " + 
    				attributes.getTimeEnd() + "\t new: " + ((attributes.getTimeBegin() + attributes.getTimeEnd()) >> 1));
    		closeTime = (attributes.getTimeBegin() + attributes.getTimeEnd()) >> 1;
    	}
    	
    	return new Position(closeTime, selectedProcess);
	}
	
	
	private void setCSSample()
    {
    	if(mouseDown == null)
    		return;
    	
    	Position position = updatePosition();
    	notifyChangePosition(position);
    }

	
	private long getNumTimeUnitDisplayed()
	{
		return (attributes.getTimeInterval());
	}
	
	private double getNumProcessesDisplayed()
	{
		return (attributes.getProcessInterval());
	}
	
	/* *****************************************************************
	 *		
	 *		MouseListener and MouseMoveListener interface Implementation
	 *      
	 * *****************************************************************/

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) { }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent e)
	{
		// take into account ONLY when the button-1 is clicked and it's never been clicked before
		// the click is not right click (or modifier click on Mac)
		if (e.button == 1 && mouseState == MouseState.ST_MOUSE_NONE 
				&& (e.stateMask & SWT.MODIFIER_MASK)==0 )
		{
			mouseState = MouseState.ST_MOUSE_DOWN;
			mouseDown = new Point(e.x,e.y);
		}
		System.out.println("STDC " + e + " \t brn:" + e.button + " \tm: " + e.stateMask);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
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
				if(getNumTimeUnitDisplayed() == Constants.MIN_TIME_UNITS_DISP)
				{
					if(getNumTimeUnitDisplayed() > MIN_PROC_DISP)
					{
						mouseDown.x = 0;
						mouseUp.x = view.width;
						adjustSelection(mouseDown,mouseUp);
						setDetail();
					}
				}
				else
				{
					adjustSelection(mouseDown,mouseUp);
					setDetail();
				}
				redraw();
			}
			//don't draw the selection if you're not selecting anything
			//adjustSelection(new Point(-1,-1),new Point(-1,-1));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent e)
	{
		if(mouseState == MouseState.ST_MOUSE_DOWN)
		{
			Point mouseTemp = new Point(e.x,e.y);
			adjustSelection(mouseDown,mouseTemp);
			redraw();
		}
	}
	
	
	/*********************************************************************************
	 * Refresh the content of the canvas with new input data or boundary or parameters
	 *  
	 *  @param refreshData boolean whether we need to refresh and read again the data or not
	 *********************************************************************************/
	public void refresh(boolean refreshData) {
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

		if (view.width==0 && view.height==0) {
			view.width = this.getClientArea().width;
			view.height = this.getClientArea().height;
		}
		// -----------------------------------------------------------------------
		// imageFinal is the final image with info of the depth and number of samples
		// the size of the final image is the same of the size of the canvas
		// -----------------------------------------------------------------------
		
		final Image imageFinal = new Image(getDisplay(), view.width, view.height);
		GC bufferGC = new GC(imageFinal);
		bufferGC.setBackground(Constants.COLOR_WHITE);
		bufferGC.fillRectangle(0,0,view.width,view.height);
		
		// -----------------------------------------------------------------------
		// imageOrig is the original image without "attributes" such as depth
		// this imageOrig will be used by SummaryView to count the number of colors
		// the size of the "original" image should be equivalent to the minimum of 
		//	the number of ranks or the number of pixels
		// -----------------------------------------------------------------------
		
		int numLines = Math.min(view.height, attributes.getProcessInterval() );
		Image imageOrig = new Image(getDisplay(), view.width, numLines);
		
		GC origGC = new GC(imageOrig);
		origGC.setBackground(Constants.COLOR_WHITE);
		origGC.fillRectangle(0,0,view.width,view.height);

		// -----------------------------------------------------------------------
		// main method to paint to the canvas
		// if there's no exception or interruption, we redraw the canvas
		// -----------------------------------------------------------------------
		if ( paintDetailViewport(bufferGC, origGC, 
				stData.getAttributes().getProcessBegin(), stData.getAttributes().getProcessEnd(), 
				stData.getAttributes().getTimeBegin(), stData.getAttributes().getTimeEnd(), 
				view.width, view.height, refreshData) ) {
			
			if (imageBuffer != null) {
				imageBuffer.dispose();
			}
			imageBuffer = imageFinal;
			
			// in case of filter, we may need to change the cursor position
			if (refreshData) {
				final String []ranks = stData.getBaseData().getListOfRanks();
				final Position p = attributes.getPosition();
				
				if (p.process > ranks.length-1) {
					// need to change position
					Position new_p = new Position( p.time, ranks.length >> 1 );
					notifyChangePosition(new_p);
				}
			}
			super.redraw();

			// -----------------------------------------------------------------------
			// notify to SummaryView that a new image has been created,
			//	and it needs to refresh the view
			// -----------------------------------------------------------------------

			BufferRefreshOperation brOp = new BufferRefreshOperation("refresh", imageOrig.getImageData());
			try {
				TraceOperation.getOperationHistory().execute(brOp, null, null);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
			updateButtonStates();
		} else {
			// we don't need this "new image" since the paint fails
			imageFinal.dispose();
		}
		// free resources 
		bufferGC.dispose();
		origGC.dispose();
		imageOrig.dispose();
	}
	

	/*************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 * 
	 *	@param masterGC   		 The GC that will contain the combination of all the 1-line GCs.
	 * 	@param origGC			 The original GC without texts
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *	@param begProcess        The first process that will be painted.
	 *	@param endProcess 		 The last process that will be painted.
	 *	@param begTime           The first time unit that will be displayed.
	 *	@param endTime 			 The last time unit that will be displayed.
	 *  @param numPixelsH		 The number of horizontal pixels to be painted.
	 *  @param numPixelsV		 The number of vertical pixels to be painted.
	 *  
	 *  @return boolean true of the pain is successful, false otherwise
	 *************************************************************************/
	public boolean paintDetailViewport(final GC masterGC, final GC origGC, 
			int _begProcess, int _endProcess, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV,
			boolean refreshData)
	{	
		ImageTraceAttributes attributes = stData.getAttributes();
		boolean changedBounds = (refreshData? refreshData : !attributes.sameTrace(oldAttributes) );
		
		attributes.numPixelsH = _numPixelsH;
		attributes.numPixelsV = _numPixelsV;
		
		oldAttributes.copy(attributes);
		if (changedBounds) {
			final int num_traces = Math.min(attributes.numPixelsV, attributes.getProcessInterval());
			ProcessTimeline []traces = new ProcessTimeline[ num_traces ];
			ptlService.setProcessTimeline(traces);
		}

		DetailViewPaint detailPaint = new DetailViewPaint(masterGC, origGC, stData, 
					attributes, changedBounds, window, threadExecutor); 
		
		return detailPaint.paint(this);
	}
	

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose () { 
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
		threadExecutor.shutdown();
		super.dispose();
	}

	//-----------------------------------------------------------------------------------------
	// Part for notifying changes to other views
	//-----------------------------------------------------------------------------------------
	
	
	/***********************************************************************************
	 * notify changes to other views
	 * 
	 * @param _topLeftTime
	 * @param _topLeftProcess
	 * @param _bottomRightTime
	 * @param _bottomRightProcess
	 ***********************************************************************************/
	private void notifyChanges(String label, long _topLeftTime, int _topLeftProcess, 
			long _bottomRightTime, int _bottomRightProcess) 
	{
		final ImageTraceAttributes attributes = stData.getAttributes();
		final Frame frame = new Frame(attributes.getFrame());
		frame.set(_topLeftTime, _bottomRightTime, _topLeftProcess, _bottomRightProcess);
		
		String sLabel = (label == null ? "Set region" : label);

		// forces all other views to refresh with the new region
		try {
			// notify change of ROI
			TraceOperation.getOperationHistory().execute(
					new ZoomOperation(sLabel, frame), 
					null, null);
			
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************
	 * notify cursor position change to other views
	 * 
	 * @param position
	 ***********************************************************************************/
	private void notifyChangePosition(Position position) 
	{
		try {
			TraceOperation.getOperationHistory().execute(
					new PositionOperation(position), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------------------------------
	// Part for handling operation triggered from other views
	//-----------------------------------------------------------------------------------------
	private HistoryOperation historyOperation = new HistoryOperation();
	
	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		final IUndoableOperation operation = event.getOperation();

		// handling the operations
		if (operation.hasContext(TraceOperation.traceContext)) 
		{
			int type = event.getEventType();
			// warning: hack solution
			// this space time detail canvas has priority to execute first before the others
			// the reason is most objects requires a new value of process time lines
			//	however this objects are set by this class
			switch (type)
			{
			case OperationHistoryEvent.ABOUT_TO_EXECUTE:
			case OperationHistoryEvent.ABOUT_TO_REDO:
			case OperationHistoryEvent.ABOUT_TO_UNDO:
				historyOperation.setOperation(operation);
				getDisplay().syncExec(historyOperation);
				break;
			}
		}
	}
	
	/********************************************************
	 * retrieve the image of the buffer to be stored in a file
	 * 
	 * @return
	 ********************************************************/
	public ImageData getImageData() {
		return imageBuffer.getImageData();
	}
	
	/*****
	 * 
	 * Thread-centric operation to perform undoable operations asynchronously
	 *
	 *****/
	private class HistoryOperation implements Runnable
	{
		private IUndoableOperation operation;
		
		public void setOperation(IUndoableOperation operation) {
			this.operation = operation;
		}
		
		@Override
		public void run() {
			// zoom in/out or change of ROI ?
			if (operation instanceof ZoomOperation) {
				Frame frame = ((ZoomOperation)operation).getFrame();
				Debugger.printDebug(1, "STDC: " + attributes + "\t New: " + frame);
				painter.setPosition(frame.position);
				zoom(frame.begTime, frame.begProcess, frame.endTime, frame.endProcess);
			}
			// change of cursor position ?
			else if (operation instanceof PositionOperation) {
				Position p = ((PositionOperation)operation).getPosition();
				painter.setPosition(p);

				// just change the position, doesn't need to fully refresh
				redraw();
			} 
			else if (operation instanceof DepthOperation) {
				int depth = ((DepthOperation)operation).getDepth();
				setDepth(depth);
			}
		}


	}
}