package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;

import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.data.graph.CallPath;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;


/*******************************************************************************************
 * 
 * Class to store global information concerning the database and the trace.
 * The class is designed to work for both local and remote database. Any references have to 
 * 	be addressed to the methods of this class instead of the derived class to enable
 *  transparency.
 * 
 * @author Original authors: Sinchan Banarjee, Michael France, Reed Lundrum and Philip Taffet
 * 
 * Modification:
 * - 2013 Philip: refactoring into three classes : abstract (this class), local and remote
 * - 2014.2.1 Laksono: refactoring to make it as simple as possible and avoid code redundancy
 *
 *******************************************************************************************/
public abstract class SpaceTimeDataController 
{
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
	AtomicInteger lineNum, depthLineNum;
		
	/** The maximum depth of any single CallStackSample in any trace. */
	protected int maxDepth;
	
	protected ColorTable colorTable;
	private boolean enableMidpoint;
	
	protected IBaseData dataTrace = null;
	final protected BaseExperiment exp;
	

	/***
	 * Constructor to create a data based on File. This constructor is more suitable
	 * for local database
	 * 
	 * @param _window : SWT window
	 * @param expFile : experiment file
	 */
	public SpaceTimeDataController(IWorkbenchWindow _window, File expFile) {
		
		exp = new ExperimentWithoutMetrics();
		try {
			exp.open(expFile, new ProcedureAliasMap());
		} catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line "
					+ e.getLineNumber());
			e.printStackTrace();
			// return;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		init(_window);
	}
	
	/*****
	 * Constructor to create a data based on input stream, which is convenient for remote database
	 * 
	 * @param _window : SWT window
	 * @param expStream : input stream
	 * @param Name : the name of the file on the remote server
	 *****/
	public SpaceTimeDataController(IWorkbenchWindow _window, InputStream expStream, String Name) {
		
		exp = new ExperimentWithoutMetrics();

		try {
			// Without metrics, so param 3 is false
			exp.open(expStream, new ProcedureAliasMap(), Name);
		}
		catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line " + e.getLineNumber());
			e.printStackTrace();
			// return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		init(_window);
	}

	/******
	 * Initialize the object
	 * 
	 * @param _window
	 ******/
	protected void init(IWorkbenchWindow _window) {
		
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
		
		attributes = new ImageTraceAttributes();

		lineNum = new AtomicInteger(0);
		depthLineNum = new AtomicInteger(0);

		ISourceProviderService sourceProviderService = (ISourceProviderService) _window.getService(ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 

		TraceAttribute trAttribute = exp.getTraceAttribute();
		minBegTime = trAttribute.dbTimeMin;
		maxEndTime = trAttribute.dbTimeMax;

		dbName = exp.getName();

		painter = new PaintManager(attributes, colorTable, maxDepth);
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
	
	public int getNumberOfLines() {
		return lineNum.get();
	}
	
	public int getNumberOfDepthLines() {
		return depthLineNum.get();
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