package edu.rice.cs.hpc.traceviewer.depth;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rice.cs.hpc.traceviewer.data.db.DataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;


/*************************************************
 * 
 * Timeline thread for depth view
 *
 *************************************************/
public class TimelineDepthThread 
	extends BaseTimelineThread
{

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
			AtomicInteger timelineDone, boolean usingMidpoint)
	{
		super(data, scaleY, queue, timelineDone, usingMidpoint);
	}


	@Override
	protected ProcessTimeline getNextTrace() {
		return stData.getNextDepthTrace();
	}

	@Override
	protected boolean init(ProcessTimeline trace) {

		return true;
	}

	@Override
	protected void finalize() {
	}

	@Override
	protected DataPreparation getData(ColorTable colorTable,
			ProcessTimeline timeline, long timeBegin, int linenum, int height,
			double pixelLength, boolean midPoint) {

		return new DepthDataPreparation(stData.getColorTable(), 
				timeline, timeBegin,
				linenum, height, pixelLength, midPoint);
	}	
}
