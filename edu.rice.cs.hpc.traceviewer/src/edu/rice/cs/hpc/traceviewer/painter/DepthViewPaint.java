package edu.rice.cs.hpc.traceviewer.painter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.DepthPaintThread;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineDepthThread;

/******************************************************
 * 
 * Painting class for depth view
 *
 ******************************************************/
public class DepthViewPaint extends BaseViewPaint {

	private final GC masterGC;
	
	public DepthViewPaint(IWorkbenchWindow window, final GC masterGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound, ExecutorService threadExecutor) {
		
		super(_data, _attributes, _changeBound,  window, threadExecutor);
		this.masterGC = masterGC;
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {
		return changedBounds;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale, 
			List<Future<List<ImagePosition>>> listOfImages) {

		final float numPixels = attributes.numPixelsDepthV/(float)painter.getMaxDepth();
		
		for (Future<List<ImagePosition>> listOfLines : listOfImages ) {
			try {
				final List<ImagePosition> imageLine = listOfLines.get();
				
				for (ImagePosition img : imageLine) {
					masterGC.drawImage(img.image, 0, 0, img.image.getBounds().width, 
							img.image.getBounds().height, 0, 
							Math.round(img.position*numPixels), 
							img.image.getBounds().width, img.image.getBounds().height);
					
					img.image.dispose();
				}
			} catch (InterruptedException e) {

				e.printStackTrace();
			} catch (ExecutionException e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsDepthV, painter.getMaxDepth());
	}

	@Override
	protected Callable<Integer> getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, AtomicInteger counter) {
		return new TimelineDepthThread( controller, yscale, queue, counter, controller.isEnableMidpoint());
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

	@Override
	protected Callable<List<ImagePosition>> getPaintThread(
			Queue<TimelineDataSet> queue, AtomicInteger counter, Device device, int width) {

		return new DepthPaintThread(controller, queue, counter, device, width);
	}
}
