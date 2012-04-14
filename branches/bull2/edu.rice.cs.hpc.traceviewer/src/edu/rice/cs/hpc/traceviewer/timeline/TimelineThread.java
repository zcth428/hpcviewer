package edu.rice.cs.hpc.traceviewer.timeline;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.painter.DetailSpaceTimePainter;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

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
	private SpaceTimeData stData;
	
	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	/**Stores whether a SpaceTimeDetailCanvas or a DepthTimeCanvas is being painted*/
	private boolean detailPaint;
	
	/**The canvas on which to paint.*/
	private Canvas canvas;
	
	/**The width that the images that this thread draws should be.*/
	private int width;
	
	/**The scale in the x-direction of pixels to time (for the drawing of the images).*/
	private double scaleX;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private double scaleY;

	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	private final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;
	
	final private TimelineProgressMonitor monitor;
	
	final private IWorkbenchWindow window;
	
	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread(IWorkbenchWindow window, SpaceTimeData _stData, 
			boolean _changedBounds, Canvas _canvas, int _width, 
			double _scaleX, double _scaleY, TimelineProgressMonitor _monitor)
	{
		super();
		stData = _stData;
		changedBounds = _changedBounds;
		canvas = _canvas;
		width = _width;
		scaleX = _scaleX;
		scaleY = _scaleY;
		detailPaint = canvas instanceof SpaceTimeDetailCanvas;
		
		monitor = _monitor;
		this.window = window;
	}
	
	/***************************************************************
	 * Reads in data for one line if the bounds have changed, 
	 * then paints the data to an image, then adds the data and the
	 * image to the stData that created it, and then gets the next
	 * line that it needs to do all this for if there are any left.
	 ***************************************************************/
	public void run()
	{
		if (detailPaint)
		{
			ProcessTimeline nextTrace = stData.getNextTrace(changedBounds);
			while(nextTrace != null)
			{
				if(changedBounds)
				{
					nextTrace.readInData(stData.getHeight());
					stData.addNextTrace(nextTrace);
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
				stData.paintDetailLine(spp, nextTrace.line(), imageHeight, changedBounds);
				
				gcFinal.dispose();
				gcOriginal.dispose();
				
				stData.addNextImage(lineOriginal, lineFinal, nextTrace.line());
				
				monitor.announceProgress();
				
				nextTrace = stData.getNextTrace(changedBounds);
			}
		}
		else
		{
			ProcessTimeline nextTrace = stData.getNextDepthTrace();
			while (nextTrace != null)
			{
				int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));
				if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
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
}