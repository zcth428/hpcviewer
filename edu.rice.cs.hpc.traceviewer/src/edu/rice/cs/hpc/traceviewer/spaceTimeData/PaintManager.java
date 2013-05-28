package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.db.DecompressionAndRenderThread;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.BaseViewPaint;
import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.util.Constants;

/**
 * This contains the painting components from SpaceTimeData
 * 
 * @author Philip Taffet and original authors
 * 
 */

public class PaintManager {

	// I think this is only set in the
	// constructor of the old SpaceTimeData, so it can just be passed in once
	// and not need to change after that? Alternatively, since the other is
	// final public, I could just get it from there.
	private ImageTraceAttributes attributes;
	final private ImageTraceAttributes oldAttributes;

	// These are also not changed, so have the owner of this class set them in
	// the constructor.
	/** The minimum beginning time stamp across all traces (in microseconds)). */
	private long minBegTime;
	
	/** The maximum depth of any single CallStackSample in any trace. */
	private int maxDepth;

	/**
	 * Stores the color to function name assignments for all of the functions in
	 * all of the processes.
	 */
	private ColorTable colorTable;



	private IStatusLineManager statusMgr;
	final private IWorkbenchWindow window;
	
	/** Stores the current position of cursor */
	
	private Position currentPosition;
	/** Stores the current depth and data object that are being displayed.*/
	private int currentDepth;
	private int currentDataIdx;
	
	/**
	 * The composite images created by painting all of the samples in a given
	 * line to it.
	 */
	private Image[] compositeFinalLines;

	/**
	 * The composite images created by painting all of the samples in a given
	 * line to it.
	 */
	private Image[] compositeOrigLines;
	
	/**
	 * The composite image of all the lines that make up the depth trace
	 */
	private Image[] compositeDepthLines;
	
	private final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;
	
	
	private final TimelineProgressMonitor monitor;

	public PaintManager(ImageTraceAttributes _attributes,
			ImageTraceAttributes _oldAttributes, IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, ColorTable _colorTable, int _maxDepth,
			long _minBegTime) {
		
		attributes = _attributes;
		oldAttributes = _oldAttributes;
		window = _window;
		statusMgr = _statusMgr;
		
		statusMgr.getProgressMonitor();//This was in the SpaceTimeData ctor before. I guess it initializes something because it's result isn't actually used.
		monitor = new TimelineProgressMonitor(statusMgr);
		
		colorTable = _colorTable;
		colorTable.setColorTable();
		
		maxDepth = _maxDepth;
		
		minBegTime = _minBegTime;
		
		//defaut position
		this.currentDepth = 0;
		this.currentDataIdx = Constants.dataIdxNULL;
		currentPosition = new Position(0, 0);
	}

	/*************************************************************************
	 * paint a space time detail line
	 * 
	 * @param spp
	 * @param process
	 * @param height
	 * @param changedBounds
	 *************************************************************************/
	public void paintDetailLine(SpaceTimeSamplePainter spp, ProcessTimeline Trace,
			int height, boolean changedBounds) {

		
		if (Trace == null || Trace.size() < 2)
			return;

		if (changedBounds)
			Trace.shiftTimeBy(minBegTime);
		double pixelLength = (attributes.endTime - attributes.begTime)
				/ (double) attributes.numPixelsH;//Time per pixel

		// do the paint
		BasePaintLine detailPaint = new BasePaintLine(colorTable, Trace, spp,
				attributes.begTime, currentDepth, height, pixelLength) {
			// @Override
			public void finishPaint(int currSampleMidpoint,
					int succSampleMidpoint, int currDepth, String functionName,
					int sampleCount) {
				DetailSpaceTimePainter dstp = (DetailSpaceTimePainter) spp;
				dstp.paintSample(currSampleMidpoint, succSampleMidpoint,
						height, functionName);

				final boolean isOverDepth = (currDepth < depth);
				
				// write texts (depth and number of samples) if needed
				dstp.paintOverDepthText(currSampleMidpoint,
						Math.min(succSampleMidpoint, attributes.numPixelsH),
						currDepth, functionName, isOverDepth, sampleCount);
			}
		};
		detailPaint.paint();
		
	}

