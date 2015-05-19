package edu.rice.cs.hpc.traceviewer.main;


import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.data.db.DataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

public class TimelineThread 
	extends BaseTimelineThread
{
	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	final private ProcessTimelineService traceService;
	
	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeDataController _stData, ProcessTimelineService traceService,
			boolean _changedBounds, double _scaleY, Queue<TimelineDataSet> queue, 
			AtomicInteger numTimelines, IProgressMonitor monitor)
	{
		super(_stData, _scaleY, queue, numTimelines,_stData.isEnableMidpoint(), monitor);
		changedBounds = _changedBounds;		
		this.traceService = traceService;		
	}
	
	
	@Override
	protected ProcessTimeline getNextTrace() {
		return stData.getNextTrace(changedBounds);
	}

	
	@Override
	protected boolean init(ProcessTimeline trace) throws IOException {
		//nextTrace.data is not empty if the data is from the server
		if(changedBounds)
		{
			if (trace.isEmpty()) {
				
				trace.readInData();
				traceService.setProcessTimeline(trace.line(), trace);
			}
			trace.shiftTimeBy(stData.getMinBegTime());
		}
		boolean res = (trace.size()>=2);
		return res;
	}

	@Override
	protected void finalize() {
	}

	@Override
	protected DataPreparation getData(ColorTable colorTable,
			ProcessTimeline timeline, long timeBegin, int linenum, int height,
			double pixelLength, boolean midPoint) {

		return new DetailDataPreparation(colorTable, timeline, 
				timeBegin, stData.getAttributes().getDepth(), height, pixelLength, midPoint);
	}

}