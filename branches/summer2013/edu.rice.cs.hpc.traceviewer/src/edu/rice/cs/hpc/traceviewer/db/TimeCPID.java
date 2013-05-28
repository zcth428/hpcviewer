package edu.rice.cs.hpc.traceviewer.db;

/***
 * struct object of time and CPID pair
 * 
 * @author laksonoadhianto
 * 
 */
public class TimeCPID {
	public double timestamp;
	public int cpid;

	public TimeCPID(double _timestamp, int _cpid) {
		this.timestamp = _timestamp;
		this.cpid = _cpid;
	}

	@Override
	public String toString() {
		return "TS: " + timestamp + "  CPID: "+ cpid;
	}
}
