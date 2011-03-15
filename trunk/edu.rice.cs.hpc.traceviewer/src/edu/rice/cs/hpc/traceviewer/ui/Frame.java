package edu.rice.cs.hpc.traceviewer.ui;

import java.io.Serializable;

import edu.rice.cs.hpc.traceviewer.painter.Position;

public class Frame implements Serializable
{
	
	private static final long serialVersionUID = 1L;

	/**The first and last process being viewed now*/
    public long begTime, endTime;
    
    /**The first and last time being viewed now*/
    public double begProcess, endProcess;
	
	public Position position;
	
	/**The depth of the frame saved*/
	public int depth;
	
	public Frame(long _begTime, long _endTime, double _begProcess, double _endProcess,
			int _depth, long _selectedTime, int _selectedProcess)
	{
		begTime = _begTime;
		endTime = _endTime;
		begProcess = _begProcess;
		endProcess = _endProcess;
		depth = _depth;
		
		position = new Position(_selectedTime, _selectedProcess);
	}
	
	public Frame(long _begTime, long _endTime, long _selectedTime, int _selectedDepth)
	{
		begTime = _begTime;
		endTime = _endTime;
		begProcess = -1;
		endProcess = -1;
		depth = -1;
		position = new Position(_selectedTime, _selectedDepth);
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
}
