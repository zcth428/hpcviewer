package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.text.NumberFormat;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.events.TraceEvents;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.BaseViewPaint;
import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

/**
 * This contains the painting components from SpaceTimeData
 * 
 * @author Philip Taffet and original authors
 * 
 */

public class PaintManager extends TraceEvents {

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

	/** Stores the current depth that is being displayed. */
	private int currentDepth;

	private IStatusLineManager statusMgr;
	final private IWorkbenchWindow window;
	
	/** Stores the current position of cursor */
	private Position currentPosition;
	
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
	
	private int numTraces;
	
	private SpaceTimeDataController controller;

	public PaintManager(ImageTraceAttributes _attributes,
			ImageTraceAttributes _oldAttributes, IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, ColorTable _colorTable, int _maxDepth,
			long _minBegTime, SpaceTimeDataController spaceTimeDataController) {
		
		attributes = _attributes;
		oldAttributes = _oldAttributes;
		window = _window;
		statusMgr = _statusMgr;
		
		statusMgr.getProgressMonitor();//This was in the SpaceTimeData ctor before. I guess it initializes something because it's result isn't actually used.
		
		colorTable = _colorTable;
		colorTable.setColorTable();
		
		maxDepth = _maxDepth;
		
		minBegTime = _minBegTime;
		
		//defaut position
		currentPosition = new Position(0, 0);
		
		controller = spaceTimeDataController;
	}

	/*************************************************************************
	 * paint a space time detail line
	 * 
	 * @param spp
	 * @param process
	 * @param height
	 * @param changedBounds
	 *************************************************************************/
	public void paintDetailLine(SpaceTimeSamplePainter spp, int process,
			int height, boolean changedBounds) {

		ProcessTimeline ptl = controller.getTrace(process);
		if (ptl == null || ptl.size() < 2)
			return;

		if (changedBounds)
			ptl.shiftTimeBy(minBegTime);
		double pixelLength = (attributes.endTime - attributes.begTime)
				/ (double) attributes.numPixelsH;//Time per pixel

		// do the paint
		BasePaintLine detailPaint = new BasePaintLine(colorTable, ptl, spp,
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
	 ********************************************************************/
	public void paintDepthLine(SpaceTimeSamplePainter spp, int depth, int height) {
		// System.out.println("I'm painting process "+process+" at depth "+depth);
		
		ProcessTimeline ptl = controller.getDepthTrace();

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

	// TODO: Redirect calls to this accessor and mutator. The old STData class
	// doesn't need currentDepth
	public void setDepth(int _depth) {
		this.currentDepth = _depth;
	}

	public int getDepth() {
		return this.currentDepth;
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

		attributes.lineNum = 0; 

		BaseViewPaint detailPaint = new BaseViewPaint(
				changedBounds, this.statusMgr, window, _controller) {

			// @Override
			protected boolean startPainting(int linesToPaint,
					boolean changedBounds) {
				if (compositeFinalLines != null)
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
				}
				printMemInfo();
				System.gc();
				printMemInfo();
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

			private void printMemInfo() {
				Runtime runtime = Runtime.getRuntime();

			    NumberFormat format = NumberFormat.getInstance();

			    StringBuilder sb = new StringBuilder();
			    long maxMemory = runtime.maxMemory();
			    long allocatedMemory = runtime.totalMemory();
			    long freeMemory = runtime.freeMemory();

			    sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
			    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
			    sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
			    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
			    System.out.println(sb.toString());
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

		attributes.lineNum = 0;
		attributes.numPixelsDepthV = _numPixelsV;
		attributes.setTime(_begTime, _endTime);

		controller.setCurrentlySelectedProccess(currentPosition.process);
		oldAttributes.copy(attributes);

		BaseViewPaint depthPaint = new BaseViewPaint(
				changedBounds, this.statusMgr, window, _controller) {

			// @Override
			protected boolean startPainting(int linesToPaint,
					boolean changedBounds) {
				controller.prepareDepthViewportPainting();
				//TODO: Get the depth viewport working
				//compositeFinalLines = new Image[linesToPaint];
				return changedBounds;
			}

			// @Override
			protected void endPainting(int linesToPaint, double xscale,
					double yscale) {

				for (int i = 0; i < linesToPaint; i++) {
					masterGC.drawImage(
							compositeFinalLines[i],
							0,
							0,
							compositeFinalLines[i].getBounds().width,
							compositeFinalLines[i].getBounds().height,
							0,
							(int) Math.round(i * attributes.numPixelsDepthV
									/ (float) maxDepth),
							compositeFinalLines[i].getBounds().width,
							compositeFinalLines[i].getBounds().height);
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

	@Override
	public void setPosition(Position position) {
		// TODO Auto-generated method stub
		// The controller actually needs the position, and I don't know if the
		// PaintManger does. Should the PaintManager share the data with the
		// controller some how, or will this not be an issue once TraceEvents is
		// removed.
		this.currentPosition = position;
	}
}
