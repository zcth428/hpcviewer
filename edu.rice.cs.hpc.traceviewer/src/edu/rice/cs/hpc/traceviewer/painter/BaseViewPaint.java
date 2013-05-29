package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.PaintManager;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;



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
	protected boolean changedBounds;
	protected TimelineProgressMonitor monitor;
	
	protected final IWorkbenchWindow window;
	
	protected SpaceTimeDataController controller;
	protected PaintManager painter;
	
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
			IWorkbenchWindow window) 
	{
		changedBounds = _changeBound;
		controller = _data;
		painter = _data.getPainter();
		this.window = (window == null ? Util.getActiveWindow() : window);
		IViewSite site = (IViewSite) window.getActivePage().getActivePart().getSite();
		monitor = new TimelineProgressMonitor( site.getActionBars().getStatusLineManager() );

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

		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we 
		//			 return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		if (attributes.numPixelsH <= 0)
			return;
		
		startPainting(linesToPaint, changedBounds);

		
		monitor.beginProgress(linesToPaint, "Rendering space time view...", "Trace painting", window.getShell());
		
		
		double xscale = canvas.getScaleX();
		double yscale = Math.max(canvas.getScaleY(), 1);

		//Threading is implemented by fillTraces, to allow a slightly different use of threads for remote
		controller.fillTraces(canvas, linesToPaint, xscale, yscale, changedBounds);


		// -------------------------------------------------------------------
		// Finalize the painting (to be implemented by the instance
		// -------------------------------------------------------------------

		//endPainting(linesToPaint, xscale, yscale);
		
		monitor.endProgress();

		changedBounds = false;

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
