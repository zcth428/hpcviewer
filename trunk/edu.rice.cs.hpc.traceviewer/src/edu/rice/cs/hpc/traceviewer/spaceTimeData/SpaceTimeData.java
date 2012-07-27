package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
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
import edu.rice.cs.hpc.traceviewer.util.Constants;

/*************************************************************************
 * 
 *	SpaceTimeData stores and creates all of the data involved in creating
 *	the view including all of the ProcessTimelines.
 *
 ************************************************************************/
public class SpaceTimeData extends TraceEvents
{
	/** Contains all of the ProcessTimelines. It's a HashMap because,
	 * due to the multithreading, the traces may not get added in order.
	 * So, each ProcessTimeline now knows which line it is, and the
	 * HashMap is a map between that line and the ProcessTimeline.*/
	private ProcessTimeline traces[];

	public ProcessTimeline depthTrace;
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeOrigLines;
	
	/** Stores the color to function name assignments for all of the functions in all of the processes.*/
	private ColorTable colorTable;
	
	/**The map between the nodes and the cpid's.*/
	private HashMap<Integer, CallPath> scopeMap;
	
	/**The maximum depth of any single CallStackSample in any trace.*/
	private int maxDepth;

	private TraceAttribute traceAttributes;
	
	final public ImageTraceAttributes attributes;
	
	final private ImageTraceAttributes oldAttributes;
	
	/**The process to be painted in the depth time viewer.*/
	private int dtProcess;
		
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
	 ************************************************************************/
	public SpaceTimeData(IWorkbenchWindow window, File expFile, File traceFile, IStatusLineManager _statusMgr)
	{
		this.window = window;
		statusMgr = _statusMgr;

		attributes = new ImageTraceAttributes();
		oldAttributes = new ImageTraceAttributes();
		
		statusMgr.getProgressMonitor();
		
		colorTable = new ColorTable(window.getShell().getDisplay());
		
		//Initializes the CSS that represents time values outside of the time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		
		this.traceFile = traceFile;

		BaseExperiment exp = new ExperimentWithoutMetrics();
		try
		{
			exp.open( expFile, new ProcedureAliasMap() );
		}
		catch (InvalExperimentException e)
		{
			System.out.println("Parse error in Experiment XML at line " + e.getLineNumber());
			e.printStackTrace();
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		traceAttributes = exp.getTraceAttribute();
		
		try
		{
			dataTrace = new BaseData(traceFile.getAbsolutePath(), traceAttributes.dbHeaderSize);
		}
		catch (IOException e)
		{
			System.err.println("Master buffer could not be created");
		}

		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);	
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);
		
		colorTable.setColorTable();
		
		// default position
		this.currentDepth = 0;
		this.currentDataIdx = Constants.dataIdxNULL;
		this.currentPosition = new Position(0,0);
		this.dbName = exp.getName();
		//System.gc();		
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


