package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.lang.Math;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
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
	
	/**The minimum beginning and maximum ending time stamp across all traces (in microseconds)).*/
	private long minBegTime;
	private long maxEndTime;
	
	final public ImageTraceAttributes attributes;
	
	final private ImageTraceAttributes oldAttributes;
	
	/**The process to be painted in the depth time viewer.*/
	private int dtProcess;
		
	/** Stores the current depth that is being displayed.*/
	private int currentDepth;
	
	/** Stores the current position of cursor */
	private Position currentPosition;
	
	private String dbName;
	
	final private boolean debug =  true;
	
	private IStatusLineManager statusMgr;
	final private IWorkbenchWindow window;
	
	private BaseDataFile dataTrace;
	
	private int HEADER_SIZE;
	
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
		try
		{
			dataTrace = new BaseDataFile(traceFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			System.err.println("Master buffer could not be created");
		}
		
		System.out.println("Reading experiment database file '" + expFile.getPath() + "'");

		BaseExperiment exp = new ExperimentWithoutMetrics(expFile);
		try
		{
			exp.open(expFile);
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
		
		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);	
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);
		
		colorTable.setColorTable();
		
		minBegTime = exp.getTraceAttribute().dbTimeMin;
		maxEndTime = exp.getTraceAttribute().dbTimeMax;
		
		// default position
		this.currentPosition = new Position(0,0);
		this.dbName = exp.getName();
		//System.gc();
		
		final TraceAttribute attribute = exp.getTraceAttribute();
		HEADER_SIZE = attribute.dbHeaderSize;
	}

	public String getName()
	{
		return this.dbName;
	}
	
	public void setDepth(int _depth)
	{
		this.currentDepth = _depth;
	}
	
	public int getDepth()
	{
		return this.currentDepth;
	}
	/*************************************************************************
	 *	Returns width of the spaceTimeData:
	 *	The width (the last time in the ProcessTimeline) of the longest 
	 *	ProcessTimeline. 
	 ************************************************************************/
	public long getWidth()
	{
		return maxEndTime - minBegTime;
	}
	
	/******************************************************************************
	 *	Returns number of processes (ProcessTimelines) held in this SpaceTimeData.
	 ******************************************************************************/
	public int getHeight()
	{
		return dataTrace.getNumberOfFiles();
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
		return minBegTime;
	}

	/*************************************************************************
	 * @return the highest end time of all of the process time lines
	 *************************************************************************/
	public long getMaxBegTime()
	{
		return maxEndTime;
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
			int _begProcess, int _endProcess, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV)
	{	
		boolean changedBounds = !attributes.sameTrace(oldAttributes);

		attributes.numPixelsH = _numPixelsH;
		attributes.numPixelsV = _numPixelsV;
		
		oldAttributes.copy(attributes);

		attributes.lineNum = 0;
		
		BaseViewPaint detailPaint = new BaseViewPaint(this, attributes, changedBounds, this.statusMgr, window) {

			@Override
			protected boolean startPainting(int linesToPaint, boolean changedBounds) {
				compositeOrigLines = new Image[linesToPaint];
				compositeFinalLines = new Image[linesToPaint];

				if (changedBounds) {
					final int num_traces = Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
					traces = new ProcessTimeline[ num_traces ];
				}
				return true;
			}

			@Override
			protected void endPainting(int linesToPaint, double xscale, double yscale) {
				for (int i = 0; i < linesToPaint; i++) {
					int yposition = (int) Math.round(i * yscale);
					origGC.drawImage(compositeOrigLines[i], 0, yposition);
					masterGC.drawImage(compositeFinalLines[i], 0, yposition);
				}
			}

			@Override
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

			@Override
			protected boolean startPainting(int linesToPaint, boolean changedBounds) {
				depthTrace = new ProcessTimeline(lineNum, scopeMap, dataTrace, dtProcess, 
						attributes.numPixelsH, attributes.endTime-attributes.begTime,
						minBegTime+attributes.begTime, HEADER_SIZE);
				
				depthTrace.readInData(getHeight());
				depthTrace.shiftTimeBy(minBegTime);
				compositeFinalLines = new Image[linesToPaint];

				return changedBounds;
			}

			@Override
			protected void endPainting(int linesToPaint, double xscale,
					double yscale) {

				for (int i = 0; i < linesToPaint; i++)
				{
					masterGC.drawImage(compositeFinalLines[i], 0, 0, compositeFinalLines[i].getBounds().width, 
							compositeFinalLines[i].getBounds().height, 0,(int)Math.round(i*attributes.numPixelsDepthV/(float)maxDepth), 
							compositeFinalLines[i].getBounds().width, compositeFinalLines[i].getBounds().height);
				}
			}

			@Override
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
			@Override
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
			ptl.shiftTimeBy(minBegTime);
		double pixelLength = (attributes.endTime - attributes.begTime)/(double)attributes.numPixelsH;
		
		// do the paint
		BasePaintLine detailPaint = new BasePaintLine(colorTable, ptl, spp, attributes.begTime, currentDepth, height, pixelLength)
		{
			@Override
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
		return traces[process];
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
						attributes.endTime-attributes.begTime, minBegTime + attributes.begTime, 
						HEADER_SIZE);
			else
				return traces[attributes.lineNum-1];
		}
		else
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
					attributes.numPixelsH, attributes.endTime-attributes.begTime, minBegTime+attributes.begTime,
					HEADER_SIZE);
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
	
	@Override
	public void setPosition(Position position)
	{
		this.currentPosition = position;
	}
	
	public Position getPosition()
	{
		return this.currentPosition;
	}
	
	
	public BaseDataFile getTraceData()
	{
		return this.dataTrace;
	}
	
	private void printDebug(String str)
	{
		if (this.debug)
			System.err.println(str);
	}
}