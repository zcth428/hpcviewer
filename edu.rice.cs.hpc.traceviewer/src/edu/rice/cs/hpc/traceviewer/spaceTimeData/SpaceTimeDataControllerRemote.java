package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController{

	final RemoteDataRetriever dataRetriever;
	
	public SpaceTimeDataControllerRemote(RemoteDataRetriever _dataRetiever)
	{
		dataRetriever = _dataRetiever;
		//dbName, attributes
	}
	
	@Override
	public void setCurrentlySelectedProccess(int ProcessNumber) {
		// TODO Auto-generated method stub
		
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
	public String[] getTraceDataValuesX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillTraces(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds) {
		// TODO Auto-generated method stub
		
	}


	

}
