package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
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
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.ui.Frame;
import edu.rice.cs.hpc.traceviewer.util.Constants;


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

		
		Position position = new Position(-1,-1);
		painter.setPosition(position);
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
			// force the paint to refresh the data
			ImageTraceAttributes attr = attributes;
			notifyChanges("Resize", attr.begTime, attr.begProcess,
					attr.endTime, attr.endProcess);
		}
	}

	
	/*************************************************************************
	 * Sets the bounds of the data displayed on the detail canvas to be those 
	 * specified by the zoom operation and adjusts everything accordingly.
	 *************************************************************************/
	public void setDetailZoom(long _topLeftTime, int _topLeftProcess, long _bottomRightTime, int _bottomRightProcess)
	{
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		attributes.assertTimeBounds(stData.getTimeWidth());
		
		attributes.setTime(_topLeftTime, _bottomRightTime);
		attributes.setProcess(_topLeftProcess, _bottomRightProcess);
		
		final long numTimeDisplayed = this.getNumTimeUnitDisplayed();
		if (numTimeDisplayed < Constants.MIN_TIME_UNITS_DISP)
		{
			attributes.begTime += (numTimeDisplayed - Constants.MIN_TIME_UNITS_DISP) / 2;
			attributes.endTime = attributes.begTime + Constants.MIN_TIME_UNITS_DISP;
		}
		
		final double numProcessDisp = this.getNumProcessesDisplayed();
		if (numProcessDisp < MIN_PROC_DISP)
		{
			attributes.begProcess = attributes.begProcess;
			attributes.endProcess = attributes.begProcess+MIN_PROC_DISP;
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
		
		topLeftPixelX = Math.round(attributes.begTime * getScaleX());
		topLeftPixelY = Math.round(attributes.begProcess * getScaleY());
		
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
		long selectedTime = painter.getPosition().time;
		int selectedProcess = painter.getPosition().process;
		
		int topPixelCrossHairX = (int)(Math.round(selectedTime*getScaleX())-10-topLeftPixelX);
		int topPixelCrossHairY = (int)(Math.round((selectedProcess+.5)*getScaleY())-10-topLeftPixelY);
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
		topLeftPixelX = 0;
		topLeftPixelY = 0;
		
		viewWidth = this.getClientArea().width;
		viewHeight = this.getClientArea().height;
		
		if (viewWidth <= 0)
			viewWidth = 1;
		if (viewHeight <= 0)
			viewHeight = 1;
		
		// laksono 2012.03.07: this following line causes white paint when changing
		//					   from home to a zoom (or another area) and vice-versa
		//imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);


		notifyChanges(ZoomOperation.ActionHome, 0, 0, stData.getTimeWidth(), stData.getTotalTraceCount());
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'open' button is pressed - 
	 * sets everything to the data stored in the Frame toBeOpened.
	 **************************************************************************/
	public void open(Frame toBeOpened)
	{
		setFrame(toBeOpened);
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'save' button is pressed - 
	 * it stores all the relevant data to this current configuration to a new 
	 * Frame.
	 **************************************************************************/
	public Frame save()
	{
		long selectedTime = painter.getPosition().time;
		int selectedProcess = painter.getPosition().process;

		return new Frame(attributes, painter.getDepth(), selectedTime, selectedProcess);
	}
	

	
	
	
	/**************************************************************************
	 * Sets everything to the data stored in the Frame 'current.'
	 **************************************************************************/
	private void setFrame(Frame current)
	{
		if (current.begTime == painter.getViewTimeBegin() && current.endTime == painter.getViewTimeEnd() 
				&& current.begProcess == painter.getBegProcess() && current.endProcess == painter.getEndProcess()) {
			
		} else {
			notifyChanges("Frame", current.begTime, current.begProcess, 
					current.endTime, current.endProcess);	
			return;
		}
		
		if (current.depth != painter.getDepth()) {
			// we have change of depth

			painter.setDepth(current.depth);
		}
		
		if (!current.position.isEqual(painter.getPosition())) {
	    	notifyChangePosition(current.position);
		}
	}
	
	/**************************************************************************
	 * The action that gets performed when the 'process zoom in' button is pressed - 
	 * zooms in processwise with a scale of .4.
	 **************************************************************************/
	public void processZoomIn()
	{
		final double SCALE = .4;
		
		double yMid = (attributes.endProcess+attributes.begProcess)/2.0;
		
		final double numProcessDisp = attributes.endProcess - attributes.begProcess;
		
		int p2 = (int) Math.ceil( yMid+numProcessDisp*SCALE );
		int p1 = (int) Math.floor( yMid-numProcessDisp*SCALE );
		
		attributes.assertProcessBounds(stData.getTotalTraceCount());
		
		if(p2 == attributes.endProcess && p1 == attributes.begProcess)
		{
			if(numProcessDisp == 2)
				p2--;
			else if(numProcessDisp > 2)
			{
				p2--;
				p1++;
			}
		}
		
		notifyChanges("Zoom in V", attributes.begTime, p1, attributes.endTime, p2);

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
		double yMid = ((double)attributes.endProcess + (double)attributes.begProcess)/2.0;
		
		final double numProcessDisp = attributes.endProcess - attributes.begProcess;
		

		int p2 = (int) Math.min( stData.getTotalTraceCount(), Math.ceil( yMid+numProcessDisp*SCALE ) );
		int p1 = (int) Math.max( 0, Math.floor( yMid-numProcessDisp*SCALE ) );
		
		if(p2 == attributes.endProcess && p1 == attributes.begProcess)
		{
			if(numProcessDisp == 2)
				p2++;
			else if(numProcessDisp > 2)
			{
				p2++;
				p1--;
			}
		}
		notifyChanges("Zoom out V", attributes.begTime, p1, attributes.endTime, p2);
	}

	
	/**************************************************************************
	 * The action that gets performed when the 'time zoom in' button is pressed - 
	 * zooms in timewise with a scale of .4.
	 **************************************************************************/
	public void timeZoomIn()
	{
		final double SCALE = .4;
		
		long xMid = (attributes.endTime + attributes.begTime) / 2;
		
		final long numTimeUnitsDisp = attributes.endTime - attributes.begTime;
		
		long t2 = xMid + (long)(numTimeUnitsDisp * SCALE);
		long t1 = xMid - (long)(numTimeUnitsDisp * SCALE);
		
		notifyChanges("Zoom in H", t1, attributes.begProcess, t2, attributes.endProcess);
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
		long xMid = (attributes.endTime + attributes.begTime) / 2;
		
		final long td2 = (long)(this.getNumTimeUnitDisplayed() * SCALE); 
		long t2 = Math.min( stData.getTimeWidth(), xMid + td2);
		final long td1 = (long)(this.getNumTimeUnitDisplayed() * SCALE);
		long t1 = Math.max(0, xMid - td1);
		
		notifyChanges("Zoom out H", t1, attributes.begProcess, t2, attributes.endProcess);
	}
	
	/**************************************************************************
	 * Gets the scale along the x-axis (pixels per time unit).
	 **************************************************************************/
	public double getScaleX()
	{
		return (double)viewWidth / (double)this.getNumTimeUnitDisplayed();
	}
	
	/**************************************************************************
	 * Gets the scale along the y-axis (pixels per process).
	 **************************************************************************/
	public double getScaleY()
	{
		return viewHeight / this.getNumProcessesDisplayed();
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
        painter.fixPosition();
        final String processes[] = traceData.getListOfRanks();

        final int proc_start = painter.getBegProcess();
        
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
    		final String selectedProcessLabel = processes[rank];
            
        	crossHairLabel.setText("Cross Hair: (" + (selectedTime/1000)/1000.0 + "s, " + selectedProcessLabel + ")");
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
		int topLeftProcess = (int) (selectionTopLeftY / getScaleY());
		long topLeftTime = (long)(selectionTopLeftX / getScaleX());
		
		// ---------------------------------------------------------------------------------------
		// we should include the partial selection of a time or a process
		// for instance if the user selects processes where the max process is between
		// 	10 and 11, we should include process 11 (just like keynote selection)
		// ---------------------------------------------------------------------------------------
		int bottomRightProcess = (int) Math.ceil( (selectionBottomRightY / getScaleY()) );
		long bottomRightTime = (long)Math.ceil( (selectionBottomRightX / getScaleX()) );
		
		notifyChanges("Zoom", topLeftTime, topLeftProcess, bottomRightTime, bottomRightProcess);
    }
    
/*<<<<<<< .working
    *//******
     * If necessary adjust the position of cross hair with the view region
     * If the cross hair is outside the region, we force to position it within the region
     * 	in order to avoid exposed bugs and confusion that the depth and the call path are not
     * 	consistent with the trace view
     * 
     * @param t1
     * @param p1
     * @param t2
     * @param p2
     *//*
    private void adjustCrossHair(long t1, double p1, long t2, double p2) {
    	
    	Position current = this.painter.getPosition();
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
    	this.painter.updatePosition(newPosition);
    }*

    /****
     * Adjust the position of cross hair depending of the availability of traces
     * 
     * @param position
     * @return adjusted position
     */
    /*private Position getAdjustedProcess(Position position) {
    	
    	double numDisplayedProcess = painter.getNumberOfDisplayedProcesses();
    	int estimatedProcess = (int) (position.process - attributes.begProcess);			
    	double scaleProcess = numDisplayedProcess/(double)this.getNumProcessesDisplayed();
    	
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
    	
    	//position.processInCS = relativeProcess;
    	if (estimatedProcess != relativeProcess) {
        	//---------------------------------------------------------------------------------------
        	// if there is any change between the estimated process by mouse click and the
    		//	estimated process by the array of displayed process, we need to adjust
    		//	the absolute process
        	//---------------------------------------------------------------------------------------
        	position.process = (int) (relativeProcess/scaleProcess + attributes.begProcess);
    	}
    	return position;
    	
    }
*/
  
    private boolean canGoEast() {
    	return (attributes.begTime > 0);
    }
    
    private boolean canGoWest() {
    	return (attributes.endTime< this.stData.getTimeWidth());
    }
    
    private boolean canGoNorth() {
    	return (attributes.begProcess>0);
    }
    
    private boolean canGoSouth() {
    	return (attributes.endProcess<this.stData.getTotalTraceCount());
    }
    /**********
     * check the status of all buttons
     */
    private void updateButtonStates() 
    {
		this.tZoomInButton.setEnabled( this.getNumTimeUnitDisplayed() > Constants.MIN_TIME_UNITS_DISP );
		this.tZoomOutButton.setEnabled(attributes.begTime>0 || attributes.endTime<stData.getTimeWidth() );
		
		this.pZoomInButton.setEnabled( getNumProcessesDisplayed() > MIN_PROC_DISP );
		this.pZoomOutButton.setEnabled( attributes.begProcess>0 || attributes.endProcess<stData.getTotalTraceCount());
		
		this.goEastButton.setEnabled( canGoEast() );
		this.goWestButton.setEnabled( canGoWest() );
		this.goNorthButton.setEnabled( canGoNorth() );
		this.goSouthButton.setEnabled( canGoSouth() );
		
		homeButton.setEnabled( attributes.begTime>0 || attributes.endTime<stData.getTimeWidth()
				|| attributes.begProcess>0 || attributes.endProcess<stData.getTotalTraceCount() );

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
    	notifyChanges("Zoom H", topLeftTime, attributes.begProcess, 
    			bottomRightTime, attributes.endProcess);
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
    	if(viewHeight > getNumProcessesDisplayed())
    	{
    		selectedProcess = (int)(attributes.begProcess+mouseDown.y/getScaleY());
    	}
    	else
    	{
    		selectedProcess = (int)(attributes.begProcess+(mouseDown.y*(getNumProcessesDisplayed()))/viewHeight);
    	}
    	long closeTime = attributes.begTime + (long)(mouseDown.x / getScaleX());
    	if (closeTime > attributes.endTime) {
    		System.err.println("ERR STDC SCSSample time: " + closeTime +" max time: " + 
    				attributes.endTime + "\t new: " + ((attributes.begTime + attributes.endTime) >> 1));
    		closeTime = (attributes.begTime + attributes.endTime) >> 1;
    		
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
		return (attributes.endTime - attributes.begTime);
	}
	
	private double getNumProcessesDisplayed()
	{
		return (attributes.endProcess - attributes.begProcess);
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
				if(getNumTimeUnitDisplayed() == Constants.MIN_TIME_UNITS_DISP)
				{
					if(getNumTimeUnitDisplayed() > MIN_PROC_DISP)
					{
						mouseDown.x = 0;
						mouseUp.x = viewWidth;
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

		if (viewWidth==0 && viewHeight==0) {
			viewWidth = this.getClientArea().width;
			viewHeight = this.getClientArea().height;
		}
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		GC bufferGC = new GC(imageBuffer);
		bufferGC.setBackground(Constants.COLOR_WHITE);
		bufferGC.fillRectangle(0,0,viewWidth,viewHeight);
		
		Image imageOrig = new Image(getDisplay(), viewWidth, viewHeight);
		GC origGC = new GC(imageOrig);
		origGC.setBackground(Constants.COLOR_WHITE);
		origGC.fillRectangle(0,0,viewWidth,viewHeight);

		paintDetailViewport(bufferGC, origGC, attributes.begProcess, attributes.endProcess, 
				attributes.begTime, attributes.endTime, viewWidth, viewHeight, refreshData);

		
		bufferGC.dispose();
		origGC.dispose();
		
		super.redraw();

		BufferRefreshOperation brOp = new BufferRefreshOperation("refresh", imageOrig.getImageData());
		try {
			TraceOperation.getOperationHistory().execute(brOp, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		updateButtonStates();
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
	 *************************************************************************/
	public void paintDetailViewport(final GC masterGC, final GC origGC, 
			int _begProcess, int _endProcess, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV,
			boolean refreshData)
	{	
		ImageTraceAttributes attributes = stData.getAttributes();
		boolean changedBounds = (refreshData? refreshData : !attributes.sameTrace(oldAttributes) );
		
		
		attributes.numPixelsH = _numPixelsH;
		attributes.numPixelsV = _numPixelsV;
		
		oldAttributes.copy(attributes);
		if (changedBounds) {
			final int num_traces = Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
			ProcessTimeline []traces = new ProcessTimeline[ num_traces ];
			ptlService.setProcessTimeline(traces);
		}

		DetailViewPaint detailPaint = new DetailViewPaint(masterGC, origGC, stData, 
					attributes, changedBounds, window); 
		
		detailPaint.paint(this);
	}
	

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose () { 
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
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
		attributes.begTime = _topLeftTime;
		attributes.endTime = _bottomRightTime;
		attributes.begProcess = _topLeftProcess;
		attributes.endProcess = _bottomRightProcess;
		Frame frame = new Frame(attributes, painter.getDepth(), 
				painter.getPosition().time, painter.getPosition().process);
		
		String sLabel = (label == null ? "Set region" : label);
		
		// forces all other views to refresh with the new region
		try {
			// notify change of ROI
			TraceOperation.getOperationHistory().execute(
					new ZoomOperation(sLabel, frame, null), 
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
					new PositionOperation(position, null), 
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
				painter.setPosition(frame.position);
				setDetailZoom(frame.begTime, frame.begProcess, frame.endTime, frame.endProcess);
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