	/**********************************************************************
	 * Paints one "line" (the timeline for one processor) to its own image,
	 * which is later copied to a master image with the rest of the lines.
	 * 
	 ********************************************************************/
	public void paintDepthLine(SpaceTimeSamplePainter spp, int depth, int height, ProcessTimeline ptl) {
		// System.out.println("I'm painting process "+process+" at depth "+depth);
		
		

		if (ptl.size() < 2)
			return;

		double pixelLength = (attributes.endTime - attributes.begTime)
				/ (double) attributes.numPixelsH;
		BasePaintLine depthPaint = new BasePaintLine(colorTable, ptl, spp,
				attributes.begTime, depth, height, pixelLength) {
			// @Override
			public void finishPaint(int currSampleMidpoint,
					int succSampleMidpoint, int currDepth, String functionName,
					int sampleCount) {
				if (currDepth >= depth) {
					spp.paintSample(currSampleMidpoint, succSampleMidpoint,
							height, functionName);
				}
			}
		};

		// do the paint
		depthPaint.paint();
	}

	public void setDepth(int _depth) {
		this.currentDepth = _depth;
	}

	public int getDepth() {
		return this.currentDepth;
	}
	/***
	 * set the current index data
	 * This is used by data centric view 
	 * @param dataIdx
	 */
	public void setData(int dataIdx)
	{
		this.currentDataIdx = dataIdx;
	}
	
	/**
	 * get the current index data
	 * @return
	 */
	public int getData()
	{
		return this.currentDataIdx;
	}
	

	// Redirect these calls as well
	public int getBegProcess() {
		return attributes.begProcess;
	}

	public int getEndProcess() {
		return attributes.endProcess;
	}

	/*************************************************************************
	 * Paints the specified time units and processes at the specified depth on
	 * the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 * the sample's max depth before becoming overDepth on samples that have
	 * gone over depth.
	 * 
	 * @param masterGC
	 *            The GC that will contain the combination of all the 1-line
	 *            GCs.
	 * @param origGC
	 *            The original GC without texts
	 * @param canvas
	 *            The SpaceTimeDetailCanvas that will be painted on.
	 * @param begProcess
	 *            The first process that will be painted.
	 * @param endProcess
	 *            The last process that will be painted.
	 * @param begTime
	 *            The first time unit that will be displayed.
	 * @param endTime
	 *            The last time unit that will be displayed.
	 * @param numPixelsH
	 *            The number of horizontal pixels to be painted.
	 * @param numPixelsV
	 *            The number of vertical pixels to be painted.
	 *************************************************************************/
	public void paintDetailViewport(final GC masterGC, final GC origGC,
			SpaceTimeDetailCanvas canvas, int _begProcess, int _endProcess,
			long _begTime, long _endTime, int _numPixelsH, int _numPixelsV,
			boolean refreshData, SpaceTimeDataController _controller) {
		boolean changedBounds = (refreshData ? refreshData : !attributes
				.sameTrace(oldAttributes));

		attributes.numPixelsH = _numPixelsH;
		attributes.numPixelsV = _numPixelsV;

		oldAttributes.copy(attributes);

		//attributes.lineNum = 0; 

		BaseViewPaint detailPaint = new BaseViewPaint( _controller, attributes,
				changedBounds, window) {

			// @Override
			protected boolean startPainting(int linesToPaint,
					boolean changedBounds) {
				/*if (compositeFinalLines != null)
				{
					System.out.println("Disposing compFinalLines");
					for (int i = 0; i < compositeFinalLines.length; i++) {
						compositeFinalLines[i].dispose();
					}
				}
				if (compositeOrigLines != null)
				{
					System.out.println("Disposing compOrigLines");
					for (int i = 0; i < compositeOrigLines.length; i++) {
						compositeOrigLines[i].dispose();
					}
				}*/
				compositeOrigLines = new Image[linesToPaint];
				compositeFinalLines = new Image[linesToPaint];

				//Moved to prepareViewportPainting
				/*if (changedBounds) {
					numTraces = Math.min(attributes.numPixelsV,
							attributes.endProcess - attributes.begProcess);
					traces = new ProcessTimeline[numTraces];
				}*/
				return true;
			}

			// @Override
			protected void endPainting(int linesToPaint, double xscale,
					double yscale) {
				for (int i = 0; i < linesToPaint; i++) {
					int yposition = (int) Math.round(i * yscale);
					origGC.drawImage(compositeOrigLines[i], 0, yposition);
					masterGC.drawImage(compositeFinalLines[i], 0, yposition);
				}
			}

			// @Override
			protected int getNumberOfLines() {
				return Math.min(attributes.numPixelsV, attributes.endProcess
						- attributes.begProcess);
			}
		};

		detailPaint.paint(canvas);
	}
	
