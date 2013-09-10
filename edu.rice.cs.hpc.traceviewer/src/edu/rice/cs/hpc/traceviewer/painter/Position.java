package edu.rice.cs.hpc.traceviewer.painter;

import java.io.Serializable;

public class Position  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2287052521974687520L;
	
	public long time;
	public int process;
	
	public int sample;
	
	public Position(long _time, int _process ) {
		this.time = _time;
		this.process = _process;
	}
	
	public boolean isEqual(Position p) {
		return (time == p.time && process == p.process);
	}
	
	@Override
	public String toString() {
		return "("+time+","+process+")";
	}
}
