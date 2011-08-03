package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.Math;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;
import edu.rice.cs.hpc.traceviewer.events.TraceEvents;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;

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
	private Image[] compositeLines;
	
	/**The byte buffer that contains all the data from the trace file*/
	public LargeByteBuffer masterBuff;
	
	/** Stores the color to function name assignments for all of the functions in all of the processes.*/
	private ColorTable colorTable;
	
	/**The map between the nodes and the cpid's.*/
	private HashMap<Integer, CallPath> scopeMap;
	
	/**The total number of traces.*/
	private int height;
	
	/**The maximum depth of any single CallStackSample in any trace.*/
	private int maxDepth;
	
	/**The minimum beginning and maximum ending time stamp across all traces (in microseconds)).*/
	private long minBegTime;
	private long maxEndTime;
	
	/**The beginning/end of the process range on the viewer.*/
	private int begProcess;
	private int endProcess;
	
	/**The process to be painted in the depth time viewer.*/
	private int dtProcess;
	
	/**The beginning/end of the time range on the viewer.*/
	private long begTime;
	private long endTime;
	
	/** The width of the detail canvas in pixels.*/
	private int numPixelsH;
	
	/** The height of the detail canvas in pixels.*/
	private int numPixelsV;
	
	/**The number of the line that's being processed (for threads).*/
	private int lineNum;
	
	/** Stores the current depth that is being displayed.*/
	private int currentDepth;
	
	/** Stores the current position of cursor */
	private Position currentPosition;
	
	private String dbName;
	
	final private boolean debug =  true;
	
	private final IProgressMonitor monitor;
	
	/*************************************************************************
	 *	Creates, stores, and adjusts the ProcessTimelines and the ColorTable.
	 ************************************************************************/
	public SpaceTimeData(Display display, File expFile, File traceFile, IProgressMonitor _monitor)
	{
		this.monitor = _monitor;
		
		colorTable = new ColorTable(display);
		
		//Initializes the CSS that represents time values outside of the time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		try
		{
			FileChannel f = new RandomAccessFile(traceFile, "r").getChannel();
			masterBuff = new LargeByteBuffer(f);
		}
		catch (IOException e)
		{
			System.err.println("Master buffer could not be created");
		}
		
		System.out.println("Reading experiment database file '" + expFile.getPath() + "'");

		Experiment exp = new Experiment(expFile);
		try
		{
			exp.open(false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InvalExperimentException e)
		{
			System.out.println("Parse error in Experiment XML at line " + e.getLineNumber());
			e.printStackTrace();
			return;
		}
		
		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);	
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);
		
		colorTable.setColorTable();
		
		minBegTime = exp.trace_minBegTime;
		maxEndTime = exp.trace_maxEndTime;
		
		height = masterBuff.getInt(0);
		
		// default position
		this.currentPosition = new Position(0,0);
		this.dbName = exp.getName();
		//System.gc();
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
		return height;
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
		return this.begTime;
	}
	
	public long getViewTimeEnd()
	{
		return this.endTime;
	}

	/*************************************************************************
	 *	Returns the largest depth of all of the CallStackSamples of all of the
	 *	ProcessTimelines.
	 ************************************************************************/
	public int getMaxDepth()
	{
		return maxDepth;
	}
	
	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param masterGC   		 The GC that will contain the combination of all the 1-line GCs.
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *	@param begProcess        The first process that will be painted.
	 *	@param endProcess 		 The last process that will be painted.
	 *	@param begTime           The first time unit that will be displayed.
	 *	@param endTime 			 The last time unit that will be displayed.
	 *  @param numPixelsH		 The number of horizontal pixels to be painted.
	 *  @param numPixelsV		 The number of vertical pixels to be painted.
	 ***********************************************************************************/
	public void paintDetailViewport(GC masterGC, SpaceTimeDetailCanvas canvas, int _begProcess, int _endProcess, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV)
	{
		//long programTime = System.currentTimeMillis();
		boolean changedBounds = true;
		if (begTime == _begTime && endTime == _endTime && begProcess == _begProcess && endProcess == _endProcess && numPixelsH == _numPixelsH && numPixelsV == _numPixelsV)
			changedBounds = false;
		else
			traces = new ProcessTimeline[Math.min(_numPixelsV, _endProcess - _begProcess)];

		begTime = _begTime;
		endTime = _endTime;
		begProcess = _begProcess;
		endProcess = _endProcess;
		numPixelsH = _numPixelsH;
		numPixelsV = _numPixelsV;
		
		//depending upon how zoomed out you are, the iteration you will be making will be either the number of pixels or the processor
		int linesToPaint = Math.min(numPixelsV, endProcess - begProcess);
		final int num_threads = Math.min(linesToPaint, Runtime.getRuntime().availableProcessors());

		monitor.beginTask("Trace painting", (2 * num_threads) + linesToPaint);
		
		compositeLines = new Image[linesToPaint];
		lineNum = 0;
		TimelineThread[] threads = new TimelineThread[num_threads];
		
		for (int threadNum = 0; threadNum < threads.length; threadNum++)
		{
			threads[threadNum] = new TimelineThread(this, changedBounds, canvas, numPixelsH, canvas.getScaleX(), Math.max(canvas.getScaleY(), 1));
			threads[threadNum].start();
			monitor.worked(1);
		}
		
		try
		{
			for (int threadNum = 0; threadNum < threads.length; threadNum++)
			{
				if (threads[threadNum].isAlive())
					threads[threadNum].join();
				monitor.worked(1);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		for (int i = 0; i < linesToPaint; i++)
		{
			masterGC.drawImage(compositeLines[i], 0, 0, compositeLines[i].getBounds().width, compositeLines[i].getBounds().height, 0, (int)Math.round(i*Math.max(canvas.getScaleY(),1)), 
					compositeLines[i].getBounds().width, compositeLines[i].getBounds().height);
			monitor.worked(1);
		}
		monitor.done();
	}
	
	public void paintDepthViewport(GC masterGC, DepthTimeCanvas canvas, long _begTime, long _endTime, int _numPixelsH, int _numPixelsV)
	{
		boolean changedBounds = true;
		int process = this.currentPosition.process;
		
		if (begTime == _begTime && endTime == _endTime && dtProcess == process && numPixelsH == _numPixelsH && numPixelsV == _numPixelsV)
		{
			changedBounds = false;
		}
		else
		{
			depthTrace = null;
		}
		
		//depending upon how zoomed out you are, the iteration you will be making will be either the number of pixels or the processor
		//long programTime = System.currentTimeMillis();
		int linesToPaint = Math.min(_numPixelsV, maxDepth);
		if (changedBounds)
		{
			begTime = _begTime;
			endTime = _endTime;
			dtProcess = process;
			numPixelsH = _numPixelsH;
			numPixelsV = _numPixelsV;
			
			compositeLines = new Image[linesToPaint];
			lineNum = 0;
			depthTrace = new ProcessTimeline(lineNum, scopeMap, masterBuff, dtProcess, numPixelsH, endTime-begTime, minBegTime+begTime);
			
			depthTrace.readInData(getHeight());
			//depthTrace.readInData(inFile, f, getHeight());
			depthTrace.shiftTimeBy(minBegTime);
			
			
			TimelineThread[] threads;
			threads = new TimelineThread[Math.min(linesToPaint, Runtime.getRuntime().availableProcessors())];
			
			for (int threadNum = 0; threadNum < threads.length; threadNum++)
			{
				threads[threadNum] = new TimelineThread(this, false, canvas, numPixelsH, canvas.getScaleX(), Math.max(numPixelsV/(double)maxDepth, 1));
				threads[threadNum].start();
			}
			
			try
			{
				for (int threadNum = 0; threadNum < threads.length; threadNum++)
				{
					if (threads[threadNum].isAlive())
						threads[threadNum].join();
				}
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		for (int i = 0; i < linesToPaint; i++)
		{
			masterGC.drawImage(compositeLines[i], 0, 0, compositeLines[i].getBounds().width, 
					compositeLines[i].getBounds().height, 0,(int)Math.round(i*numPixelsV/(float)maxDepth), 
					compositeLines[i].getBounds().width, compositeLines[i].getBounds().height);
		}
	}
	

	
	/**********************************************************************
	 * Paints one "line" (the timeline for one processor) to its own image,
	 * which is later copied to a master image with the rest of the lines.
	 ********************************************************************/
	/**////////////////////////////////////////////////////////////////////////////////////
	//Because you will be painting between midpoints of samples,
	//you need to paint from the midpoint of the two nearest samples off screen
	//--which, when Math.max()'d with 0 will always return 0--
	//to the midpoint of the samples straddling the edge of the screen
	//--which, when Math.max()'d with 0 sometimes will return 0--
	//as well as from the midpoint of the samples straddling the edge of the screen
	//to the midpoint of the first two samples officially in the view
	//before even entering the loop that paints samples that exist fully in view
	////////////////////////////////////////////////////////////////////////////////////*/
	public void paintDepthLine(SpaceTimeSamplePainter spp, int depth, int height)
	{
		//System.out.println("I'm painting process "+process+" at depth "+depth);
		ProcessTimeline ptl = depthTrace;

		double pixelLength = (endTime - begTime)/(double)numPixelsH;
		//Reed - special cases were giving me a headache, so I threw them in a switch
		switch(ptl.size())
		{
			case 0:
			case 1:
				this.printDebug("Warning! incorrect timestamp size in depthPaint: " + ptl.size() );
				break;

			default:
			{
				BasePaintLine depthPaint = new BasePaintLine(colorTable, ptl, spp, begTime, depth, height, pixelLength)
				{
					@Override
					public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName)
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
			break;
		}
	}
	
	public void paintDetailLine(SpaceTimeSamplePainter spp, int process, int height, boolean changedBounds)
	{
		//System.out.println("I'm painting process "+process+" at depth "+depth);
		ProcessTimeline ptl = traces[process];
		if (ptl == null)
			return;
		
		if (changedBounds)
			ptl.shiftTimeBy(minBegTime);
		double pixelLength = (endTime - begTime)/(double)numPixelsH;
		//Reed - special cases were giving me a headache, so I threw them in a switch
		switch(ptl.size())
		{
			case 0:
			case 1:
				this.printDebug("Warning! incorrect timestamp size in detailPaint: " + ptl.size() );
				break;
			default:
			{
				// do the paint
				BasePaintLine detailPaint = new BasePaintLine(colorTable, ptl, spp, begTime, currentDepth, height, pixelLength)
				{
					@Override
					public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName)
					{
						spp.paintSample(currSampleMidpoint, succSampleMidpoint, height, functionName);			
						if (currDepth < depth)
						{
							spp.paintOverDepthText(currSampleMidpoint, Math.min(succSampleMidpoint, numPixelsH), currDepth, functionName);
						}
					}
				};
				detailPaint.paint();
			}
			break;
		}
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
		if(endProcess-begProcess > numPixelsV)
			return begProcess + (line*(endProcess-begProcess))/(numPixelsV);
		else
			return begProcess + line;
	}
	
	/***********************************************************************
	 * Gets the next available trace to be filled/painted
	 * @param changedBounds Whether or not the thread should get the data.
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextTrace(boolean changedBounds)
	{
		if(lineNum < Math.min(numPixelsV, endProcess-begProcess))
		{
			lineNum++;
			if(changedBounds)
				return new ProcessTimeline(lineNum-1, scopeMap, masterBuff, lineToPaint(lineNum-1), numPixelsH, endTime-begTime, minBegTime + begTime);
			else
				return traces[lineNum-1];
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
		if (lineNum < Math.min(numPixelsV, maxDepth))
		{
			if (lineNum==0)
			{
				lineNum++;
				return depthTrace;
			}
			ProcessTimeline toDonate = new ProcessTimeline(lineNum, scopeMap, masterBuff, dtProcess, numPixelsH, endTime-begTime, minBegTime+begTime);
			toDonate.times = depthTrace.times;
			toDonate.timeLine = depthTrace.timeLine;
			lineNum++;
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
		compositeLines[index] = line;
	}
	
	public int getBegProcess()
	{
		return this.begProcess;
	}
	
	
	public int getEndProcess()
	{
		return this.endProcess;
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
	
	private void printDebug(String str)
	{
		if (this.debug)
			System.err.println(str);
	}
}