	/**
	 * Renders the SpaceTimeDetailView from an array of ProcessTimelines by enqueueing work to do for the DecompressionAndRenderThreads
	 * @param workThreads 
	 */
	public void renderTraces(ProcessTimeline[] _traces) {
		
		for (int i = 0; i < _traces.length; i++) {
			DecompressionAndRenderThread.workToDo.add(new DecompressionAndRenderThread.RenderItemToDo(i));
		}
	
		
		monitor.endProgress();
	}
	
	public synchronized void renderDepthTrace(SpaceTimeCanvas canvas, double scaleX,
			double scaleY, ProcessTimeline nextTrace, int width, ProcessTimeline depthTrace) {
		int imageHeight = (int) (Math
				.round(scaleY * (nextTrace.line() + 1)) - Math.round(scaleY
				* nextTrace.line()));
		if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
			imageHeight--;
		else
			imageHeight++;

		Image line = new Image(canvas.getDisplay(), width, imageHeight);
		GC gc = new GC(line);
		SpaceTimeSamplePainter spp = new SpaceTimeSamplePainter(gc,
				this.getColorTable(), scaleX, scaleY) {

			// @Override
			public void paintSample(int startPixel, int endPixel,
					int height, String function) {

				this.internalPaint(gc, startPixel, endPixel, height,
						function);
			}
		};

		this.paintDepthLine(spp, nextTrace.line(), imageHeight, depthTrace);
		gc.dispose();

		this.addNextDepthImage(line, nextTrace.line());
	}

	public synchronized void renderTrace(Canvas canvas, boolean changedBounds,
			double scaleX, double scaleY, int width, ProcessTimeline trace) {
		int imageHeight = (int) (Math
				.round(scaleY * (trace.line() + 1)) - Math.round(scaleY
				* trace.line()));// The height of each individual Image,
										// not the full vertical height of
										// the window
		if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
			imageHeight--;
		else
			imageHeight++;

		Image lineFinal = new Image(canvas.getDisplay(), width, imageHeight);
		Image lineOriginal = new Image(canvas.getDisplay(), width,
				imageHeight);
		GC gcFinal = new GC(lineFinal);
		GC gcOriginal = new GC(lineOriginal);

		SpaceTimeSamplePainter spp = this
				.CreateDetailSpaceTimePainter(gcOriginal, gcFinal, scaleX,
						scaleY);
		this.paintDetailLine(spp, trace,
				imageHeight, changedBounds);
		gcFinal.dispose();
		gcOriginal.dispose();
		this.addNextImage(lineOriginal, lineFinal,
				trace.line());
	}

	/*************************************************************************
	 * Paint the depth view
	 * 
	 * @param masterGC
	 * @param canvas
	 * @param _begTime
	 * @param _endTime
	 * @param _numPixelsH
	 * @param _numPixelsV
	 *************************************************************************/
	public void paintDepthViewport(final GC masterGC, DepthTimeCanvas canvas,
			long _begTime, long _endTime, int _numPixelsH, int _numPixelsV,
			SpaceTimeDataController _controller) {
		boolean changedBounds = true; // !( dtProcess == currentPosition.process
										// &&
										// attributes.sameDepth(oldAttributes));

		//attributes.lineNum = 0;
		attributes.numPixelsDepthV = _numPixelsV;
		attributes.setTime(_begTime, _endTime);

		setPosition(currentPosition);
		
		oldAttributes.copy(attributes);

		BaseViewPaint depthPaint = new BaseViewPaint(
				changedBounds, this.statusMgr, window, _controller) {

			// @Override
			protected boolean startPainting(int linesToPaint,
					boolean changedBounds) {
				controller.prepareDepthViewportPainting();
				//TODO: Get the depth viewport working
				compositeDepthLines = new Image[linesToPaint];
				return changedBounds;
			}

			// @Override
			protected void endPainting(int linesToPaint, double xscale,
					double yscale) {

				for (int i = 0; i < linesToPaint; i++) {
					masterGC.drawImage(
							compositeDepthLines[i],
							0,
							0,
							compositeDepthLines[i].getBounds().width,
							compositeDepthLines[i].getBounds().height,
							0,
							(int) Math.round(i * attributes.numPixelsDepthV
									/ (float) maxDepth),
									compositeDepthLines[i].getBounds().width,
									compositeDepthLines[i].getBounds().height);
				}
			}

			// @Override
			protected int getNumberOfLines() {
				return Math.min(attributes.numPixelsDepthV, maxDepth);
			}
		};

		depthPaint.paint(canvas);
	}

