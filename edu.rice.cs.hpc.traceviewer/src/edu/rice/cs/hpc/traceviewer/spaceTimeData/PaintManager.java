package edu.rice.cs.hpc.traceviewer.spaceTimeData;


import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;

import edu.rice.cs.hpc.traceviewer.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;

/**
 * This contains the painting components from SpaceTimeData
 * 
 * @author Philip Taffet and original authors
 * 
 */
//Should this class be deleted and re-merged into SpaceTimeData???
public class PaintManager {


	private final ImageTraceAttributes attributes;
	/** The maximum depth of any single CallStackSample in any trace. */
	private int maxDepth;

	/**
	 * Stores the color to function name assignments for all of the functions in
	 * all of the processes.
	 */
	private ColorTable colorTable;

	private int currentDataIdx;

	public PaintManager(ImageTraceAttributes _attributes,
			ColorTable _colorTable, int _maxDepth) {
		
		attributes = _attributes;
		
		colorTable = _colorTable;
		colorTable.setColorTable();
		
		maxDepth = _maxDepth;
		
		this.currentDataIdx = Constants.dataIdxNULL;
	}



	public void setDepth(int _depth) {
		attributes.setDepth(_depth);
	}

	public int getDepth() {
		return attributes.getDepth();
	}
	/***
	 * set the current index data
	 * This is used by data centric view 
	 * @param dataIdx
	 */
	public void setData(int dataIdx)
	{
		this.currentDataIdx = dataIdx;
	}
	
	/**
	 * get the current index data
	 * @return
	 */
	public int getData()
	{
		return this.currentDataIdx;
	}
	

	// Redirect these calls as well
	public int getBegProcess() {
		return attributes.getProcessBegin();
	}

	public int getEndProcess() {
		return attributes.getProcessEnd();
	}
	
	/*************************************************************************
	 * Returns the ColorTable holding all of the color to function name
	 * associations for this SpaceTimeData.
	 ************************************************************************/
	public ColorTable getColorTable() {
		return colorTable;
	}

	public long getViewTimeBegin() {
		return attributes.getTimeBegin();
	}

	public long getViewTimeEnd() {
		return attributes.getTimeEnd();
	}

	/*************************************************************************
	 * Returns the largest depth of all of the CallStackSamples of all of the
	 * ProcessTimelines.
	 ************************************************************************/
	public int getMaxDepth() {
		return maxDepth;
	}

	public Position getPosition() {
		return attributes.getPosition();
	}
	
	public int getProcessRelativePosition(int numDisplayedProcess)
	{
		// general case
    	int estimatedProcess = (getPosition().process - attributes.getProcessBegin());
    	
    	// case for num displayed processes is less than the number of processes
    	estimatedProcess = (int) ((float)estimatedProcess* 
    			((float)numDisplayedProcess/(attributes.getProcessInterval())));
    	
    	// case for single process
    	estimatedProcess = Math.min(estimatedProcess, numDisplayedProcess-1);
    	
    	return estimatedProcess;
	}
	

	public void setPosition(Position position) {
		// The controller actually needs the position, and I don't know if the
		// PaintManger does. Should the PaintManager share the data with the
		// controller some how, or will this not be an issue once TraceEvents is
		// removed.
		attributes.setPosition(position);
	}
}
