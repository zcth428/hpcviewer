package edu.rice.cs.hpc.traceviewer.timeline;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*************
 * 
 * Timeline thread for depth view
 *
 */
public class TimelineDepthThread extends Thread {

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

	
	public void run() 
	{
		ProcessTimeline nextTrace = stData.getNextDepthTrace();
		while (nextTrace != null)
		{
			int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));
			if (scaleY > TimelineThread.MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;
			
			Image line = new Image(canvas.getDisplay(), width, imageHeight);
			GC gc = new GC(line);
			SpaceTimeSamplePainter spp = new SpaceTimeSamplePainter(gc, stData.getColorTable(), scaleX, scaleY) {

				//@Override
				public void paintSample(int startPixel, int endPixel,
						int height, Color color) {

					this.internalPaint(gc, startPixel, endPixel, height, color);
				}
			};
			
			paintDepthLine(nextTrace, spp, nextTrace.line(), imageHeight);
			gc.dispose();
			
			addNextImage(line, nextTrace.line());
			nextTrace = stData.getNextDepthTrace();
		}
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
		
		BasePaintLine depthPaint = new BasePaintLine(stData.getColorTable(), ptl, spp, 
				stData.getAttributes().getTimeBegin(), depth, height, pixelLength, usingMidpoint)
		{
			//@Override
			public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, Color color, int sampleCount)
			{
				if (currDepth >= depth)
				{
					spp.paintSample(currSampleMidpoint, succSampleMidpoint, height, color);
				}
			}
		};
		
		// do the paint
		depthPaint.paint();
	}

	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	public synchronized void addNextImage(Image line, int index)
	{
		compositeFinalLines[index] = line;
	}

}
