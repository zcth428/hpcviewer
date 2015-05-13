package edu.rice.cs.hpc.traceviewer.data.db;

public interface ITraceDataCollector 
{
	public boolean isEmpty();
	public int findClosestSample(long time, boolean usingMidpoint);
	public void readInData(int rank, long timeStart, long timeRange, double pixelLength);
	public long getTime(int sample);
	public int getCpid(int sample);
	public int size();
	public void shiftTimeBy(long lowestStartingTime);
	public void duplicate(ITraceDataCollector traceData);
}
