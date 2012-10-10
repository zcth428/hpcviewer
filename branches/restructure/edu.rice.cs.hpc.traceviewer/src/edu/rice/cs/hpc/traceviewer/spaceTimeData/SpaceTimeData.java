package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.io.File;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.events.TraceEvents;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.util.Constants;

/*************************************************************************
 * 
 *	SpaceTimeData stores and creates all of the data involved in creating
 *	the view including all of the ProcessTimelines.
 *
 ************************************************************************/
public class SpaceTimeData extends TraceEvents
{
	/** Stores the color to function name assignments for all of the functions in all of the processes.*/
	private ColorTable colorTable;
	
	/**The map between the nodes and the cpid's.*/
	private HashMap<Integer, CallPath> scopeMap;
	
	/**The maximum depth of any single CallStackSample in any trace.*/
	private int maxDepth;

	private TraceAttribute traceAttributes;
	
	final public ImageTraceAttributes attributes;
	
	/** Stores the current depth and data object that are being displayed.*/
	private int currentDepth;
	private int currentDataIdx;
	
	/** Stores the current position of cursor */
	private Position currentPosition;
	
	private String dbName;
	
	private IStatusLineManager statusMgr;
	final private IWorkbenchWindow window;
	
	private IBaseData dataTrace;
	final private File traceFile;
	
	/*************************************************************************
	 *	Creates, stores, and adjusts the ProcessTimelines and the ColorTable.
	 * @throws Exception 
	 ************************************************************************/
	public SpaceTimeData(IWorkbenchWindow window, File expFile, File traceFile, IStatusLineManager _statusMgr)
			throws Exception, InvalExperimentException
	{
		this.window = window;
		statusMgr = _statusMgr;

		attributes = new ImageTraceAttributes();
		
		statusMgr.getProgressMonitor();
		
		colorTable = new ColorTable(window.getShell().getDisplay());
		
		//Initializes the CSS that represents time values outside of the time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		
		this.traceFile = traceFile;

		BaseExperiment exp = new ExperimentWithoutMetrics();
		
		// when the open throws an exception, we cannot continue
		exp.open( expFile, new ProcedureAliasMap() );
		
		traceAttributes = exp.getTraceAttribute();
		if (traceAttributes == null) {
			throw new Exception("Invalid XML experiment file");
		}
		
		// record size = sizeof(long) + sizeof(int) = 24
		dataTrace = new BaseData(traceFile.getAbsolutePath(), traceAttributes.dbHeaderSize, 24);

		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);	
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);
		
		colorTable.setColorTable();
		
		// default position
		this.currentDepth = 0;
		this.currentDataIdx = Constants.dataIdxNULL;
		this.currentPosition = new Position(0,0);
		this.dbName = exp.getName();
	}

	/***
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param baseData
	 */
	public void setBaseData(IBaseData baseData) 
	{
		dataTrace = baseData;
		
		// we have to change the range of displayed processes
		attributes.begProcess = 0;
		
		// hack: for unknown reason, "endProcess" is exclusive.
		// TODO: we should change to inclusive just like begProcess
		attributes.endProcess = baseData.getNumberOfRanks();
		
		if (currentPosition.process >= attributes.endProcess) {
			// if the current process is beyond the range, make it in the middle
			currentPosition.process = (attributes.endProcess >> 1);
		}
	}
	
	public IStatusLineManager getStatusLineManager()
	{
		return statusMgr;
	}
	
	public IWorkbenchWindow getWindow()
	{
		return window;
	}
	
	public HashMap<Integer, CallPath> getScopeMap()
	{
		return scopeMap;
	}
	
	/***
	 * retrieve the object to access trace data
	 * @return
	 */
	public IBaseData getBaseData()
	{
		return dataTrace;
	}
	
	/***
	 * retrieve the trace file of this experiment
	 * @return
	 */
	public File getTraceFile()
	{
		return traceFile;
	}
	
	/***
	 * retrieve the attributes of the trace file
	 * @return
	 */
	public TraceAttribute getTraceAttribute()
	{
		return traceAttributes;
	}
	
	/***
	 * get the name of the experiment
	 * @return
	 */
	public String getName()
	{
		return this.dbName;
	}
	
	/**
	 * update the current depth
	 * @param _depth : the current depth
	 */
	public void setDepth(int _depth)
	{
		this.currentDepth = _depth;
	}
	
	/***
	 * get the current depth
	 * @return
	 */
	public int getDepth()
	{
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


	/*************************************************************************
	 *	Returns width of the spaceTimeData:
	 *	The width (the last time in the ProcessTimeline) of the longest 
	 *	ProcessTimeline. 
	 ************************************************************************/
	public long getWidth()
	{
		return getMaxBegTime() - getMinBegTime();
	}
	
	/******************************************************************************
	 *	Returns number of processes (ProcessTimelines) held in this SpaceTimeData.
	 ******************************************************************************/
	public int getHeight()
	{
		return dataTrace.getNumberOfRanks();
	}
	
	/*************************************************************************
	 *	Returns the ColorTable holding all of the color to function name 
	 *	associations for this SpaceTimeData.
	 ************************************************************************/
	public ColorTable getColorTable()
	{
		return colorTable;
	}
	
	/*************************************************************************
	 *	Returns the lowest starting time of all of the ProcessTimelines.
	 ************************************************************************/
	public long getMinBegTime()
	{
		return traceAttributes.dbTimeMin;
	}

	/*************************************************************************
	 * @return the highest end time of all of the process time lines
	 *************************************************************************/
	public long getMaxBegTime()
	{
		return traceAttributes.dbTimeMax;
	}
	
	public long getViewTimeBegin()
	{
		return attributes.begTime;
	}
	
	public long getViewTimeEnd()
	{
		return attributes.endTime;
	}

	/*************************************************************************
	 *	Returns the largest depth of all of the CallStackSamples of all of the
	 *	ProcessTimelines.
	 ************************************************************************/
	public int getMaxDepth()
	{
		return maxDepth;
	}

	
	/****
	 * dispose allocated native resources (image, colors, ...)
	 */
	public void dispose() 
	{
		colorTable.dispose();
		dataTrace.dispose();
	}
	
	public int getBegProcess()
	{
		return attributes.begProcess;
	}
	
	
	public int getEndProcess()
	{
		return attributes.endProcess;
	}
	
	//@Override
	public void setPosition(Position position)
	{
		this.currentPosition = position;
	}
	
	public Position getPosition()
	{
		return this.currentPosition;
	}
	
	public int getProcessRelativePosition(int numDisplayedProcess)
	{
    	int estimatedProcess = (int) (currentPosition.process - attributes.begProcess);			
    	return (int) ((float)estimatedProcess* ((float)numDisplayedProcess/(attributes.endProcess-attributes.begProcess)));
	}
}