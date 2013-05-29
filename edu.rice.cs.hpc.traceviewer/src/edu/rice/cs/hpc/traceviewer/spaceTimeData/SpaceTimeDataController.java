package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public abstract class SpaceTimeDataController {

	PaintManager painter;
	static int[] MethodCounts = new int[15];
	//FIXME: Some places access this directly while others use the getter.
	public ImageTraceAttributes attributes;// Should this be final?
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
	
	/** The map between the nodes and the cpid's. */
	protected HashMap<Integer, CallPath> scopeMap;
	
	/**
	 * The currently selected process;
	 */
	int dtProcess;

	/**
	 * The number of processes in the database, independent of the current
	 * display size
	 */
	protected int height;
	
	/** The maximum depth of any single CallStackSample in any trace. */
	protected int maxDepth;
	
	protected ColorTable colorTable;
	

	// So, I'd like to declare attributes and dbName final and give them their
	// values in the child class's constructor, but that doesn't work in Java.
	// Alternatively, I'd be fine with making them final and having the
	// constructor for this class take them in and set them, but the
	// superclass's constructor has to be the first line in the method, and
	// dbName isn't available on the first line. So the responsibility is left
	// to the child classes: please set attributes and dbName in your
	// constructor.

	public SpaceTimeDataController() {
		super();
	}
	
	protected void buildScopeMapAndColorTable(IWorkbenchWindow _window,
			BaseExperiment exp) {
		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);

		// This probably isn't the best way. It seems like ColorTable should be
		// created and initialized by the PaintManager, however initializing the
		// ColorTable requires the experiment file, which the PaintManager
		// should not have because it might be done differently when the data is
		// fetched remotely. Additionally the STDController needs MaxDepth along
		// with PaintManager.
		colorTable = new ColorTable();
		// Initializes the CSS that represents time values outside of the
		// time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);

	}

	/**
	 * The storage location for all the ProcessTimelines
	 */
	int getCurrentlySelectedProcess()
	{
		return painter.getPosition().process;
	}


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
		return height;
	}
	
	public HashMap<Integer, CallPath> getScopeMap() {
		return scopeMap;
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
	
	public abstract IBaseData getBaseData();

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

	/** @Deprecated Use {@link PaintManager#getMaxDepth} instead.*/
	@Deprecated
	public int getMaxDepth() {
		return maxDepth;
	}

	public ColorTable getColorTable() {
		return colorTable;	
	}

	public void dispose() {
		colorTable.dispose();
	}

}