package edu.rice.cs.hpc.traceviewer.timeline;


import java.util.concurrent.Callable;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.data.db.BaseDataVisualization;
import edu.rice.cs.hpc.traceviewer.data.db.DetailDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.DetailDataVisualization;
import edu.rice.cs.hpc.traceviewer.data.db.VisualizationDataSet;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
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
	
	/**The canvas on which to paint.*/
	private Canvas canvas;
	
	/**The width that the images that this thread draws should be.*/
	private int width;
	
	/**The scale in the x-direction of pixels to time (for the drawing of the images).*/
	private double scaleX;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;

	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;
	
	final private TimelineProgressMonitor monitor;
	
	final private IWorkbenchWindow window;
	
	final private ProcessTimelineService traceService;
	final private Image[] compositeFinalLines;
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	final private Image[] compositeOrigLines;
	
	final private ImageTraceAttributes attrib;

	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeDataController _stData, ProcessTimelineService traceService,
			boolean _changedBounds, Canvas _canvas, Image[] compositeOrigLines, 
			Image[] compositeFinalLines, int _width, double _scaleX, 
			double _scaleY, TimelineProgressMonitor _monitor)
	{
		super();
		stData = _stData;
		changedBounds = _changedBounds;
		canvas = _canvas;
		width = _width;
		scaleX = _scaleX;
		scaleY = _scaleY;
		
		monitor = _monitor;
		this.window = window;
		this.traceService = traceService;
		this.compositeOrigLines = compositeOrigLines;
		this.compositeFinalLines = compositeFinalLines;
		
		attrib = stData.getAttributes();
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
		boolean usingMidpoint = stData.isEnableMidpoint();
		
		while(nextTrace != null)
		{
			//nextTrace.data is not empty if the data is from the server
			if(changedBounds && nextTrace.getData().isEmpty())
			{
				nextTrace.readInData();
				addNextTrace(nextTrace);
			}
			
			int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));
			if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;
			
			Image lineFinal = new Image(canvas.getDisplay(), width, imageHeight);
			Image lineOriginal = new Image(canvas.getDisplay(), width, imageHeight);
			GC gcFinal = new GC(lineFinal);
			GC gcOriginal = new GC(lineOriginal);
			
			SpaceTimeSamplePainter spp = new DetailSpaceTimePainter( window, gcOriginal, gcFinal, stData.getColorTable(), 
					scaleX, scaleY );
			paintDetailLine(spp, nextTrace.line(), imageHeight, changedBounds, usingMidpoint);
			
			gcFinal.dispose();
			gcOriginal.dispose();
			
			addNextImage(lineOriginal, lineFinal, nextTrace.line());
			
			monitor.announceProgress();
			
			nextTrace = stData.getNextTrace(changedBounds);
			numTracesHandled++;
		}
		return Integer.valueOf(numTracesHandled);
	}
	
	/*************************************************************************
	 * paint a space time detail line 
	 *  
	 * @param spp
	 * @param process
	 * @param height
	 * @param changedBounds
	 *************************************************************************/
	public void paintDetailLine(SpaceTimeSamplePainter spp, int process, int height, boolean changedBounds, boolean usingMidpoint)
	{
		ProcessTimeline ptl = traceService.getProcessTimeline(process);
		if (ptl == null || ptl.size()<2 )
			return;
		
		if (changedBounds)
			ptl.shiftTimeBy(stData.getMinBegTime());
		
		double pixelLength = (attrib.getTimeInterval())/(double)attrib.numPixelsH;
		
		// ---------------------------------
		// do the paint
		// ---------------------------------

		DetailDataPreparation detailPaint = new DetailDataPreparation(stData.getColorTable(), ptl, 
				attrib.getTimeBegin(), stData.getPainter().getDepth(), height, pixelLength, usingMidpoint);
		
		// collect data from the database
		detailPaint.collect();
		
		// get the list of data
		VisualizationDataSet dataset =  detailPaint.getList();
		
		for(BaseDataVisualization data : dataset.getList() ) {
			DetailSpaceTimePainter dstp = (DetailSpaceTimePainter) spp;
			dstp.paintSample(data.x_start, data.x_end, dataset.getHeight(), data.color);
			
			final boolean isOverDepth = (data.depth < stData.getAttributes().getDepth());
			dstp.paintOverDepthText(data.x_start, data.x_end, data.depth, data.color, isOverDepth, 
					((DetailDataVisualization)data).sample_counts);
		}
	}

	
	/**Returns the index of the file to which the line-th line corresponds.*/
	public int lineToPaint(int line)
	{
		int numTimelinesToPaint = attrib.getProcessInterval();
		if(numTimelinesToPaint > attrib.numPixelsV)
			return attrib.getProcessBegin() + (line * numTimelinesToPaint)/(attrib.numPixelsV);
		else
			return attrib.getProcessBegin() + line;
	}


	
	/**Adds a filled ProcessTimeline to traces - used by TimelineThreads.*/
	synchronized public void addNextTrace(ProcessTimeline nextPtl)
	{
		traceService.setProcessTimeline(nextPtl.line(), nextPtl);
	}

	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	synchronized public void addNextImage(Image imgOriginal, Image imgFinal, int index)
	{
		compositeOrigLines[index] = imgOriginal;
		compositeFinalLines[index] = imgFinal;
	}

}