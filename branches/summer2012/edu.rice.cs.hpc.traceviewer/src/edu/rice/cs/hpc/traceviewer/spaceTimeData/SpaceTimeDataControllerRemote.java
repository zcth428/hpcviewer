package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController{

	@Override
	public void setCurrentlySelectedProccess(int ProcessNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PaintManager getPainter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessTimeline getDepthTrace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessTimeline getTrace(int process) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareViewportPainting(boolean changedBounds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void prepareDepthViewportPainting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BaseDataFile getTraceData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void launchDetailViewThreads(SpaceTimeCanvas canvas,
			int linesToPaint, double xscale, double yscale,
			boolean changedBounds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProcessTimeline getProcess(int process) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessTimeline getNextDepthTrace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessTimeline getNextTrace(boolean changedBounds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageTraceAttributes getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
