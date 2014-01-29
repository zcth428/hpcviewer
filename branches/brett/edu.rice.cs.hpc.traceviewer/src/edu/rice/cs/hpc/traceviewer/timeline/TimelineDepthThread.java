package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.Queue;
import java.util.concurrent.Callable;

import edu.rice.cs.hpc.traceviewer.data.db.DepthDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;


/*************
 * 
 * Timeline thread for depth view
 *
 */
public class TimelineDepthThread implements Callable<Integer> {

	final private SpaceTimeDataController stData;

	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;
	
	private boolean usingMidpoint;
	
	final private Queue<TimelineDataSet> queue;

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
			double scaleY, Queue<TimelineDataSet> queue, boolean usingMidpoint)
	{
		this.stData = data;
		this.scaleY = scaleY;
		this.usingMidpoint = usingMidpoint;
		this.queue = queue;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public Integer call() 
	{
		ProcessTimeline nextTrace = stData.getNextDepthTrace();
		Integer numTraces = 0;
		while (nextTrace != null)
		{
			int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));
			if (scaleY > TimelineThread.MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;

			double pixelLength = (stData.getAttributes().getTimeInterval())/(double)stData.getPixelHorizontal();

			final DepthDataPreparation dataCollected = new DepthDataPreparation(stData.getColorTable(), 
					nextTrace, 
					stData.getAttributes().getTimeBegin(), nextTrace.line(), imageHeight, pixelLength, usingMidpoint);
			
			dataCollected.collect();
			
			// do the paint
			final TimelineDataSet dataset = dataCollected.getList();
			queue.add(dataset);

			nextTrace = stData.getNextDepthTrace();
			numTraces++;
		}
		return numTraces;
	}	
}
