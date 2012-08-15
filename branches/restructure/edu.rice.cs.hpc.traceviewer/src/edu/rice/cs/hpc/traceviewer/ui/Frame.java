package edu.rice.cs.hpc.traceviewer.ui;

import java.io.Serializable;

import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;

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
	
	public Frame(ImageTraceAttributes attributes,
			int _depth, long _selectedTime, int _selectedProcess)
	{
		begTime = attributes.begTime;
		endTime = attributes.endTime;
		begProcess = attributes.begProcess;
		endProcess = attributes.endProcess;
		depth = _depth;
		
		position = new Position(_selectedTime, _selectedProcess);
	}
	
	public Frame(long timeBeg, long timeEnd, int ProcBeg, int ProcEnd)
	{
		begProcess 	= ProcBeg;
		endProcess  = ProcEnd;
		begTime	 	= timeBeg;
		endTime		= timeEnd;
	}
	
	public Frame(Frame frame)
	{
		this.begProcess = frame.begProcess;
		this.endProcess = frame.endProcess;
		this.begTime    = frame.begTime;
		this.endTime    = frame.endTime;
		this.depth	 	= frame.depth;
		this.position	= new Position(frame.position.time, frame.position.process);
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
}
