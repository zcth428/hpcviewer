package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineDepthThread;

public class DepthViewPaint extends BaseViewPaint {

	private ProcessTimeline depthTrace;
	private final GC masterGC;
	final private AtomicInteger lineNum;

	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	public DepthViewPaint(final GC masterGC, SpaceTimeData _data,
			ImageTraceAttributes _attributes, boolean _changeBound,
			IStatusLineManager _statusMgr, IWorkbenchWindow window) {
		
		super(_data, _attributes, _changeBound, _statusMgr, window);
		this.masterGC = masterGC;
		this.lineNum = new AtomicInteger();
	}

	@Override
	protected boolean startPainting(int linesToPaint, boolean changedBounds) {
		depthTrace = new ProcessTimeline(0, data.getScopeMap(), data.getBaseData(), 
				data.getPosition().process, 
				attributes.numPixelsH, attributes.endTime-attributes.begTime,
				data.getMinBegTime()+attributes.begTime);
		
		depthTrace.readInData(data.getHeight());
		depthTrace.shiftTimeBy(data.getMinBegTime());
		compositeFinalLines = new Image[linesToPaint];

		return changedBounds;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale) {

		for (int i = 0; i < linesToPaint; i++)
		{
			masterGC.drawImage(compositeFinalLines[i], 0, 0, compositeFinalLines[i].getBounds().width, 
					compositeFinalLines[i].getBounds().height, 0,(int)Math.round(i*attributes.numPixelsDepthV/(float)data.getMaxDepth()), 
					compositeFinalLines[i].getBounds().width, compositeFinalLines[i].getBounds().height);
		}
		// disposing resources
		for (Image img: compositeFinalLines) {
			img.dispose();
		}
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsDepthV, data.getMaxDepth());
	}

	@Override
	protected Thread getTimelineThread(SpaceTimeCanvas canvas, double xscale, double yscale) {
		return new TimelineDepthThread(data, canvas, compositeFinalLines, depthTrace,
				xscale, yscale, attributes.numPixelsH, lineNum);
	}

}
