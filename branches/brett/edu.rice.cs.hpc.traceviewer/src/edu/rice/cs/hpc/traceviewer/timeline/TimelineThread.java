package edu.rice.cs.hpc.traceviewer.timeline;


import java.util.concurrent.Callable;
import java.util.Queue;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.data.db.DetailDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

/***********************************************************
 * A thread that reads in the data for one line, 
 * draws that line to its own image and adds the data
 * to the SpaceTimeData object that created them, and then
 * gets the next line that it needs to do this for if there
 * are any left (synchronized methods ftw!).
 * @author Michael Franco
 **********************************************************/
public class TimelineThread implements Callable<Integer>
{
	/**The SpaceTimeData that this thread gets its files from and adds it data and images to.*/
	private SpaceTimeDataController stData;
	
	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;

	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;
	
	final private TimelineProgressMonitor monitor;
	
	final private ProcessTimelineService traceService;
	
	final private ImageTraceAttributes attrib;
	
	final private Queue<TimelineDataSet> queue;

	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeDataController _stData, ProcessTimelineService traceService,
			boolean _changedBounds, double _scaleY, Queue<TimelineDataSet> queue, TimelineProgressMonitor _monitor)
	{
		stData = _stData;
		changedBounds = _changedBounds;
		scaleY = _scaleY;
		
		monitor = _monitor;
		this.traceService = traceService;
		
		attrib = stData.getAttributes();
		this.queue = queue;
	}
	
	/***************************************************************
	 * Reads in data for one line if the bounds have changed, 
	 * then paints the data to an image, then adds the data and the
	 * image to the stData that created it, and then gets the next
	 * line that it needs to do all this for if there are any left.
	 ***************************************************************/
	public Integer call()
	{
		ProcessTimeline nextTrace = stData.getNextTrace(changedBounds);
		int numTracesHandled = 0;
		final boolean usingMidpoint = stData.isEnableMidpoint();		
		final double pixelLength = (attrib.getTimeInterval())/(double)attrib.numPixelsH;
		
		while(nextTrace != null)
		{
			//nextTrace.data is not empty if the data is from the server
			if(changedBounds && nextTrace.getData().isEmpty())
			{
				nextTrace.readInData();
				addNextTrace(nextTrace);
				nextTrace.shiftTimeBy(stData.getMinBegTime());
			}
			
			int h1 = (int) Math.round(scaleY*(nextTrace.line()+1));
			int h2 = (int) Math.round(scaleY*nextTrace.line());
			int imageHeight = h1 - h2;

			if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;
			
			// ---------------------------------
			// do the data preparation
			// ---------------------------------

			final DetailDataPreparation dataCollected = new DetailDataPreparation(stData.getColorTable(), nextTrace, 
					attrib.getTimeBegin(), stData.getPainter().getDepth(), imageHeight, pixelLength, usingMidpoint);
			
			// do collect data from the database
			dataCollected.collect();
			
			// ---------------------------------
			// get the list of data and put it in the queue to be painted
			// ---------------------------------
			final TimelineDataSet dataset =  dataCollected.getList();
			queue.add(dataset);

			// ---------------------------------
			// finalize
			// ---------------------------------
			monitor.announceProgress();
			
			nextTrace = stData.getNextTrace(changedBounds);
			numTracesHandled++;
		}
		return Integer.valueOf(numTracesHandled);
	}
	

	
	/**Adds a filled ProcessTimeline to traces - used by TimelineThreads.*/
	synchronized public void addNextTrace(ProcessTimeline nextPtl)
	{
		traceService.setProcessTimeline(nextPtl.line(), nextPtl);
	}

}