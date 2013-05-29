package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineDepthThread;

public class DepthViewPaint extends BaseViewPaint {

	private ProcessTimeline depthTrace;
	private final GC masterGC;
	final private AtomicInteger lineNum;

	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	public DepthViewPaint(IWorkbenchWindow window, final GC masterGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound) {
		
		super(_data, _attributes, _changeBound,  window);
		this.masterGC = masterGC;
		this.lineNum = new AtomicInteger();
	}

	@Override
	protected boolean startPainting(int linesToPaint, boolean changedBounds) {
		/*depthTrace = new ProcessTimeline(0, controller.getScopeMap(), controller.getBaseData(), 
				painter.getPosition().process, 
				attributes.numPixelsH, attributes.endTime-attributes.begTime,
				controller.getMinBegTime()+attributes.begTime);
		*/
		depthTrace = controller.getTrace(controller.computeScaledProcess());
	
		//depthTrace.readInData(controller.getHeight());

		//depthTrace.readInData();
		depthTrace.shiftTimeBy(controller.getMinBegTime());
		compositeFinalLines = new Image[linesToPaint];

		return changedBounds;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale) {

		for (int i = 0; i < linesToPaint; i++)
		{
			masterGC.drawImage(compositeFinalLines[i], 0, 0, compositeFinalLines[i].getBounds().width, 
					compositeFinalLines[i].getBounds().height, 0,(int)Math.round(i*attributes.numPixelsDepthV/(float)painter.getMaxDepth()), 
					compositeFinalLines[i].getBounds().width, compositeFinalLines[i].getBounds().height);
		}
		// disposing resources
		for (Image img: compositeFinalLines) {
			img.dispose();
		}
		//FIXME: Ugly solution to the counter not being reset...
		controller.resetDepthCounter();
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsDepthV, painter.getMaxDepth());
	}

	@Override
	protected Thread getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale) {
		return new TimelineDepthThread(controller, canvas, compositeFinalLines, depthTrace,
				xscale, yscale, attributes.numPixelsH, lineNum);
	}

}
