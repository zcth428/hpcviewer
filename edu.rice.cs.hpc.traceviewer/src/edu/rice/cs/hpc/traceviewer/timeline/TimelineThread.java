package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/***********************************************************
 * A thread that reads in the data for one line, 
 * draws that line to its own image and adds the data
 * to the SpaceTimeData object that created them, and then
 * gets the next line that it needs to do this for if there
 * are any left (synchronized methods ftw!).
 * @author Michael Franco
 **********************************************************/
public class TimelineThread extends Thread
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
	
	final private AtomicInteger lineNum;
	
	final private ImageTraceAttributes attrib;

	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeDataController _stData, ProcessTimelineService traceService,
			AtomicInteger lineNum, boolean _changedBounds, Canvas _canvas, 
			Image[] compositeOrigLines, Image[] compositeFinalLines, int _width, 
			double _scaleX, double _scaleY, TimelineProgressMonitor _monitor)
	{
		super();
		stData = _stData;
		changedBounds = _changedBounds;
		canvas = _canvas;
		width = _width;
		scaleX = _scaleX;
		scaleY = _scaleY;
		
		monitor = _monitor;
		this.lineNum = lineNum;
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
	public void run()
	{
		ProcessTimeline nextTrace = stData.getNextTrace(changedBounds);
		
		boolean usingMidpoint = stData.isEnableMidpoint();
		while(nextTrace != null)
		{
			if(changedBounds && nextTrace.data.isEmpty())
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
		}
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
		
		double pixelLength = (attrib.endTime - attrib.begTime)/(double)attrib.numPixelsH;
		
		// do the paint
		BasePaintLine detailPaint = new BasePaintLine(stData.getColorTable(), ptl, spp, 
				attrib.begTime, stData.getPainter().getDepth(), height, pixelLength, usingMidpoint)
		{
			//@Override
			public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName, int sampleCount)
			{
				DetailSpaceTimePainter dstp = (DetailSpaceTimePainter) spp;
				dstp.paintSample(currSampleMidpoint, succSampleMidpoint, height, functionName);
				
				final boolean isOverDepth = (currDepth < depth);
				// write texts (depth and number of samples) if needed
				dstp.paintOverDepthText(currSampleMidpoint, Math.min(succSampleMidpoint, attrib.numPixelsH), 
						currDepth, functionName, isOverDepth, sampleCount);
			}
		};
		detailPaint.paint();
	}

	
	/**Returns the index of the file to which the line-th line corresponds.*/
	public int lineToPaint(int line)
	{
		int numTimelinesToPaint = attrib.endProcess - attrib.begProcess;
		if(numTimelinesToPaint > attrib.numPixelsV)
			return attrib.begProcess + (line * numTimelinesToPaint)/(attrib.numPixelsV);
		else
			return attrib.begProcess + line;
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