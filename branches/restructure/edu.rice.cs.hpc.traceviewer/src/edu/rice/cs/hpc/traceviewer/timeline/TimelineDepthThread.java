package edu.rice.cs.hpc.traceviewer.timeline;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

/*************
 * 
 * Timeline thread for depth view
 *
 */
public class TimelineDepthThread extends Thread {

	final private SpaceTimeData stData;

	/**The canvas on which to paint.*/
	final private Canvas canvas;
	
	/**The scale in the x-direction of pixels to time (for the drawing of the images).*/
	private double scaleX;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;
	
	/**The width that the images that this thread draws should be.*/
	private int width;
	
	
	/*****
	 * Thread initialization
	 *  
	 * @param data : global data
	 * @param canvas : depth view canvas
	 * @param scaleX : The scale in the x-direction of pixels to time 
	 * @param scaleY : The scale in the y-direction of max depth
	 * @param width  : the width
	 */
	public TimelineDepthThread(SpaceTimeData data, Canvas canvas, 
			double scaleX, double scaleY, int width)
	{
		this.stData = data;
		this.canvas = canvas;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.width  = width; 
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
						int height, String function) {

					this.internalPaint(gc, startPixel, endPixel, height, function);
				}
			};
			
			stData.paintDepthLine(spp, nextTrace.line(), imageHeight);
			gc.dispose();
			
			stData.addNextImage(line, nextTrace.line());
			nextTrace = stData.getNextDepthTrace();
		}
	}
}
