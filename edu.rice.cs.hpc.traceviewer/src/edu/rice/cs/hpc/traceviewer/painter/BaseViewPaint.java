package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.TimelineThread;


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
	
	private AtomicInteger progress;
	private IStatusLineManager statusMgr;
	private IProgressMonitor monitor;
	
	private ImageTraceAttributes attributes;
	private SpaceTimeData data;
	private boolean changedBounds;
	
	protected int lineNum;
	
	
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
			IStatusLineManager _statusMgr, IProgressMonitor _monitor) 
	{
		data = _data;
		progress = new AtomicInteger();
		statusMgr = _statusMgr;
		monitor = _monitor;
		attributes = _attributes;
		changedBounds = _changeBound;
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
		
		beginProgress(linesToPaint);

		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance
		// -------------------------------------------------------------------
		if (! startPainting(linesToPaint, changedBounds))
			return;

		// -------------------------------------------------------------------
		// Create multiple threads to paint the view
		// -------------------------------------------------------------------
		lineNum = 0;
		TimelineThread[] threads = new TimelineThread[num_threads];
		double xscale = canvas.getScaleX();
		double yscale = Math.max(canvas.getScaleY(), 1);
		
		for (int threadNum = 0; threadNum < threads.length; threadNum++) {
			threads[threadNum] = new TimelineThread(data, changedBounds, canvas, attributes.numPixelsH, xscale, yscale);
			threads[threadNum].start();
		}
		
		try {
			for (int threadNum = 0; threadNum < threads.length; threadNum++) {
				while (threads[threadNum].isAlive()) {
					Thread.sleep(30);
					reportProgress();
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
		
		endProgress();
	}

	
	
	private void beginProgress(int totalWork)
	{
		progress.set(0);
		statusMgr.setMessage("Rendering space time view...");
		// shell.update();
		monitor.beginTask("Trace painting", totalWork);
	}
	
	private void announceProgress()
	{
		progress.getAndIncrement();
	}
	
	private void reportProgress()
	{
		int workDone = progress.getAndSet(0);
		if (workDone > 0)
			monitor.worked(workDone);
	}
	
	private void endProgress()
	{
		monitor.done();
		statusMgr.setMessage(null);
		// shell.update();
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
	
}
