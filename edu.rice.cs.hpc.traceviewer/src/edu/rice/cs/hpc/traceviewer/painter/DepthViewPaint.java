package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineDepthThread;

public class DepthViewPaint extends BaseViewPaint {

	private final GC masterGC;


	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	public DepthViewPaint(IWorkbenchWindow window, final GC masterGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound, ExecutorService threadExecutor) {
		
		super(_data, _attributes, _changeBound,  window, threadExecutor);
		this.masterGC = masterGC;
	}

	@Override
	protected boolean startPainting(int linesToPaint, boolean changedBounds) {
		controller.getDepthTrace();
	
		compositeFinalLines = new Image[linesToPaint];

		return changedBounds;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale) {

		if (compositeFinalLines != null) {
			for (int i = 0; i < linesToPaint; i++)
			{
				if (compositeFinalLines[i] != null)
					masterGC.drawImage(compositeFinalLines[i], 0, 0, compositeFinalLines[i].getBounds().width, 
						compositeFinalLines[i].getBounds().height, 0, Math.round(i*attributes.numPixelsDepthV/(float)painter.getMaxDepth()), 
						compositeFinalLines[i].getBounds().width, compositeFinalLines[i].getBounds().height);
			}
			// disposing resources
			for (Image img: compositeFinalLines) {
				if (img != null)
					img.dispose();
			}
		}
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsDepthV, painter.getMaxDepth());
	}

	@Override
	protected Callable<Integer> getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale) {
		return new TimelineDepthThread(controller, canvas, compositeFinalLines, xscale,
				yscale, attributes.numPixelsH, controller.isEnableMidpoint());
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

}
