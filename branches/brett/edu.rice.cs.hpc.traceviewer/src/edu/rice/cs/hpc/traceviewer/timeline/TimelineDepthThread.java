package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.concurrent.Callable;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import edu.rice.cs.hpc.traceviewer.data.db.BaseDataVisualization;
import edu.rice.cs.hpc.traceviewer.data.db.DepthDataPreparation;
import edu.rice.cs.hpc.traceviewer.data.db.VisualizationDataSet;
import edu.rice.cs.hpc.traceviewer.painter.DepthSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;


/*************
 * 
 * Timeline thread for depth view
 *
 */
public class TimelineDepthThread implements Callable<Integer> {

	final private SpaceTimeDataController stData;

	/**The canvas on which to paint.*/
	final private Canvas canvas;
	
	/**The scale in the x-direction of pixels to time (for the drawing of the images).*/
	private double scaleX;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;
	
	/**The width that the images that this thread draws should be.*/
	private int width;
	
	private boolean usingMidpoint;
	
	final private Image[] compositeFinalLines;

	/*****
	 * Thread initialization
	 *  
	 * @param data : global data
	 * @param canvas : depth view canvas
	 * @param scaleX : The scale in the x-direction of pixels to time 
	 * @param scaleY : The scale in the y-direction of max depth
	 * @param width  : the width
	 */
	public TimelineDepthThread(SpaceTimeDataController data, Canvas canvas, Image[] compositeFinalLines,
			double scaleX, double scaleY, int width, boolean usingMidpoint)
	{
		this.stData = data;
		this.canvas = canvas;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.width  = width; 
		this.compositeFinalLines = compositeFinalLines;
		this.usingMidpoint = usingMidpoint;
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
			
			Image line = new Image(canvas.getDisplay(), width, imageHeight);
			GC gc = new GC(line);
			SpaceTimeSamplePainter spp = new DepthSpaceTimePainter(gc, stData.getColorTable(), scaleX, scaleY);
			
			paintDepthLine(nextTrace, spp, nextTrace.line(), imageHeight);
			gc.dispose();
			
			addNextImage(line, nextTrace.line());
			nextTrace = stData.getNextDepthTrace();
			numTraces++;
		}
		return numTraces;
	}
	
	
	/**********************************************************************
	 * Paints one "line" (the timeline for one processor) to its own image,
	 * which is later copied to a master image with the rest of the lines.
	 ********************************************************************/
	public void paintDepthLine(ProcessTimeline ptl, SpaceTimeSamplePainter spp, int depth, int height)
	{
		if (ptl.size() < 2)
			return;

		double pixelLength = (stData.getAttributes().getTimeInterval())/(double)stData.getPixelHorizontal();
		
		DepthDataPreparation depthPaint = new DepthDataPreparation(stData.getColorTable(), ptl, 
				stData.getAttributes().getTimeBegin(), depth, height, pixelLength, usingMidpoint);
		
		depthPaint.collect();
		
		// do the paint
		VisualizationDataSet dataset = depthPaint.getList();
		
		for(BaseDataVisualization data : dataset.getList()) {
			if (data.depth >= depth) {
				spp.paintSample(data.x_start, data.x_end, dataset.getHeight(), data.color);
			}
		}
	}

	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	public synchronized void addNextImage(Image line, int index)
	{
		compositeFinalLines[index] = line;
	}

}
