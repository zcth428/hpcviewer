package edu.rice.cs.hpc.traceviewer.painter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Device;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.PaintManager;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
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
public abstract class BaseViewPaint {

	protected ImageTraceAttributes attributes;
	protected boolean changedBounds;
	protected TimelineProgressMonitor monitor;
	
	protected final IWorkbenchWindow window;
	
	protected SpaceTimeDataController controller;
	protected PaintManager painter;
	final private ExecutorService threadExecutor;

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

	public BaseViewPaint(SpaceTimeDataController _data, ImageTraceAttributes _attributes, boolean _changeBound, 
			IWorkbenchWindow window, ExecutorService threadExecutor) 
	{
		changedBounds = _changeBound;
		controller = _data;
		attributes = _data.getAttributes();
		painter = _data.getPainter();
		this.window = (window == null ? Util.getActiveWindow() : window);
		IViewSite site = (IViewSite) window.getActivePage().getActivePart().getSite();
		monitor = new TimelineProgressMonitor( site.getActionBars().getStatusLineManager() );

		this.threadExecutor = threadExecutor;
	}
	
	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *  @return boolean true of the pain is successful, false otherwise
	 ***********************************************************************************/
	public boolean paint(SpaceTimeCanvas canvas)
	{	
		
		// depending upon how zoomed out you are, the iteration you will be
		// making will be either the number of pixels or the processors
		int linesToPaint = getNumberOfLines();
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
		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we
		// return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		if (attributes.numPixelsH <= 0)
			return false;
		
		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		if (!startPainting(linesToPaint, Utility.getNumThreads(linesToPaint), changedBounds))
			return false;

		monitor.beginProgress(linesToPaint, "Rendering space time view...",
				"Trace painting", window.getShell());

		// -------------------------------------------------------------------
		// Create multiple threads to paint the view
		// -------------------------------------------------------------------

		double xscale = canvas.getScalePixelsPerTime();
		double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);
		
		// decompression can be done with multiple threads without accessing gtk (on linux)
		// It looks like there's no major performance effect though
		int launch_threads = Utility.getNumThreads(linesToPaint);
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
		
		for (int threadNum = 0; threadNum < Utility.getNumThreads(linesToPaint); threadNum++) {
			final Callable<Integer> thread = getTimelineThread(canvas, xscale, yscale, queue, timelineDone);
			final Future<Integer> submit = threadExecutor.submit( thread );
			threads.add(submit);
		}
		
		// -------------------------------------------------------------------
		// painting to the buffer concurrently
		// -------------------------------------------------------------------
		final List<Future<List<ImagePosition>>> threadsPaint = new ArrayList<Future<List<ImagePosition>>>();

		for (int threadNum=0; threadNum < numPaintThreads; threadNum++) 
		{
			final Callable<List<ImagePosition>> thread = getPaintThread(queue, linesToPaint, timelineDone,
					canvas.getDisplay(), attributes.numPixelsH);
			if (thread != null) {
				final Future<List<ImagePosition>> submit = threadExecutor.submit( thread );
				threadsPaint.add(submit);
			}
		}

		Debugger.printTimestampDebug("Rendering mostly finished. (" + canvas.toString()+")");

		// -------------------------------------------------------------------
		// Finalize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		endPainting(linesToPaint, xscale, yscale, threadsPaint);
		
		Debugger.printTimestampDebug("Rendering finished. (" + canvas.toString()+")");
		monitor.endProgress();
		changedBounds = false;

		return true;
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
	
	/**
	 * Finalize the paint
	 * 
	 * @param linesToPaint
	 * @param xscale
	 * @param yscale
	 */
	abstract protected void endPainting(int linesToPaint, double xscale, double yscale, 
			List<Future<List<ImagePosition>>> listOfImages);
	
	/**
	 * Retrieve the number of lines to paint 
	 * @return
	 */
	abstract protected int getNumberOfLines();
	
	abstract protected void launchDataGettingThreads(boolean changedBounds, int numThreads) 
			throws IOException;

	abstract protected Callable<Integer>  getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, AtomicInteger timelineDone);
	
	abstract protected Callable<List<ImagePosition>> getPaintThread( Queue<TimelineDataSet> queue, int numLines, 
			AtomicInteger timelineDone, Device device, int width);
}
