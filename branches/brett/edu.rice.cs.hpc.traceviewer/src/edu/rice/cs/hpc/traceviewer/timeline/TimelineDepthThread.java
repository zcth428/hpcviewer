package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rice.cs.hpc.traceviewer.data.db.DepthDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;


/*************************************************
 * 
 * Timeline thread for depth view
 *
 *************************************************/
public class TimelineDepthThread implements Callable<Integer> {

	final private SpaceTimeDataController stData;

	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;
	
	private boolean usingMidpoint;
	
	final private Queue<TimelineDataSet> queue;
	final private AtomicInteger counter;
	/*****
	 * Thread initialization
	 *  
	 * @param data : global data
	 * @param canvas : depth view canvas
	 * @param scaleX : The scale in the x-direction of pixels to time 
	 * @param scaleY : The scale in the y-direction of max depth
	 * @param width  : the width
	 */
	public TimelineDepthThread(SpaceTimeDataController data, 
			double scaleY, Queue<TimelineDataSet> queue, 
			AtomicInteger counter,
			boolean usingMidpoint)
	{
		this.stData = data;
		this.scaleY = scaleY;
		this.usingMidpoint = usingMidpoint;
		this.queue = queue;
		this.counter = counter;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public Integer call() 
	{
		ProcessTimeline nextTrace = stData.getNextDepthTrace();
		counter.decrementAndGet();
		
		Integer numTraces = 0;
		final double pixelLength = (stData.getAttributes().getTimeInterval())/(double)stData.getPixelHorizontal();
		final long timeBegin = stData.getAttributes().getTimeBegin();
		
		while (nextTrace != null && counter.get()>0)
		{
			int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));
			if (scaleY > TimelineThread.MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;

			final DepthDataPreparation dataCollected = new DepthDataPreparation(stData.getColorTable(), 
					nextTrace, timeBegin,
					nextTrace.line(), imageHeight, pixelLength, usingMidpoint);
			
			dataCollected.collect();
			
			// add into the queue
			final TimelineDataSet dataset = dataCollected.getList();
			queue.add(dataset);

			nextTrace = stData.getNextDepthTrace();
			numTraces++;
			int c = counter.decrementAndGet();
			if (nextTrace == null && c>0) {
				System.err.println("Error: counter is not 0 : " + c);
			}
		}
		return numTraces;
	}	
}
