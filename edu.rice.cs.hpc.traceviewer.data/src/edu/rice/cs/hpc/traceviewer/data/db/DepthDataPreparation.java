package edu.rice.cs.hpc.traceviewer.data.db;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
/*********************************************
 * 
 * Class to prepare data for depth view
 *
 *********************************************/
public class DepthDataPreparation extends DataPreparation {

	/*****
	 * list of data to be painted on the depth view
	 */
	final private ArrayList<BaseDataVisualization> list;
	
	/****
	 * Constructor to prepare data
	 * 
	 * @param _colorTable
	 * @param _ptl
	 * @param _begTime
	 * @param _depth
	 * @param _height
	 * @param _pixelLength
	 * @param _usingMidpoint
	 */
	public DepthDataPreparation(ColorTable _colorTable, ProcessTimeline _ptl,
			long _begTime, int _depth, int _height, double _pixelLength,
			boolean _usingMidpoint) {
		
		super(_colorTable, _ptl, _begTime, _depth, _height, _pixelLength,
				_usingMidpoint);
		list = new ArrayList<BaseDataVisualization>(_ptl.size());
	}

	@Override
	public void finishLine(int currSampleMidpoint, int succSampleMidpoint,
			int currDepth, Color color, int sampleCount) {

		BaseDataVisualization data = new BaseDataVisualization(currSampleMidpoint, 
				succSampleMidpoint, currDepth, color);
		list.add(data);
	}
	
	/***
	 * retrieve the list of data to be painted 
	 * 
	 * @return
	 */
	public Collection<BaseDataVisualization> getList() {
		
		return list;
	}

}
