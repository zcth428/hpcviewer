package edu.rice.cs.hpc.traceviewer.data.db;

import java.util.Collection;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

public class DetailDataPreparation extends DataPreparation {

	private ArrayList<DetailDataVisualization> list;
	
	/*****
	 * Constructor for preparing data to paint on the space-time canvas
	 * 
	 * @param _colorTable
	 * @param _ptl
	 * @param _begTime
	 * @param _depth
	 * @param _height
	 * @param _pixelLength
	 * @param _usingMidpoint
	 */
	public DetailDataPreparation(ColorTable _colorTable, ProcessTimeline _ptl,
			long _begTime, int _depth, int _height, double _pixelLength,
			boolean _usingMidpoint) 
	{
		super(_colorTable, _ptl, _begTime, _depth, _height, _pixelLength,
				_usingMidpoint);
		list = new ArrayList<DetailDataVisualization>(_ptl.size());
	}

	@Override
	public void finishLine(int currSampleMidpoint, int succSampleMidpoint,
			int currDepth, Color color, int sampleCount) {

		final DetailDataVisualization data = new DetailDataVisualization(currSampleMidpoint, 
				succSampleMidpoint, currDepth, color, sampleCount);
		list.add(data);
	}

	/*****
	 * retrieve the list of data to paint
	 * @return
	 */
	public Collection<DetailDataVisualization> getList() {
		
		return list;
	}
}
