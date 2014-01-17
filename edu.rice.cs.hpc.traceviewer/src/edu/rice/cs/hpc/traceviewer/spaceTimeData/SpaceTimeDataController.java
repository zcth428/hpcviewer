package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public abstract class SpaceTimeDataController {

	PaintManager painter;

	
	protected ImageTraceAttributes attributes;
	protected String dbName;
	/**
	 * The minimum beginning and maximum ending time stamp across all traces (in
	 * microseconds)).
	 */
	protected long maxEndTime, minBegTime;


	protected ProcessTimelineService ptlService;

	
	/** The map between the nodes and the cpid's. */
	protected HashMap<Integer, CallPath> scopeMap;
	
	// We probably want to get away from this. The for code that needs it should be
	// in one of the threads. It's here so that both local and remote can use
	// the same thread class yet get their information differently.
	final AtomicInteger lineNum, depthLineNum;
	

	/**
	 * The number of processes in the database, independent of the current
	 * display size
	 */
	protected int totalTraceCountInDB;
	
	/** The maximum depth of any single CallStackSample in any trace. */
	protected int maxDepth;
	
	protected ColorTable colorTable;
	private boolean enableMidpoint;
	
	protected IBaseData dataTrace = null;
	// So, I'd like to declare attributes and dbName final and give them their
	// values in the child class's constructor, but that doesn't work in Java.
	// Alternatively, I'd be fine with making them final and having the
	// constructor for this class take them in and set them, but the
	// superclass's constructor has to be the first line in the method, and
	// dbName isn't available on the first line. So the responsibility is left
	// to the child classes: please set attributes and dbName in your
	// constructor.

	public SpaceTimeDataController() {
		attributes = new ImageTraceAttributes();

		lineNum = new AtomicInteger(0);
		depthLineNum = new AtomicInteger(0);
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


	private int getCurrentlySelectedProcess()
	{
		return painter.getPosition().process;
	}
	
	/**
	 * {@link getCurrentlySelectedProcess()} returns something on [begProcess,
	 * endProcess-1]. We need to map that to something on [0, numTracesShown -
	 * 1]. We use a simple linear mapping:
	 * begProcess    -> 0,
	 * endProcess-1  -> numTracesShown-1
	 */
	public int computeScaledProcess() {
		int numTracesShown = Math.min(attributes.getProcessInterval(), attributes.numPixelsV);
		int selectedProc = getCurrentlySelectedProcess();
		
		double scaledDTProcess = (((double) numTracesShown -1 )
					/ ((double) attributes.getProcessInterval() - 1) * 
					(selectedProc - attributes.getProcessBegin()));
		return (int)scaledDTProcess;
	}


	public PaintManager getPainter() {
		
		return painter;
	}



	public ProcessTimeline getDepthTrace() {
		int scaledDTProcess = computeScaledProcess();
		return  ptlService.getProcessTimeline(scaledDTProcess);
	}
	
	public abstract ProcessTimeline getNextTrace(boolean changedBounds);
	
	/***********************************************************************
	 * Gets the next available trace to be filled/painted from the DepthTimeView
	 * 
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextDepthTrace() {
		
		ProcessTimeline depthTrace = getDepthTrace();
		if (depthTrace == null)
			return null;
		
		int currentDepthLineNum = depthLineNum.getAndIncrement();
		if (currentDepthLineNum < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(currentDepthLineNum,
					scopeMap, dataTrace, getCurrentlySelectedProcess(), attributes.numPixelsH,
					attributes.getTimeInterval(), minBegTime
							+ attributes.getTimeBegin());

			toDonate.copyDataFrom(depthTrace);

			return toDonate;
		} else
			return null;
	}
	
	public IBaseData getBaseData(){
		return dataTrace;
	}

	/******************************************************************************
	 * Returns number of processes (ProcessTimelines) held in this
	 * SpaceTimeData.
	 ******************************************************************************/
	public int getTotalTraceCount() {
		return dataTrace.getNumberOfRanks();
	}
	
	public HashMap<Integer, CallPath> getScopeMap() {
		return scopeMap;
	}

	/******************************************************************************
	 * getter/setter trace attributes
	 * @return
	 ******************************************************************************/
	
	public int getPixelHorizontal() {
		return attributes.numPixelsH;
	}
	
	
	public void setTraceAttributes(ImageTraceAttributes attributes) {
		this.attributes = attributes;
	}

	public ImageTraceAttributes getAttributes() {
		return attributes;
	}

	/*************************************************************************
	 * Returns width of the spaceTimeData: The width (the last time in the
	 * ProcessTimeline) of the longest ProcessTimeline.
	 ************************************************************************/
	public long getTimeWidth() {
		return maxEndTime - minBegTime;
	}

	public String getName() {
		return dbName;
	}

	public long getMaxEndTime() {
		return maxEndTime;
	}

	public long getMinBegTime() {
		return minBegTime;
	}

	public ColorTable getColorTable() {
		return colorTable;
	}

	public void dispose() {
		colorTable.dispose();
	}

	public void setEnableMidpoint(boolean enable) {
		this.enableMidpoint = enable;
	}

	public boolean isEnableMidpoint() {
		return enableMidpoint;
	}

	public void resetCounters() {
		lineNum.set(0);
		depthLineNum.set(0);
	}

	public abstract void closeDB();

	//see the note where this is called in FilterRanks
	public IFilteredData getFilteredBaseData() {
		if (dataTrace instanceof IFilteredData)
			return (IFilteredData) dataTrace;
		return null;
	}
	/**
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param filteredBaseData
	 */
	public void setBaseData(IFilteredData filteredBaseData) {
		dataTrace = filteredBaseData;
		// we have to change the range of displayed processes
		//attributes.begProcess = 0;

		int endProcess = attributes.getProcessEnd();
		int begProcess = attributes.getProcessBegin();
		
		//Snap it back into the acceptable limits.
		if (endProcess > dataTrace.getNumberOfRanks())
			endProcess  = dataTrace.getNumberOfRanks();
		
		if (begProcess >= endProcess)
			begProcess = 0;
		
		attributes.setProcess(begProcess, endProcess);
	}

	public abstract IFilteredData createFilteredBaseData();

	public abstract void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch)
			throws IOException;



}