	/** Adds a painted Image to compositeLines - used by TimelineThreads. */
	public synchronized void addNextImage(Image line, int index) {
		compositeFinalLines[index] = line;
	}

	/** Adds a painted Image to compositeLines - used by TimelineThreads. */
	public synchronized void addNextImage(Image imgOriginal, Image imgFinal,
			int index) {
		compositeOrigLines[index] = imgOriginal;
		compositeFinalLines[index] = imgFinal;
	}
	
	public synchronized void addNextDepthImage(Image line, int index)
	{
		compositeDepthLines[index] = line;
	}

	/****
	 * dispose allocated native resources (image, colors, ...)
	 */
	public void dispose() {
		this.colorTable.dispose();
	}

	

	public int getNumberOfDisplayedProcesses() {
		return Math.min(attributes.numPixelsV,
				attributes.endProcess - attributes.begProcess);
	}
	
	/*************************************************************************
	 * Returns the ColorTable holding all of the color to function name
	 * associations for this SpaceTimeData.
	 ************************************************************************/
	public ColorTable getColorTable() {
		return colorTable;
	}
	/*************************************************************************
	 * Returns the lowest starting time of all of the ProcessTimelines.
	 ************************************************************************/
	public long getMinBegTime() {
		return minBegTime;
	}


	public long getViewTimeBegin() {
		return attributes.begTime;
	}

	public long getViewTimeEnd() {
		return attributes.endTime;
	}

	/*************************************************************************
	 * Returns the largest depth of all of the CallStackSamples of all of the
	 * ProcessTimelines.
	 ************************************************************************/
	public int getMaxDepth() {
		return maxDepth;
	}

	public Position getPosition() {
		return this.currentPosition;
	}
	
	public int getProcessRelativePosition(int numDisplayedProcess)
	{
		// general case
    	int estimatedProcess = (int) (currentPosition.process - attributes.begProcess);
    	
    	// case for num displayed processes is less than the number of processes
    	estimatedProcess = (int) ((float)estimatedProcess* 
    			((float)numDisplayedProcess/(attributes.endProcess-attributes.begProcess)));
    	
    	// case for single process
    	estimatedProcess = Math.min(estimatedProcess, numDisplayedProcess-1);
    	
    	return estimatedProcess;
	}
	/**
	 * This makes a new DetailSpaceTimePainter. The line of code that is in this
	 * method was in TimelineThread, but TimelineThread shouldn't need access to
	 * window, so this takes care of that issue.
	 * 
	 * @return
	 */
	public SpaceTimeSamplePainter CreateDetailSpaceTimePainter(GC gcOriginal, GC gcFinal, double scaleX, double scaleY) {
		return new DetailSpaceTimePainter(window, gcOriginal, gcFinal,
				colorTable, scaleX, scaleY);
	}

	public void setPosition(Position position) {
		// TODO Auto-generated method stub
		// The controller actually needs the position, and I don't know if the
		// PaintManger does. Should the PaintManager share the data with the
		// controller some how, or will this not be an issue once TraceEvents is
		// removed.
		this.currentPosition = position;
	}
	
	public void resetPosition(){
		if (currentPosition.process >= attributes.endProcess) {
			// if the current process is beyond the range, make it in the middle
			currentPosition.process = (attributes.endProcess >> 1);
		}
	}
}
