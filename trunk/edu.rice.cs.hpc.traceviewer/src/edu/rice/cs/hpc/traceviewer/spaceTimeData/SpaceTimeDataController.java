package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public abstract class SpaceTimeDataController 
{
	protected PaintManager painter;
	
	//TODO: Some places access this directly while others use the getter.
	protected ImageTraceAttributes attributes;// Should this be final?
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
	final protected AtomicInteger lineNum;
	

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
	 * dtProcess scaled to be the index in traces[] that corresponds to this
	 * process. dtProcess has length equal to the number of files in data trace
	 * while scaledDTProcess typically has length equal to the number of vertical pixels in
	 * SpaceTimeDetailView.
	 */
	public int computeScaledProcess() {
		int numTracesShown = Math.min(attributes.endProcess - attributes.begProcess, attributes.numPixelsV);
		int selectedProc = getCurrentlySelectedProcess();
		double scaledDTProcess = (((double) numTracesShown -1 )
					/ ((double) attributes.endProcess - attributes.begProcess - 1) * (selectedProc - attributes.begProcess));
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
		
		int currentDepthLineNum = lineNum.getAndIncrement();
		if (currentDepthLineNum < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(currentDepthLineNum,
					scopeMap, dataTrace, getCurrentlySelectedProcess(), attributes.numPixelsH,
					attributes.endTime - attributes.begTime, minBegTime
							+ attributes.begTime);

			toDonate.copyDataFrom(depthTrace);

			return toDonate;
		} else
			return null;
	}
	
	public abstract IBaseData getBaseData();
	public abstract String[] getTraceNames();

	/******************************************************************************
	 * Returns number of processes (ProcessTimelines) held in this
	 * SpaceTimeData.
	 ******************************************************************************/
	public int getTotalTraceCount() {
		return totalTraceCountInDB;
	}
	
	
	public HashMap<Integer, CallPath> getScopeMap() {
		return scopeMap;
	}

	/******************************************************************************
	 * getter/setter trace attributes
	 * @return
	 ******************************************************************************/
	public long getTimeBegin() {
		return attributes.begTime;
	}
	
	public long getTimeEnd() {
		return attributes.endTime;
	}
	
	public int getProcessBegin() {
		return attributes.begProcess;
	}
	
	public int getProcessEnd() {
		return attributes.endProcess;
	}
	
	public int getPixelHorizontal() {
		return attributes.numPixelsH;
	}
	
	public ImageTraceAttributes getTraceAttributes() {
		return attributes;
	}
	
	public void setTraceAttributes(ImageTraceAttributes attributes) {
		this.attributes = attributes;
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

	public ImageTraceAttributes getAttributes() {
		return attributes;
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
	}

	public abstract void closeDB();


}