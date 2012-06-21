package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public abstract class SpaceTimeDataController {
	
	PaintManager painter;
	static int[] MethodCounts = new int[15];

	public SpaceTimeDataController() {
		super();
	}

	public abstract void setCurrentlySelectedProccess(int ProcessNumber);

	public PaintManager getPainter()
	{
		MethodCounts[9]++;
		return painter;
	}

	public abstract ProcessTimeline getDepthTrace();

	public abstract ProcessTimeline getTrace(int process);

	public abstract void prepareViewportPainting(boolean changedBounds);

	abstract void prepareDepthViewportPainting();

	public abstract BaseDataFile getTraceData();

	public abstract int getHeight();

	public abstract long getWidth();

	public abstract void launchDetailViewThreads(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds);

	public abstract ProcessTimeline getProcess(int process);

//	public abstract void addNextTrace(ProcessTimeline nextPtl);//synchronized
//
	public abstract  ProcessTimeline getNextDepthTrace();//synchronized

	public abstract  ProcessTimeline getNextTrace(boolean changedBounds);//synchronized

	public abstract String getName();
	
	public abstract ImageTraceAttributes getAttributes();

}