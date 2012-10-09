package edu.rice.cs.hpc.traceviewer.painter;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineThread;

public class DetailViewPaint extends BaseViewPaint {
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeOrigLines;

	private final GC masterGC;
	private final GC origGC;
	
	private AtomicInteger lineNum;
	
	public DetailViewPaint(final GC masterGC, final GC origGC, SpaceTimeData _data,
			ImageTraceAttributes _attributes, boolean _changeBound,
			IStatusLineManager _statusMgr, IWorkbenchWindow window) 
	{
		super(_data, _attributes, _changeBound, _statusMgr, window);
		this.masterGC = masterGC;
		this.origGC   = origGC;
		lineNum = new AtomicInteger(0);
	}

	@Override
	protected boolean startPainting(int linesToPaint, boolean changedBounds) {
		compositeOrigLines = new Image[linesToPaint];
		compositeFinalLines = new Image[linesToPaint];

		return true;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale) {
		for (int i = 0; i < linesToPaint; i++) {
			int yposition = (int) Math.round(i * yscale);
			if (compositeOrigLines[i] == null)
				System.out.println("i="+i);
			origGC.drawImage(compositeOrigLines[i], 0, yposition);
			masterGC.drawImage(compositeFinalLines[i], 0, yposition);
		}
		
		// disposing resources
		for (int i=0; i<linesToPaint; i++) {
			compositeOrigLines[i].dispose();
			compositeFinalLines[i].dispose();
		}
		// reset the line number to paint
		lineNum.set(0);
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
	}

	@Override
	protected Thread getTimelineThread(SpaceTimeCanvas canvas, double xscale,
			double yscale) {

		return new TimelineThread(this.window, data, data.getProcessTimeline(), lineNum, changedBounds, canvas,
				compositeOrigLines, compositeFinalLines, attributes.numPixelsH, 
				xscale, yscale, monitor);
	}
	
	public ProcessTimeline getProcessTimeline(int proc)
	{
		ProcessTimeline []traces = data.getProcessTimeline();
		
		assert(proc >=  0);
		
		// TODO hack to force proc within the range
		int p = Math.min(proc, traces.length-1);
		
		return traces[p];
	}
	
	public int getNumProcess()
	{
		return data.getProcessTimeline().length;
	}
}
