package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;

import edu.rice.cs.hpc.traceviewer.painter.BasePaintLine;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.PaintManager;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*************
 * 
 * Timeline thread for depth view
 *
 */
public class TimelineDepthThread extends Thread {

	final private SpaceTimeDataController stData;
	final private PaintManager painter;

	/**The canvas on which to paint.*/
	final private Canvas canvas;
	
	/**The scale in the x-direction of pixels to time (for the drawing of the images).*/
	private double scaleX;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;
	
	/**The width that the images that this thread draws should be.*/
	private int width;
	
	final private ProcessTimeline depthTrace;
	final private Image[] compositeFinalLines;
	final private AtomicInteger lineNum;

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
			ProcessTimeline  depthTrace, double scaleX, double scaleY, int width, AtomicInteger lineNum)
	{
		this.stData = data;
		this.painter = stData.getPainter();
		this.canvas = canvas;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.width  = width; 
		this.depthTrace = depthTrace;
		this.compositeFinalLines = compositeFinalLines;
		this.lineNum = lineNum;
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
		
		double pixelLength = (stData.attributes.endTime - stData.attributes.begTime)/(double)stData.attributes.numPixelsH;
		BasePaintLine depthPaint = new BasePaintLine(stData.getColorTable(), ptl, spp, stData.attributes.begTime, depth, height, pixelLength)
		{
			//@Override
			public void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName, int sampleCount)
			{
				if (currDepth >= depth)
				{
					spp.paintSample(currSampleMidpoint, succSampleMidpoint, height, functionName);
				}
			}
		};
		
		// do the paint
		depthPaint.paint();
	}

	/***********************************************************************
	 * Gets the next available trace to be filled/painted from the DepthTimeView
	 * @return The next trace.
	 **********************************************************************//*
	public ProcessTimeline getNextDepthTrace()
	{
		ProcessTimeline ptl = null;
		int line = lineNum.getAndIncrement();
		
		if (line < Math.min(stData.attributes.numPixelsDepthV, painter.getMaxDepth()))
		{
			if (line==0)
			{
				ptl = depthTrace;
			}else
			{
				ptl = new ProcessTimeline(line, stData.getScopeMap(),
						stData.getBaseData(), painter.getPosition().process, 
						stData.attributes.numPixelsH, stData.attributes.endTime-stData.attributes.begTime, 
						stData.getMinBegTime()+stData.attributes.begTime);
				ptl.copyDataFrom(depthTrace);
			}
			
			//System.out.println("Depth line: " + line);
		}
		return ptl;
	}*/
	
	/**Adds a painted Image to compositeLines - used by TimelineThreads.*/
	public synchronized void addNextImage(Image line, int index)
	{
		compositeFinalLines[index] = line;
		//System.out.println("set image line " + index);
	}

}
