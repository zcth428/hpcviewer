package edu.rice.cs.hpc.traceviewer.painter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Device;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.util.OSValidator;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;
import edu.rice.cs.hpc.traceviewer.util.Utility;


/******************************************************
 * 
 * Abstract class to paint depth view and detail view
 * The instance of the children of the class needs to 
 * implement the start and the end method of the painting
 * 
 *
 *******************************************************/
public abstract class BaseViewPaint 
extends Job
{

	protected boolean changedBounds;
	
	protected final IWorkbenchWindow window;
	
	protected SpaceTimeDataController controller;

	final private ExecutorService threadExecutor;
	final private ISpaceTimeCanvas canvas;

	/**
	 * Constructor to paint a view (trace and depth view)
	 * @param controller: the object used to launch the mode-specific prep before painting
	 * 
	 * @param _data: global data of the traces
	 * @param _attributes: the attribute of the trace view
	 * @param _changeBound: true or false if it requires changes of bound
	 * @param _statusMgr: used for displaying the status
	 * @param _monitor: progress monitor
	 */

	public BaseViewPaint(String title, IWorkbenchWindow window, ExecutorService threadExecutor, ISpaceTimeCanvas canvas) 
	{
		super(title);
		
		this.window 	= (window == null ? Util.getActiveWindow() : window);
		this.threadExecutor = threadExecutor;
		this.canvas         = canvas;
	}
	
	
	protected void setData( SpaceTimeDataController controller, boolean changeBounds) 
	{
		this.controller  	= controller;
		this.changedBounds 	= changeBounds;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		IStatus status = Status.CANCEL_STATUS;
		try {
			boolean result = paint(canvas, monitor);
			if (result) {
				status = Status.OK_STATUS;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		monitor.done();

		return status;
	}	

	
	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *  @return boolean true of the pain is successful, false otherwise
	 * @throws Exception 
	 ***********************************************************************************/
	private boolean paint(ISpaceTimeCanvas canvas, IProgressMonitor monitor)
			throws Exception
	{	
		// depending upon how zoomed out you are, the iteration you will be
		// making will be either the number of pixels or the processors
		int linesToPaint = getNumberOfLines();
		Debugger.printDebug(2, "BVP-begin " + linesToPaint + " lines");

		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we
		// return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		final ImageTraceAttributes attributes = controller.getAttributes();
		if (attributes.numPixelsH <= 0)
			return false;
		
		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		if (!startPainting(linesToPaint, Utility.getNumThreads(linesToPaint), changedBounds))
			return false;

		monitor.beginTask("Rendering space time view ...", linesToPaint);
		// -------------------------------------------------------------------
		// Create multiple threads to collect data
		// -------------------------------------------------------------------
		
		// decompression can be done with multiple threads without accessing gtk (on linux)
		// It looks like there's no major performance effect though
		int launch_threads = Utility.getNumThreads(linesToPaint);
		Debugger.printDebug(2, "BVP launch threads " + launch_threads);
		try {
			launchDataGettingThreads(changedBounds, launch_threads);
			
		} catch (IOException e) {
			MessageDialog.openError(window.getShell(), "Error while reading data", 
					e.getMessage());
			e.printStackTrace();
			
			return false;
		}
		
		// -------------------------------------------------------------------
		// instantiate queue based on whether we need multi-threading or not
		// in case of multithreading, we want a thread-safe queue
		// -------------------------------------------------------------------
		final Queue<TimelineDataSet> queue;
		if (Utility.getNumThreads(linesToPaint) > 1) {
			queue = new ConcurrentLinkedQueue<TimelineDataSet>();
		} else {
			queue = new LinkedList<TimelineDataSet>();
		}

		// -------------------------------------------------------------------
		// case where everything works fine, and all the data has been read,
		//	we paint the canvas using multiple threads
		// -------------------------------------------------------------------
		
		Debugger.printTimestampDebug("Rendering beginning (" + canvas.toString()+")");

		// reset the line number to paint
		controller.resetCounters();
		
		final List<Future<Integer>> threads = new ArrayList<Future<Integer>>();
		final AtomicInteger timelineDone = new AtomicInteger(linesToPaint);

		final double xscale = canvas.getScalePixelsPerTime();
		final double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);
		
		for (int threadNum = 0; threadNum < Utility.getNumThreads(linesToPaint); threadNum++) {
			final Callable<Integer> thread = getTimelineThread(canvas, xscale, yscale, queue, 
					timelineDone, monitor);
			final Future<Integer> submit = threadExecutor.submit( thread );
			threads.add(submit);
		}
		
		// -------------------------------------------------------------------
		// draw to the canvas
		// -------------------------------------------------------------------
		int numPaintThreads = 1; 

		// -------------------------------------------------------------------
		// hack: On Linux, gtk is not threads-safe, and SWT-gtk implementation
		//		 uses lock everytime it calls gtk functions. This greatly impact
		//		 the performance, we don't have the solution until now.
		//	At the moment we don't see any reason to use multi-threading to render
		//	 	 the canvas
		// -------------------------------------------------------------------
		if (!OSValidator.isUnix()) {
			numPaintThreads = edu.rice.cs.hpc.traceviewer.util.Utility.getNumThreads(linesToPaint);
		}
		final boolean singleThread = (numPaintThreads == 1);
		
		final List<Future<List<ImagePosition>>> threadsPaint = new ArrayList<Future<List<ImagePosition>>>();

		Debugger.printDebug(1, "BVP --- lp: " + linesToPaint + ", tld: " + timelineDone + ", qs: " + queue.size());
		
		// -------------------------------------------------------------------
		// painting to the buffer "concurrently" if numPaintThreads > 1
		// -------------------------------------------------------------------
		Device device = window.getShell().getDisplay();
		for (int threadNum=0; threadNum < numPaintThreads; threadNum++) 
		{
			final BasePaintThread thread = getPaintThread(queue, linesToPaint, timelineDone,
					device, attributes.numPixelsH, monitor);
			if (thread != null) {
				if (singleThread) 
				{
					doSingleThreadPainting(canvas, thread);
				} else
				{
					final Future<List<ImagePosition>> submit = threadExecutor.submit( thread );
					threadsPaint.add(submit);
				}
			}
		}

		Debugger.printTimestampDebug("Rendering mostly finished. (" + canvas.toString()+")");

		if (!singleThread) {
			// -------------------------------------------------------------------
			// Finalize the painting (to be implemented by the instance)
			// -------------------------------------------------------------------
			endPainting(canvas, threadsPaint);
		}
		
		Debugger.printTimestampDebug("Rendering finished. (" + canvas.toString()+")");

		changedBounds = false;

		return true;
	}
	
	/****
	 * perform a data painting with only a single thread.
	 * this method doesn't need collection or painting finalization since only one
	 * thread is involved.
	 * 
	 * @param canvas
	 * @param paintThread
	 * @throws Exception 
	 */
	private void doSingleThreadPainting(ISpaceTimeCanvas canvas, BasePaintThread paintThread)
			throws Exception
	{
		// do the data painting, and directly get the generated images
		List<ImagePosition> listImages = paintThread.call();

		// the listImages is null, it means the user cancels the operation
		if (listImages != null) 
		{
			// set the images into the canvas. 
			for ( ImagePosition image: listImages )
			{
				drawPainting(canvas, image);
			}
		}
	}

	/******
	 * finalize the data collection, and put all images into a canvas
	 * 
	 * @param canvas
	 * @param listOfImageThreads
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private void endPainting(ISpaceTimeCanvas canvas, List<Future<List<ImagePosition>>> listOfImageThreads) 
			throws InterruptedException, ExecutionException
	{
		for( Future<List<ImagePosition>> listFutures : listOfImageThreads ) 
		{
			List<ImagePosition> listImages = listFutures.get();
			if (listImages != null)
			{
				for (ImagePosition image : listImages) 
				{
					drawPainting(canvas, image);
				}
			}
		}
	}
	
	
	//------------------------------------------------------------------------------------------------
	// abstract methods 
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Initialize the paint, before creating the threads to paint
	 * The method return false to exit the paint, true to paint
	 * 
	 * @param linesToPaint
	 * @param changedBounds
	 * @return false will exit the painting
	 */
	abstract protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds);
	
	/***
	 * start painting an image to the canvas
	 * 
	 * @param canvas: canvas to be painted
	 * @param imagePosition : a pair of image and position
	 */
	abstract protected void drawPainting(ISpaceTimeCanvas canvas, ImagePosition imagePosition);
	
	/**
	 * Retrieve the number of lines to paint 
	 * @return
	 */
	abstract protected int getNumberOfLines();
	
	/****
	 * launching threads for remote communication
	 * 
	 * @param changedBounds
	 * @param numThreads
	 * @throws IOException
	 */
	abstract protected void launchDataGettingThreads(boolean changedBounds, int numThreads) 
			throws IOException;

	/****
	 * get a thread for collecting timeline data
	 * @param canvas
	 * @param xscale
	 * @param yscale
	 * @param queue
	 * @param timelineDone
	 * @return
	 */
	abstract protected BaseTimelineThread  getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, AtomicInteger timelineDone, IProgressMonitor monitor);
	
	/***
	 * get a thread for painting a number of lines
	 * @param queue
	 * @param numLines
	 * @param timelineDone
	 * @param device
	 * @param width
	 * @return
	 */
	abstract protected BasePaintThread getPaintThread( Queue<TimelineDataSet> queue, int numLines, 
			AtomicInteger timelineDone, Device device, int width, IProgressMonitor monitor);
}
