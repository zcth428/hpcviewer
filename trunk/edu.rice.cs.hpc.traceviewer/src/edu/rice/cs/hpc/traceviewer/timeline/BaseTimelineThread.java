package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpc.traceviewer.data.db.DataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*****************************************************************************
 * 
 * Abstract class for handling data collection
 * The class is based on Java Callable, and returns the number of
 * traces the thread has been processed
 *
 * @author Michael Franco 
 * 	modified by Laksono and Philip
 *****************************************************************************/
public abstract class BaseTimelineThread implements Callable<Integer> {

	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;

	/**The SpaceTimeData that this thread gets its files from and adds it data and images to.*/
	final protected SpaceTimeDataController stData;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	final private double scaleY;	
	final protected boolean usingMidpoint;
	final private Queue<TimelineDataSet> queue;
	final private AtomicInteger numTimelines;
	final private IProgressMonitor monitor;
	
	public BaseTimelineThread(SpaceTimeDataController stData,
			double scaleY, Queue<TimelineDataSet> queue, IProgressMonitor monitor,
			AtomicInteger numTimelines, boolean usingMidpoint)
	{
		this.stData 		= stData;
		this.scaleY 		= scaleY;
		this.usingMidpoint 	= usingMidpoint;
		this.queue 			= queue;
		this.numTimelines 	= numTimelines;
		this.monitor  		= monitor;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Integer call() throws Exception {

		ProcessTimeline trace = getNextTrace();
		Integer numTraces = 0;
		final double pixelLength = (stData.getAttributes().getTimeInterval())/(double)stData.getPixelHorizontal();
		final long timeBegin = stData.getAttributes().getTimeBegin();

		while (trace != null)
		{
			// do not continue if a user cancels the operation
			if (monitor.isCanceled())
				throw new CancellationException();

			// ---------------------------------
			// begin collecting the data if needed
			// ---------------------------------
			if (init(trace))
			{				
				int h1 = (int) Math.round(scaleY*trace.line());
				int h2 = (int) Math.round(scaleY*(trace.line()+1)) ;			
				int imageHeight = h2 - h1;
				
				if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
					imageHeight--;
				else
					imageHeight++;
				
    			// ---------------------------------
    			// do the data preparation
    			// ---------------------------------

				final DataPreparation data = getData(stData.getColorTable(),
						trace, timeBegin, trace.line(),
						imageHeight, pixelLength, usingMidpoint);
				
				data.collect();
				
				final TimelineDataSet dataSet = data.getList();
				queue.add(dataSet);				
			}
			numTimelines.decrementAndGet();
			
			trace = getNextTrace();
			numTraces++;
			
			// ---------------------------------
			// finalize
			// ---------------------------------
			finalize();
		}
		return numTraces;
	}

	/****
	 * Retrieve the next available trace, null if no more trace available 
	 * 
	 * @return
	 ****/
	abstract protected ProcessTimeline getNextTrace();
	
	abstract protected boolean init(ProcessTimeline trace);
	
	abstract protected void finalize();
	
	abstract protected DataPreparation getData(ColorTable colorTable, ProcessTimeline timeline,
			long timeBegin, int linenum,
			int height, double pixelLength, boolean midPoint);
}
