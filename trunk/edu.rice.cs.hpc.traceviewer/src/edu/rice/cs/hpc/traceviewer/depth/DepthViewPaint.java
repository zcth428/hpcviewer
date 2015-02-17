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
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;

/******************************************************
 * 
 * Painting class for depth view
 *
 ******************************************************/
public class DepthViewPaint extends BaseViewPaint {

	private final GC masterGC;
	private float numPixels;
	
	public DepthViewPaint(IWorkbenchWindow window, final GC masterGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound, ISpaceTimeCanvas canvas, 
			ExecutorService threadExecutor) {
		
		super("Depth view", _data, _attributes, _changeBound,  window, canvas, threadExecutor);
		this.masterGC = masterGC;
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {
		int process = attributes.getPosition().process;
		
		// we need to check if the data is ready.
		// data is ready iff 
		//  - a process has been selected for the depth view (within the range)
		//  - and the main view has finished generated the timelines
		
		if (process >= attributes.getProcessBegin() && process <= attributes.getProcessEnd()) {
			
			if ( controller.getDepthTrace() != null) {
				numPixels = attributes.numPixelsDepthV/(float)controller.getMaxDepth();
				return changedBounds;
			}
		}
		return false;
	}


	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsDepthV, controller.getMaxDepth());
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, AtomicInteger timelineDone, IProgressMonitor monitor) {
		return new TimelineDepthThread( controller, yscale, queue, timelineDone, 
				controller.isEnableMidpoint(), monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

	@Override
	protected BasePaintThread getPaintThread(
			Queue<TimelineDataSet> queue, int linesToPaint, AtomicInteger timelineDone, Device device, int width) {

		return new DepthPaintThread(controller, queue, linesToPaint, timelineDone, device, width);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas,
			ImagePosition img) {
		if (masterGC != null && !masterGC.isDisposed())
		{
			masterGC.drawImage(img.image, 0, 0, img.image.getBounds().width, 
					img.image.getBounds().height, 0, 
					Math.round(img.position*numPixels), 
					img.image.getBounds().width, img.image.getBounds().height);
			
			img.image.dispose();
		}
	}
}
