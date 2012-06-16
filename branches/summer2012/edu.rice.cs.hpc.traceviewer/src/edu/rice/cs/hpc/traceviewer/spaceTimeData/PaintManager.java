package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import edu.rice.cs.hpc.traceviewer.events.TraceEvents;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

/**
 * This contains the painting components from SpaceTimeData
 * 
 * @author Philip Taffet and original authors
 * 
 */

public class PaintManager extends TraceEvents {

	private ImageTraceAttributes attributes;// I think this is only set in the
	// constructor of the old SpaceTimeData, so it can just be passed in once
	// and not need to change after that. Alternatively, since the other is
	// final public, I could just get it from there.

	// These are also not changed, so have the owner of this class set them in
	// the constructor.
	/** The minimum beginning time stamp across all traces (in microseconds)). */
	private long minBegTime;

	/**
	 * Stores the color to function name assignments for all of the functions in
	 * all of the processes.
	 */
	private ColorTable colorTable;

	/** Stores the current depth that is being displayed. */
	private int currentDepth;

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

		// TODO: Implement the method. I guess PaintManager will have a
		// reference to Controller that can be set in the constructor.
		ProcessTimeline ptl = Controller.GetATrace(process);
		if (ptl == null || ptl.size() < 2)
			return;

		if (changedBounds)
			ptl.shiftTimeBy(minBegTime);
		double pixelLength = (attributes.endTime - attributes.begTime)
				/ (double) attributes.numPixelsH;

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
		ProcessTimeline ptl = depthTrace;

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

	@Override
	public void setPosition(Position position) {
		// TODO Auto-generated method stub
		// The controller actually needs the position, and I don't know if the
		// PaintManger does. Should the PaintManager share the data with the
		// controller some how, or will this not be an issue once TraceEvents is
		// removed.
	}
}
