package edu.rice.cs.hpc.traceviewer.timeline;


import java.util.Queue;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.data.db.DataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.DetailDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

public class TimelineThread 
	extends BaseTimelineThread
{
	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	final private TimelineProgressMonitor monitor;
	
	final private ProcessTimelineService traceService;
	
	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeDataController _stData, ProcessTimelineService traceService,
			boolean _changedBounds, double _scaleY, Queue<TimelineDataSet> queue, 
			TimelineProgressMonitor _monitor)
	{
		super(_stData, _scaleY, queue, _stData.isEnableMidpoint());
		changedBounds = _changedBounds;		
		monitor = _monitor;
		this.traceService = traceService;		
	}
	
	
	@Override
	protected ProcessTimeline getNextTrace() {
		return stData.getNextTrace(changedBounds);
	}

	
	@Override
	protected boolean init(ProcessTimeline trace) {
		//nextTrace.data is not empty if the data is from the server
		if(changedBounds)
		{
			if (trace.getData().isEmpty()) {
				
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
		monitor.announceProgress();		
	}

	@Override
	protected DataPreparation getData(ColorTable colorTable,
			ProcessTimeline timeline, long timeBegin, int linenum, int height,
			double pixelLength, boolean midPoint) {

		return new DetailDataPreparation(colorTable, timeline, 
				timeBegin, stData.getPainter().getDepth(), height, pixelLength, midPoint);
	}

}