	/*************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 * 
	 *	@param masterGC   		 The GC that will contain the combination of all the 1-line GCs.
	 * 	@param origGC			 The original GC without texts
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *	@param begProcess        The first process that will be painted.
	 *	@param endProcess 		 The last process that will be painted.
	 *	@param begTime           The first time unit that will be displayed.
	 *	@param endTime 			 The last time unit that will be displayed.
	 *  @param numPixelsH		 The number of horizontal pixels to be painted.
	 *  @param numPixelsV		 The number of vertical pixels to be painted.
	 *************************************************************************/
	public void paintDetailViewport(final GC masterGC, final GC origGC, SpaceTimeDetailCanvas canvas, 
			int _begProcess, int _endProcess, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV,
			boolean refreshData)
	{	
		boolean changedBounds = (refreshData? refreshData : !attributes.sameTrace(oldAttributes) );
		
		
		attributes.numPixelsH = _numPixelsH;
		attributes.numPixelsV = _numPixelsV;
		
		oldAttributes.copy(attributes);

		attributes.lineNum = 0;
		
		BaseViewPaint detailPaint = new BaseViewPaint(this, attributes, changedBounds, this.statusMgr, window) {

			//@Override
			protected boolean startPainting(int linesToPaint, boolean changedBounds) {
				compositeOrigLines = new Image[linesToPaint];
				compositeFinalLines = new Image[linesToPaint];

				if (changedBounds) {
					final int num_traces = Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
					traces = new ProcessTimeline[ num_traces ];
				}
				return true;
			}

			//@Override
			protected void endPainting(int linesToPaint, double xscale, double yscale) {
				for (int i = 0; i < linesToPaint; i++) {
					int yposition = (int) Math.round(i * yscale);
					origGC.drawImage(compositeOrigLines[i], 0, yposition);
					masterGC.drawImage(compositeFinalLines[i], 0, yposition);
				}
				
				// disposing resources
				for (int i=0; i<linesToPaint; i++) {
					compositeOrigLines[i].dispose();
					compositeFinalLines[i].dispose();
				}
			}

			//@Override
			protected int getNumberOfLines() {
				return Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
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
			long _begTime, long _endTime, int _numPixelsH, int _numPixelsV)
	{
		boolean changedBounds = true ; //!( dtProcess == currentPosition.process && attributes.sameDepth(oldAttributes));

		attributes.lineNum = 0;
		attributes.numPixelsDepthV = _numPixelsV;
		attributes.setTime(_begTime, _endTime);
		
		dtProcess = currentPosition.process;
		oldAttributes.copy(attributes);
		
		BaseViewPaint depthPaint = new BaseViewPaint(this, attributes, changedBounds, this.statusMgr, window) {

			//@Override
			protected boolean startPainting(int linesToPaint, boolean changedBounds) {
				depthTrace = new ProcessTimeline(lineNum, scopeMap, dataTrace, dtProcess, 
						attributes.numPixelsH, attributes.endTime-attributes.begTime,
						getMinBegTime()+attributes.begTime);
				
				depthTrace.readInData(getHeight());
				depthTrace.shiftTimeBy(getMinBegTime());
				compositeFinalLines = new Image[linesToPaint];

				return changedBounds;
			}

			//@Override
			protected void endPainting(int linesToPaint, double xscale,
					double yscale) {

				for (int i = 0; i < linesToPaint; i++)
				{
					masterGC.drawImage(compositeFinalLines[i], 0, 0, compositeFinalLines[i].getBounds().width, 
							compositeFinalLines[i].getBounds().height, 0,(int)Math.round(i*attributes.numPixelsDepthV/(float)maxDepth), 
							compositeFinalLines[i].getBounds().width, compositeFinalLines[i].getBounds().height);
				}
				// disposing resources
				for (Image img: compositeFinalLines) {
					img.dispose();
				}
			}

			//@Override
			protected int getNumberOfLines() {
				return Math.min(attributes.numPixelsDepthV, maxDepth);
			}
		};
		
		depthPaint.paint(canvas);
	}
	

	
	/**********************************************************************
	 * Paints one "line" (the timeline for one processor) to its own image,
	 * which is later copied to a master image with the rest of the lines.
	 ********************************************************************/
	public void paintDepthLine(SpaceTimeSamplePainter spp, int depth, int height)
	{
		//System.out.println("I'm painting process "+process+" at depth "+depth);
		ProcessTimeline ptl = depthTrace;

		if (ptl.size() < 2)
			return;
		
		double pixelLength = (attributes.endTime - attributes.begTime)/(double)attributes.numPixelsH;
		BasePaintLine depthPaint = new BasePaintLine(colorTable, ptl, spp, attributes.begTime, depth, height, pixelLength)
		{
			//@Override
			public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName, int sampleCount)
			{
				if (currDepth >= depth)
				{
					spp.paintSample(currSampleMidpoint, succSampleMidpoint, height, functionName);
				}
			}
		};
		
		// do the paint
		depthPaint.paint();
	}

	
	/*************************************************************************
	 * paint a space time detail line 
	 *  
	 * @param spp
	 * @param process
	 * @param height
	 * @param changedBounds
	 *************************************************************************/
	public void paintDetailLine(SpaceTimeSamplePainter spp, int process, int height, boolean changedBounds)
	{
		ProcessTimeline ptl = traces[process];
		if (ptl == null || ptl.size()<2 )
			return;
		
		if (changedBounds)
			ptl.shiftTimeBy(getMinBegTime());
		double pixelLength = (attributes.endTime - attributes.begTime)/(double)attributes.numPixelsH;
		
		// do the paint
		BasePaintLine detailPaint = new BasePaintLine(colorTable, ptl, spp, attributes.begTime, currentDepth, height, pixelLength)
		{
			//@Override
			public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName, int sampleCount)
			{
				DetailSpaceTimePainter dstp = (DetailSpaceTimePainter) spp;
				dstp.paintSample(currSampleMidpoint, succSampleMidpoint, height, functionName);
				
				final boolean isOverDepth = (currDepth < depth);
				// write texts (depth and number of samples) if needed
				dstp.paintOverDepthText(currSampleMidpoint, Math.min(succSampleMidpoint, attributes.numPixelsH), 
						currDepth, functionName, isOverDepth, sampleCount);
			}
		};
		detailPaint.paint();
	}

