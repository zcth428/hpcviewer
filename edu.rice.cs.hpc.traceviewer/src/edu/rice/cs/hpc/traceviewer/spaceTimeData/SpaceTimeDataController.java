package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public abstract class SpaceTimeDataController {

	PaintManager painter;
	static int[] MethodCounts = new int[15];
	protected ImageTraceAttributes attributes;// Should this be final?
	protected String dbName;
	/**
	 * The minimum beginning and maximum ending time stamp across all traces (in
	 * microseconds)).
	 */
	long maxEndTime, minBegTime;

	/**
	 * The storage location for all the ProcessTimelines
	 */
	protected ProcessTimeline traces[];

	protected ProcessTimeline depthTrace;

	/**
	 * The number of processes in the database, independent of the current
	 * display size
	 */
	protected int Height;

	// So, I'd like to declare attributes and dbName final and give them their
	// values in the child class's constructor, but that doesn't work in Java.
	// Alternatively, I'd be fine with making them final and having the
	// constructor for this class take them in and set them, but the
	// superclass's constructor has to be the first line in the method, and
	// dbName isn't available on the first line. So the responsibility is left
	// to the child classes: please set attributes and dbName in your
	// constructor. Bad things will happen if you don't.

	public SpaceTimeDataController() {
		super();
	}

	public abstract void setCurrentlySelectedProccess(int ProcessNumber);

	public PaintManager getPainter() {
		MethodCounts[9]++;
		return painter;
	}

	/**
	 * Returns a trace from traces[x]
	 */
	public ProcessTimeline getTrace(int process) {
		return traces[process];
	}

	public ProcessTimeline getDepthTrace() {
		return depthTrace;
	}

	public abstract void prepareViewportPainting(boolean changedBounds);

	abstract void prepareDepthViewportPainting();

	public abstract String[] getTraceDataValuesX();

	/******************************************************************************
	 * Returns number of processes (ProcessTimelines) held in this
	 * SpaceTimeData.
	 ******************************************************************************/
	public int getHeight() {
		return Height;
	}


	/*************************************************************************
	 * Returns width of the spaceTimeData: The width (the last time in the
	 * ProcessTimeline) of the longest ProcessTimeline.
	 ************************************************************************/
	public long getWidth() {
		return maxEndTime - minBegTime;
	}

	public abstract void fillTraces(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds);

	/*************************************************************************
	 * Returns the process that has been specified.
	 ************************************************************************/
	public ProcessTimeline getProcess(int process) {
		MethodCounts[4]++;
		int relativeProcess = process - attributes.begProcess;

		// in case of single process displayed
		if (relativeProcess >= traces.length)
			relativeProcess = traces.length - 1;

		return traces[relativeProcess];
	}

	// public abstract void addNextTrace(ProcessTimeline nextPtl);//synchronized
	//
	// public abstract ProcessTimeline getNextDepthTrace();//synchronized

	// public abstract ProcessTimeline getNextTrace(boolean
	// changedBounds);//synchronized

	public String getName()
	{
		return dbName;
	}

	public ImageTraceAttributes getAttributes() {
		return attributes;
	}

	public long getMaxEndTime() {
		return maxEndTime;
	}

	public long getMinBegTime() {
		return minBegTime;
	}

}