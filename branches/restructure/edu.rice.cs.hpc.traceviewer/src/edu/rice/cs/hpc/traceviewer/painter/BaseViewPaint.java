package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;


/******************************************************
 * 
 * Abstract class to paint depth view and detail view
 * The instance of the children of the class needs to 
 * implement the start and the end method of the painting
 * 
 * @author laksono
 *
 *******************************************************/
public abstract class BaseViewPaint {
	
	protected ImageTraceAttributes attributes;
	protected SpaceTimeData data;
	protected boolean changedBounds;
	
	protected TimelineProgressMonitor monitor;
	
	protected final IWorkbenchWindow window;
	
	/**
	 * Constructor to paint a view (trace and depth view)
	 * 
	 * @param _data: global data of the traces
	 * @param _attributes: the attribute of the trace view
	 * @param _changeBound: true or false if it requires changes of bound
	 * @param _statusMgr: used for displaying the status
	 * @param _monitor: progress monitor
	 */
	public BaseViewPaint(SpaceTimeData _data, ImageTraceAttributes _attributes, boolean _changeBound, 
			IStatusLineManager _statusMgr, IWorkbenchWindow window) 
	{
		data = _data;
		attributes = _attributes;
		changedBounds = _changeBound;
		monitor = new TimelineProgressMonitor(_statusMgr );
		this.window = (window == null ? Util.getActiveWindow() : window);
	}
	
	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 ***********************************************************************************/
	public void paint(SpaceTimeCanvas canvas)
	{	
		
		//depending upon how zoomed out you are, the iteration you will be making will be either the number of pixels or the processors
		int linesToPaint = getNumberOfLines();
		final int num_threads = Math.min(linesToPaint, Runtime.getRuntime().availableProcessors());

		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we 
		//			 return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		if (attributes.numPixelsH <= 0)
			return;
		
		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		if (! startPainting(linesToPaint, changedBounds))
			return;
		
		monitor.beginProgress(linesToPaint, "Rendering space time view...", "Trace painting", window.getShell());

		// -------------------------------------------------------------------
		// Create multiple threads to paint the view
		// -------------------------------------------------------------------

		Thread[] threads = new Thread[num_threads];
		double xscale = canvas.getScaleX();
		double yscale = Math.max(canvas.getScaleY(), 1);
		
		for (int threadNum = 0; threadNum < threads.length; threadNum++) {
			threads[threadNum] = getTimelineThread(canvas, xscale, yscale);
			threads[threadNum].start();
		}
		
		int numThreads = threads.length;
		try {
			// listen all threads (one by one) if they are all finish
			// somehow, a thread can be alive forever waiting to lock a resource, 
			// especially when we resize the window. this approach should reduce
			// deadlock by polling each thread
			while (numThreads > 0) {
				for (Thread thread : threads) {
					if (thread.isAlive()) {
						monitor.reportProgress();
					} else {
						if (!thread.getName().equals("end")) {
							numThreads--;
							// mark that this thread has ended
							thread.setName("end");
						}
					}
					Thread.sleep(30);
				}
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// -------------------------------------------------------------------
		// Finalize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		endPainting(linesToPaint, xscale, yscale);
		
		monitor.endProgress();
		changedBounds = false;
	}


	//------------------------------------------------------------------------------------------------
	// abstract methods 
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Initialize the paint, before creating the threads to aint
	 * The method return false to exit the paint, true to paint
	 * 
	 * @param linesToPaint
	 * @param changedBounds
	 * @return false will exit the painting
	 */
	abstract protected boolean startPainting(int linesToPaint, boolean changedBounds);
	
	/**
	 * Finalize the paint
	 * 
	 * @param linesToPaint
	 * @param xscale
	 * @param yscale
	 */
	abstract protected void endPainting(int linesToPaint, double xscale, double yscale);
	
	/**
	 * Retrieve the number of lines to paint 
	 * @return
	 */
	abstract protected int getNumberOfLines();
	
	abstract protected Thread getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale);
}
