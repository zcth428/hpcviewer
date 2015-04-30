package edu.rice.cs.hpc.traceviewer.painter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.UIJob;

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
public abstract class BaseViewPaint extends UIJob
{

	protected ImageTraceAttributes attributes;
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

	public BaseViewPaint(String title, SpaceTimeDataController _data, ImageTraceAttributes _attributes, boolean _changeBound, 
			IWorkbenchWindow window, ISpaceTimeCanvas canvas, ExecutorService threadExecutor) 
	{
		super(title);
		
		changedBounds = _changeBound;
		controller = _data;
		attributes = _data.getAttributes();

		this.window 		= (window == null ? Util.getActiveWindow() : window);
		this.canvas 		= canvas;
		this.threadExecutor = threadExecutor;
	}
	

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		
		BusyIndicator.showWhile(getDisplay(), getThread());
		if (!paint(canvas, monitor))
		{
			status = Status.CANCEL_STATUS;
		}
		
		return status;
	}	

	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *  @return boolean true of the pain is successful, false otherwise
	 ***********************************************************************************/
	public boolean paint(ISpaceTimeCanvas canvas, IProgressMonitor monitor)
	{	
		// depending upon how zoomed out you are, the iteration you will be
		// making will be either the number of pixels or the processors
		int linesToPaint = getNumberOfLines();
		Debugger.printDebug(2, "BVP-begin " + linesToPaint + " lines");

		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we
		// return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		if (attributes.numPixelsH <= 0)
			return false;
		
		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		int launch_threads = Utility.getNumThreads(linesToPaint);
		if (!startPainting(linesToPaint, launch_threads, changedBounds))
			return false;

		monitor.beginTask(getName(), linesToPaint);

		// -------------------------------------------------------------------
		// Create multiple threads to collect data
		// -------------------------------------------------------------------
		
		// decompression can be done with multiple threads without accessing gtk (on linux)
		// It looks like there's no major performance effect though
		Debugger.printDebug(2, "BVP launch threads " + launch_threads);
		try {
			launchDataGettingThreads(changedBounds, launch_threads);
			
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), "Error while reading data", 
					e.getMessage());
			e.printStackTrace();
			
			// shutdown the monitor to end the progress bar
			monitor.done();
			return false;
		}
		
		// -------------------------------------------------------------------
		// instantiate queue based on whether we need multi-threading or not
		// in case of multithreading, we want a thread-safe queue
		// -------------------------------------------------------------------
		final Queue<TimelineDataSet> queue;
		if (launch_threads > 1) {
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
		
		//final List<Future<Integer>> threads = new ArrayList<Future<Integer>>();
		final AtomicInteger timelineDone = new AtomicInteger(linesToPaint);

		final double xscale = canvas.getScalePixelsPerTime();
		final double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);
		
		ExecutorCompletionService<Integer> ecs = new ExecutorCompletionService<>(threadExecutor);
		
		for (int threadNum = 0; threadNum < launch_threads; threadNum++) {
			final BaseTimelineThread thread = getTimelineThread(canvas, xscale, yscale, queue, 
					timelineDone, monitor);
			ecs.submit(thread);
/*			final Future<Integer> submit = threadExecutor.submit( thread );
			threads.add(submit);*/
		}
		
		// -------------------------------------------------------------------
		// draw to the canvas
		// -------------------------------------------------------------------

		// -------------------------------------------------------------------
		// hack: On Linux, gtk is not threads-safe, and SWT-gtk implementation
		//		 uses lock everytime it calls gtk functions. This greatly impact
		//		 performance degradation, and we don't have the solution until now.
		//	At the moment we don't see any reason to use multi-threading to render
		//	 	 the canvas
		// -------------------------------------------------------------------

		Debugger.printDebug(1, canvas.toString() + " BVP --- lp: " + linesToPaint + ", tld: " + timelineDone + ", qs: " + queue.size());
		Debugger.printTimestampDebug("Rendering mostly finished. (" + canvas.toString()+")");

		if (OSValidator.isUnix()) 
		{
			// -------------------------------------------------------------------
			// sequential painting for Unix/Linux platform
			// -------------------------------------------------------------------
			final BasePaintThread thread = getPaintThread(queue, linesToPaint, timelineDone,
					Display.getCurrent(), attributes.numPixelsH);
			ArrayList<Integer> result = new ArrayList<Integer>();
			waitDataPreparationThreads(ecs, result, 1);
			doSingleThreadPainting(canvas, thread);
		} else
		{
			// -------------------------------------------------------------------
			// painting to the buffer "concurrently" if numPaintThreads > 1
			// -------------------------------------------------------------------
			final List<Future<List<ImagePosition>>> threadsPaint = new ArrayList<Future<List<ImagePosition>>>();

			for (int threadNum=0; threadNum < launch_threads; threadNum++) 
			{
				final BasePaintThread thread = getPaintThread(queue, linesToPaint, timelineDone,
						Display.getCurrent(), attributes.numPixelsH);
				if (thread != null) {
					final Future<List<ImagePosition>> submit = threadExecutor.submit( thread );
					threadsPaint.add(submit);
				}
			}
			// -------------------------------------------------------------------
			// Finalize the painting (to be implemented by the instance)
			// -------------------------------------------------------------------
			ArrayList<Integer> result = new ArrayList<Integer>();
			waitDataPreparationThreads(ecs, result, launch_threads);
			endPainting(canvas, threadsPaint);
		}		
		Debugger.printTimestampDebug("Rendering finished. (" + canvas.toString()+")");
		monitor.done();
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
	 */
	private void doSingleThreadPainting(ISpaceTimeCanvas canvas, BasePaintThread paintThread)
	{
		try {
			// do the data painting, and directly get the generated images
			List<ImagePosition> listImages = paintThread.call();

			// set the images into the canvas. 
			for ( ImagePosition image: listImages )
			{
				drawPainting(canvas, image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitDataPreparationThreads(ExecutorCompletionService<Integer> ecs, 
			ArrayList<Integer> result, int launch_threads)
	{
		for (int i=0; i<launch_threads; i++)
		{
			try {
				Integer linenum = ecs.take().get();
				result.add(linenum);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/******
	 * finalize the data collection, and put all images into a canvas
	 * 
	 * @param canvas
	 * @param listOfImageThreads
	 */
	private void endPainting(ISpaceTimeCanvas canvas, List<Future<List<ImagePosition>>> listOfImageThreads)
	{
		for( Future<List<ImagePosition>> listFutures : listOfImageThreads ) 
		{
			try {
				List<ImagePosition> listImages = listFutures.get();
				for (ImagePosition image : listImages) 
				{
					drawPainting(canvas, image);
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			AtomicInteger timelineDone, Device device, int width);
}
