package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.HashMap;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.traceviewer.db.TraceDataByRank;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;

/**A data structure that stores one line of timestamp-cpid data.*/
public class ProcessTimeline
{
	
	/**The mapping between the cpid's and the actual scopes.*/
	private HashMap<Integer, CallPath> scopeMap;
	
	/**This ProcessTimeline's line number.*/
	private int lineNum;
	
	/**The initial time in view.*/
	private double startingTime;
	
	/**The range of time in view.*/
	private double timeRange;
	
	/**The amount of time that each pixel on the screen correlates to.*/
	private double pixelLength;
	
	/**The ID used in the fileName that correlates to the ID of the processor*/
	public int processID;
	
	/**The ID used in the fileName that correlates to the ID of the thread*/
	public int threadID;
	
	final TraceDataByRank data;
	
	/*************************************************************************
	 *	Reads in the call-stack trace data from the binary traceFile in the form:
	 *	double time-stamp
	 *	int Call-Path ID
	 *	double time-stamp
	 *	int Call-Path ID
	 *	...
	 ************************************************************************/
	
	/**Creates a new ProcessTimeline with the given parameters.*/
	public ProcessTimeline(int _lineNum, HashMap<Integer, CallPath> _scopeMap, BaseDataFile dataTrace, 
			int _processNumber, int _numPixelH, double _timeRange, double _startingTime, final int header_size)
	{
		lineNum = _lineNum;
		scopeMap = _scopeMap;

		timeRange = _timeRange;
		startingTime = _startingTime;
				
		pixelLength = timeRange/(double)_numPixelH;
		
		data = new TraceDataByRank(dataTrace, _processNumber, _numPixelH, header_size);
	}
	
	/**Fills the ProcessTimeline with data from the file.*/
	public void readInData(int totalProcesses)
	{
		data.getData(startingTime, timeRange, pixelLength);
	}
	
	/**Gets the time that corresponds to the index sample in times.*/
	public double getTime(int sample)
	{
		return data.getTime(sample);
	}
	
	/**Gets the cpid that corresponds to the index sample in timeLine.*/
	public int getCpid(int sample)
	{
		return data.getCpid(sample);
	}
	
	
	public void shiftTimeBy(double lowestStartingTime) 
	{
		data.shiftTimeBy(lowestStartingTime);
	}
	
	/**returns the call path corresponding to the sample and depth given*/
	public CallPath getCallPath(int sample, int depth)
	{
		if (sample == -1)
		{
			System.out.println("getCallPath() fail");
			return null;
		}
		else
		{
			int cpid = getCpid(sample);
			CallPath cp = scopeMap.get(cpid);
			if(cp != null)
				cp.updateCurrentDepth(depth);
			else
			{
				System.err.println("ERROR: No sample found for cpid " + cpid + " in trace (process ID, thread ID, sample): ("+processID+","+threadID+","+sample+")");
				System.err.println("\tThere was most likely an error in the data collection; the display may be inaccurate.");
			}
			return cp;
		}
	}
	
	public void copyData(ProcessTimeline another)
	{
		this.data.setListOfData(another.data.getListOfData());
	}
	
	/**Returns the number of elements in this ProcessTimeline.*/
	public int size()
	{
		return data.size();
	}

	
	/**Returns this ProcessTimeline's line number.*/
	public int line()
	{
		return lineNum;
	}
	
	
	/**Finds the sample to which 'time' most closely corresponds in the ProcessTimeline.
	 * @param time: the requested time
	 * @return the index of the sample if the time is within the range, -1 otherwise 
	 * */
	public int findMidpointBefore(double time)
	{
		return data.findMidpointBefore(time);
	}
	
}