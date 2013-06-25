package edu.rice.cs.hpc.traceviewer.spaceTimeData;


import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.util.Constants;

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


	/** Stores the current position of cursor */
	
	private Position currentPosition;
	/** Stores the current depth and data object that are being displayed.*/
	private int currentDepth;
	private int currentDataIdx;

	public PaintManager(ImageTraceAttributes _attributes,
			ColorTable _colorTable, int _maxDepth) {
		
		attributes = _attributes;
		
		colorTable = _colorTable;
		colorTable.setColorTable();
		
		maxDepth = _maxDepth;
		
		//defaut position
		this.currentDepth = 0;
		this.currentDataIdx = Constants.dataIdxNULL;
		currentPosition = new Position(0, 0);
	}



	public void setDepth(int _depth) {
		this.currentDepth = _depth;
	}

	public int getDepth() {
		return this.currentDepth;
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
		return attributes.begProcess;
	}

	public int getEndProcess() {
		return attributes.endProcess;
	}
	
	/*************************************************************************
	 * Returns the ColorTable holding all of the color to function name
	 * associations for this SpaceTimeData.
	 ************************************************************************/
	public ColorTable getColorTable() {
		return colorTable;
	}

	public long getViewTimeBegin() {
		return attributes.begTime;
	}

	public long getViewTimeEnd() {
		return attributes.endTime;
	}

	/*************************************************************************
	 * Returns the largest depth of all of the CallStackSamples of all of the
	 * ProcessTimelines.
	 ************************************************************************/
	public int getMaxDepth() {
		return maxDepth;
	}

	public Position getPosition() {
		return this.currentPosition;
	}
	
	public int getProcessRelativePosition(int numDisplayedProcess)
	{
		// general case
    	int estimatedProcess = (currentPosition.process - attributes.begProcess);
    	
    	// case for num displayed processes is less than the number of processes
    	estimatedProcess = (int) ((float)estimatedProcess* 
    			((float)numDisplayedProcess/(attributes.endProcess-attributes.begProcess)));
    	
    	// case for single process
    	estimatedProcess = Math.min(estimatedProcess, numDisplayedProcess-1);
    	
    	return estimatedProcess;
	}
	

	public void setPosition(Position position) {
		// The controller actually needs the position, and I don't know if the
		// PaintManger does. Should the PaintManager share the data with the
		// controller some how, or will this not be an issue once TraceEvents is
		// removed.
		this.currentPosition = position;
	}
	/** Sets the selected process to the middle if it is outside the bounds.*/
	public void fixPosition(){
		if (currentPosition.process >= attributes.endProcess || currentPosition.process <= attributes.begProcess) {
			// if the current process is beyond the range, make it in the middle
			currentPosition.process = (attributes.begProcess + attributes.endProcess)/2;
		}
	}
}
