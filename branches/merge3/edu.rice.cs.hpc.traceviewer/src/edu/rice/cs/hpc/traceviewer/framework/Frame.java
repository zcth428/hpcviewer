package edu.rice.cs.hpc.traceviewer.framework;

import java.io.Serializable;

public class Frame implements Serializable
{
	
	private static final long serialVersionUID = 1L;

	/**The first and last process being viewed now*/
    public long begTime, endTime;
    
    /**The first and last time being viewed now*/
    public double begProcess, endProcess;
	
	/**The selected time that is open in the csViewer*/
    public long selectedTime;
    
    /**The selected process that is open in the csViewer*/
    public int selectedProcess;
	
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
		selectedTime = _selectedTime;
		selectedProcess = _selectedProcess;
	}
	
	public Frame(long _begTime, long _endTime, long _selectedTime, int _selectedDepth)
	{
		begTime = _begTime;
		endTime = _endTime;
		begProcess = -1;
		endProcess = -1;
		depth = -1;
		selectedTime = _selectedTime;
		selectedProcess = _selectedDepth;
	}
	
	public boolean equals(Frame other)
	{
		return (begProcess == other.begProcess
			&& begTime == other.begTime
			&& endProcess == other.endProcess
			&& endTime == other.endTime
			&& depth == other.depth
			&& selectedTime == other.selectedTime
			&& selectedProcess == other.selectedProcess);
	}
}
