package edu.rice.cs.hpc.traceviewer.ui;

import java.io.Serializable;

import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;


/***********************************************************************************
 * 
 * Frame class to store ROI (process and time range) and cursor position 
 *  	(time, process and depth)
 *
 ***********************************************************************************/
public class Frame implements Serializable
{
	
	private static final long serialVersionUID = 1L;

	/**The first and last process being viewed now*/
    public long begTime, endTime;
    
    /**The first and last time being viewed now*/
    public int begProcess, endProcess;
	
	public Position position;
	
	/**The depth of the frame saved*/
	public int depth;
	
	/****
	 * initialize frame with ROI and the cursor position (time, process and depth)
	 * if the position is not within the range, it will automatically adjust it
	 * into the middle of ROI
	 * 
	 * @param attributes
	 * @param _depth
	 * @param _selectedTime
	 * @param _selectedProcess
	 */
	public Frame(ImageTraceAttributes attributes,
			int _depth, long _selectedTime, int _selectedProcess)
	{
		this(attributes.begTime, attributes.endTime,
				attributes.begProcess, attributes.endProcess, 
				_depth, _selectedTime, _selectedProcess);
	}
	
	/****
	 * initialize frame with ROI and the cursor position (time, process and depth)
	 * if the position is not within the range, it will automatically adjust it
	 * into the middle of ROI
	 * 
	 * @param timeBeg
	 * @param timeEnd
	 * @param ProcBeg
	 * @param ProcEnd
	 * @param depth
	 * @param time
	 * @param process
	 */
	public Frame(long timeBeg, long timeEnd, int ProcBeg, int ProcEnd,
			int depth, long time, int process)
	{
		begProcess 	= ProcBeg;
		endProcess  = ProcEnd;
		begTime	 	= timeBeg;
		endTime		= timeEnd;
		this.depth  = depth;

		if (time < begTime || time > endTime) {
			time = (begTime + endTime) >> 1;
		}
		if (process < begProcess || process >= endProcess) {
			process = (begProcess + endProcess) >> 1;
		}
		position	= new Position(time, process);
	}
	
	/****
	 * initialize frame by copying with the specified frame
	 * if the position is not within the range, it will automatically adjust it
	 * into the middle of ROI
	 * 
	 * @param frame
	 */
	public Frame(Frame frame)
	{
		this.begProcess = frame.begProcess;
		this.endProcess = frame.endProcess;
		this.begTime    = frame.begTime;
		this.endTime    = frame.endTime;
		this.depth	 	= frame.depth;
		this.position	= new Position(frame.position.time, frame.position.process);
	}
	
	
	public Frame(Position position)
	{
		this.position = position;
	}
	
	public void set(int depth)
	{
		this.depth = depth;
	}
	
	public void set(Position p)
	{
		this.position = p;
	}
	
	public void set(long begTime, long endTime, int begProcess, int endProcess)
	{
		this.begProcess = begProcess;
		this.endProcess = endProcess;
		this.begTime	= begTime;
		this.endTime	= endTime;
	}
	
	public boolean equals(Frame other)
	{
		return (begProcess == other.begProcess
			&& begTime == other.begTime
			&& endProcess == other.endProcess
			&& endTime == other.endTime
			&& depth == other.depth
			&& position.isEqual(other.position) );
	}
	
	public boolean equalDimension(Frame other) {
		return (begProcess == other.begProcess
				&& begTime == other.begTime
				&& endProcess == other.endProcess
				&& endTime == other.endTime
				&& depth == other.depth);
	}
	
	@Override
	public String toString() {
		String time = "[ " + (begTime/1000)/1000.0 + "s, " + (endTime/1000)/1000.0+"s ]";
		String proc = " and [ " + begProcess + ", " + endProcess + " ]";
		return time + proc;
	}
}