	/*************************************************************************
	 *	Returns the process that has been specified.
	 ************************************************************************/
	public ProcessTimeline getProcess(int process)
	{
		int relativeProcess = process - attributes.begProcess;
		
		// in case of single process displayed
		if (relativeProcess >= traces.length)
			relativeProcess = traces.length - 1;
		
		return traces[relativeProcess];
	}

	public int getNumberOfDisplayedProcesses()
	{
		return traces.length;
	}
	 
	
	/**Returns the index of the file to which the line-th line corresponds.*/
	public int lineToPaint(int line)
	{
		int numTimelinesToPaint = attributes.endProcess - attributes.begProcess;
		if(numTimelinesToPaint > attributes.numPixelsV)
			return attributes.begProcess + (line * numTimelinesToPaint)/(attributes.numPixelsV);
		else
			return attributes.begProcess + line;
	}
	
	/***********************************************************************
	 * Gets the next available trace to be filled/painted
	 * @param changedBounds Whether or not the thread should get the data.
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextTrace(boolean changedBounds)
	{
		if(attributes.lineNum < Math.min(attributes.numPixelsV, attributes.endProcess-attributes.begProcess))
		{
			attributes.lineNum++;
			if(changedBounds)
				return new ProcessTimeline(attributes.lineNum-1, scopeMap, dataTrace, 
						lineToPaint(attributes.lineNum-1), attributes.numPixelsH, 
						attributes.endTime-attributes.begTime, getMinBegTime() + attributes.begTime);
			else {
				if (traces.length >= attributes.lineNum)
					return traces[attributes.lineNum-1];
				else
					System.err.println("STD error: trace paints " + traces.length + " < line number " + attributes.lineNum);
			}
		}
		return null;
	}
	
	/***********************************************************************
	 * Gets the next available trace to be filled/painted from the DepthTimeView
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextDepthTrace()
	{
		if (attributes.lineNum < Math.min(attributes.numPixelsDepthV, maxDepth))
		{
			if (attributes.lineNum==0)
			{
				attributes.lineNum++;
				return depthTrace;
			}
			ProcessTimeline toDonate = new ProcessTimeline(attributes.lineNum, scopeMap, dataTrace, dtProcess, 
					attributes.numPixelsH, attributes.endTime-attributes.begTime, getMinBegTime()+attributes.begTime);
			toDonate.copyData(depthTrace);
			
			attributes.lineNum++;
			return toDonate;
		}
		else
			return null;
	}
	
	/**Adds a filled ProcessTimeline to traces - used by TimelineThreads.*/
	public synchronized void addNextTrace(ProcessTimeline nextPtl)
	{
		traces[nextPtl.line()] = nextPtl;
	}
	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	public synchronized void addNextImage(Image line, int index)
	{
		compositeFinalLines[index] = line;
	}
	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	public synchronized void addNextImage(Image imgOriginal, Image imgFinal, int index)
	{
		compositeOrigLines[index] = imgOriginal;
		compositeFinalLines[index] = imgFinal;
	}
	
	/****
	 * dispose allocated native resources (image, colors, ...)
	 */
	public void dispose() 
	{
		this.colorTable.dispose();
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
	
	
	public IBaseData getTraceData()
	{
		return this.dataTrace;
	}
}