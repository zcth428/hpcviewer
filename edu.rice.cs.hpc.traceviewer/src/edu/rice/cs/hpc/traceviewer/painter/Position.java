package edu.rice.cs.hpc.traceviewer.painter;

public class Position {
	public long time;
	public int process;
	
	public Position(long _time, int _process ) {
		this.time = _time;
		this.process = _process;
	}
	
	public boolean isEqual(Position p) {
		return (time == p.time && process == p.process);
	}
}
