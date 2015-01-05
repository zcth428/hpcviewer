package edu.rice.cs.hpc.traceviewer.depth;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintThread;
import edu.rice.cs.hpc.traceviewer.painter.BaseViewPaint;
import edu.rice.cs.hpc.traceviewer.painter.ISpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.ImagePosition;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;

/******************************************************
 * 
 * Painting class for depth view
 *
 ******************************************************/
public class DepthViewPaint extends BaseViewPaint {

	private GC masterGC;
	private float numPixels;
	
	public DepthViewPaint(IWorkbenchWindow window, ExecutorService threadExecutor,
			ISpaceTimeCanvas canvas) {
		
		super("Depth view", window, threadExecutor, canvas);
	}

	public void setData(GC masterGC, SpaceTimeDataController data, boolean changeBound) 
	{
		this.masterGC = masterGC;
		setData(data, changeBound);
		
	}
	
	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {
		numPixels = controller.getAttributes().numPixelsDepthV/(float)controller.getMaxDepth();
		return changedBounds;
	}


	@Override
	protected int getNumberOfLines() {
		return Math.min(controller.getAttributes().numPixelsDepthV, controller.getMaxDepth());
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, AtomicInteger timelineDone, IProgressMonitor monitor) {
		return new TimelineDepthThread( controller, yscale, queue, monitor,
										timelineDone, controller.isEnableMidpoint());
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

	@Override
	protected BasePaintThread getPaintThread(
			Queue<TimelineDataSet> queue, int linesToPaint, AtomicInteger timelineDone, 
			Device device, int width, IProgressMonitor monitor) {

		return new DepthPaintThread(controller, queue, linesToPaint, timelineDone, monitor, device, width);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas,
			ImagePosition img) {
		
		masterGC.drawImage(img.image, 0, 0, img.image.getBounds().width, 
				img.image.getBounds().height, 0, 
				Math.round(img.position*numPixels), 
				img.image.getBounds().width, img.image.getBounds().height);
		
		img.image.dispose();
	}
}
