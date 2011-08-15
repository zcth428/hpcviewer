package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;
import java.util.Vector;

import edu.rice.cs.hpc.data.util.LargeByteBuffer;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;

/**A data structure that stores one line of timestamp-cpid data.*/
public class ProcessTimeline
{
	/**The list of cpids that correspond to this ProcessTimeline's timestamps.*/
	public Vector<Integer> timeLine;
	
	/**The timestamps needed to draw this ProcessTimeline.*/
	public Vector<Double> times;
	
	/**The mapping between the cpid's and the actual scopes.*/
	private HashMap<Integer, CallPath> scopeMap;
	
	/**The ByteBuffer that contains the entire traceFile*/
	private LargeByteBuffer masterBuff;
	
	/**The process belongs to this timeline*/
	private int processNumber;
	
	/**The minimum location in the file where data we are looking for can be found*/
	private long minimumLocation;
	
	/**The maximum location in the file where data we are looking for can be found*/
	private long maximumLocation;
	
	/**This ProcessTimeline's line number.*/
	private int lineNum;
	
	/**The initial time in view.*/
	private double startingTime;
	
	/**The range of time in view.*/
	private double timeRange;
	
	/**The amount of time that each pixel on the screen correlates to.*/
	private double pixelLength;
	
	/**The number of horizontal pixels in the viewer.*/
	private int numPixelH;
	
	/**The ID used in the fileName that correlates to the ID of the processor*/
	public int processID;
	
	/**The ID used in the fileName that correlates to the ID of the thread*/
	public int threadID;
	
	/**The size of one trace record in bytes (cpid (= 4 bytes) + timeStamp (= 8 bytes)).*/
	public final static byte SIZE_OF_TRACE_RECORD = 12;
	
	/**The new format for trace files has a 32-byte header (if 24, we were most recently testing old data).*/
	public final static byte SIZE_OF_HEADER = 32;
	
	/**The size of the header for the entire .megatrace file*/
	public final static byte SIZE_OF_MASTER_HEADER = 4;
	
	/**debugger output flag*/
	final boolean PTL_DEBUG = false;
	
	/*************************************************************************
	 *	Reads in the call-stack trace data from the binary traceFile in the form:
	 *	double time-stamp
	 *	int Call-Path ID
	 *	double time-stamp
	 *	int Call-Path ID
	 *	...
	 ************************************************************************/
	
	/**Creates a new ProcessTimeline with the given parameters.*/
	public ProcessTimeline(int _lineNum, HashMap<Integer, CallPath> _scopeMap, LargeByteBuffer _masterBuff, int _processNumber, int _numPixelH, double _timeRange, double _startingTime)
	{
		lineNum = _lineNum;
		scopeMap = _scopeMap;
		masterBuff = _masterBuff;
		processNumber = _processNumber;
		numPixelH = _numPixelH;
		timeRange = _timeRange;
		startingTime = _startingTime;
		
		times = new Vector<Double>(numPixelH);
		timeLine = new Vector<Integer>(numPixelH);
		
		pixelLength = timeRange/(double)numPixelH;
	}
	
	/**Fills the ProcessTimeline with data from the file.*/
	public void readInData(int totalProcesses)
	{
		//gets bounding locations where the data is stored for this entire process
		minimumLocation = masterBuff.getLong(SIZE_OF_MASTER_HEADER+processNumber*8);
		if (processNumber == totalProcesses-1)
			maximumLocation = masterBuff.size()-1;
		else
		{
			maximumLocation = masterBuff.getLong(SIZE_OF_MASTER_HEADER + (processNumber+1)*8)-SIZE_OF_TRACE_RECORD;
		}
		
		//reads in the ID values from the header
		processID = masterBuff.getInt(minimumLocation);
		threadID = masterBuff.getInt(minimumLocation+4);
		
		if (SpaceTimeDetailCanvas.datatype != SpaceTimeDetailCanvas.DataType.ProcessAndThreads)
		{
			if (processID!=0)
			{
				if (SpaceTimeDetailCanvas.datatype == SpaceTimeDetailCanvas.DataType.ThreadsOnly)
					SpaceTimeDetailCanvas.datatype = SpaceTimeDetailCanvas.DataType.ProcessAndThreads;
				else
					SpaceTimeDetailCanvas.datatype = SpaceTimeDetailCanvas.DataType.ProcessOnly;
			}
			if (threadID != 0)
			{
				if (SpaceTimeDetailCanvas.datatype == SpaceTimeDetailCanvas.DataType.ProcessOnly)
					SpaceTimeDetailCanvas.datatype = SpaceTimeDetailCanvas.DataType.ProcessAndThreads;
				else
					SpaceTimeDetailCanvas.datatype = SpaceTimeDetailCanvas.DataType.ThreadsOnly;
			}
		}
		
		//reads in the bounding locations where the data is located for the range of data to be viewed for this process
		long maxLoc = Math.min(findLocBeforeRAF(timeRange+startingTime)+SIZE_OF_TRACE_RECORD, masterBuff.size()-SIZE_OF_TRACE_RECORD);
		long minLoc = findLocBeforeRAF(startingTime);
		
		//fills in the rest of the data for this process timeline
		binaryFill(minLoc, maxLoc, 0, numPixelH, 0);
		
		//adds in the first and last data points
		double nextTime = masterBuff.getLong(maxLoc);
		int cpid = masterBuff.getInt(maxLoc+8);
		addSample(cpid, nextTime, times.size());
		
		nextTime = masterBuff.getLong(minLoc);
		if (!times.firstElement().equals(nextTime))
		{
			cpid = masterBuff.getInt(minLoc+8);
			addSample(cpid, nextTime, 0);
		}
		
		//TODO: no longer have hack that detects if there is any data at all or no pair data.  Should just error if this is true
		postProcess();
	}
	
	/**Adds a sample to times and timeLine.*/
	public void addSample(int cpid, double timestamp, int index)
	{
		if (index == times.size())
		{
			times.add(timestamp);
			timeLine.add(cpid);
		}
		else
		{
			times.add(index, timestamp);
			timeLine.add(index, cpid);
		}
		this.debug("add_sample " + index + ": " + cpid + "\ttime: " + timestamp);
	}
	
	/**Shifts all the times in the ProcessTimeline to the left by lowestStartingTime.*/
	public void shiftTimeBy(double lowestStartingTime)
	{
		for(int i = 0; i<times.size(); i++)
		{
			times.set(i, times.get(i)-lowestStartingTime);
		}
	}
	
	/**Gets the time that corresponds to the index sample in times.*/
	public double getTime(int sample)
	{
		if(sample<0)
			return 0;
		if(sample>=times.size())
			return width();
		return times.get(sample);
	}
	
	/**Gets the cpid that corresponds to the index sample in timeLine.*/
	public int getCpid(int sample)
	{
		return timeLine.elementAt(sample);
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
				System.err.println("ERROR: No sample found for cpid " + cpid + " in trace "+processID+"-"+threadID+"-"+sample+".");
				System.err.println("\tThere was most likely an error in the data collection; the display may be inaccurate.");
			}
			return cp;
		}
	}
	
	/**Returns the last timestamp in the ProcessTimeline.*/
	public double width()
	{
		return times.lastElement();
	}
	
	/**Returns the number of elements in this ProcessTimeline.*/
	public int size()
	{
		return timeLine.size();
	}
	
	/**Returns the first timestamp in this ProcessTimeline.*/
	public double getStartingTime()
	{
		return times.firstElement();
	}
	
	/**Returns this ProcessTimeline's line number.*/
	public int line()
	{
		return lineNum;
	}
	
	/*************************************************************************
	 *	Returns the number of elements in the trace data.
	 ************************************************************************/
	public long getElementCount()
	{
		return (masterBuff.size() - SIZE_OF_HEADER)/SIZE_OF_TRACE_RECORD;
	}
	
	/****************************************************************************
	 *	Returns the index in "times" of the closest sample to the specified time.
	 ***************************************************************************/
	public int getClosestSample(double time)
	{
		int low = 0;
        int high = times.size() - 1;
        int mid = ( low + high ) / 2;
        while( low != mid )
        {
            if(time > times.get(mid))
            	low = mid;
            else
            	high = mid;
            mid = ( low + high ) / 2;
        }
        
        if(Math.abs(time-times.get(high))<Math.abs(time-times.get(low)))
        	return high;
        else
        	return low;
	}
	
	/**Finds the sample to which 'time' most closely corresponds in the ProcessTimeline.
	 * @param time: the requested time
	 * @return the index of the sample if the time is within the range, -1 otherwise 
	 * */
	public int findMidpointBefore(double time)
	{
		int low = 0;
		int high = times.size() - 1;
		
		// do not search the sample if the time is out of range
		if (time < times.get(low) || time>times.get(high)) 
			return -1;
		
		int mid = ( low + high ) / 2;
		while( low != mid )
		{
			if (time > (times.get(mid)+times.get(mid+1))/2.0)
				low = mid;
			else
				high = mid;
			mid = ( low + high ) / 2;
		}
		if (time >= (times.get(low)+times.get(low+1))/2.0)
			return low+1;
		else
			return low;
	}
	
	/*************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid).
	 ************************************************************************/
	public long findLocBeforeRAF(double time)
	{
		long low = 0;
		long high = (maximumLocation-minimumLocation-SIZE_OF_HEADER)/SIZE_OF_TRACE_RECORD;
		long mid = (low + high)/2;
		
		long temp = 0;
		while (low < mid)
		{
			temp = masterBuff.getLong(mid*SIZE_OF_TRACE_RECORD+minimumLocation+SIZE_OF_HEADER);
			if (time > temp)
				low = mid;
			else
				high = mid;
			
			mid = (low+high)/2;
		}
		return low*SIZE_OF_TRACE_RECORD+minimumLocation+SIZE_OF_HEADER;
	}
	
	/*******************************************************************************************
	 * Recursive method that fills in times and timeLine with the correct data from the file.
	 * Takes in two pixel locations as endpoints and finds the timestamp that owns the pixel
	 * in between these two. It then recursively calls itself twice - once with the beginning 
	 * location and the newfound location as endpoints and once with the newfound location 
	 * and the end location as endpoints. Effectively updates times and timeLine by calculating 
	 * the index in which to insert the next data. This way, it keeps times and timeLine sorted.
	 * @author Reed Landrum and Michael Franco
	 * @param minLoc The beginning location in the file to bound the search.
	 * @param maxLoc The end location in the file to bound the search.
	 * @param startPixel The beginning pixel in the image that corresponds to minLoc.
	 * @param endPixel The end pixel in the image that corresponds to maxLoc.
	 * @param minIndex An index used for calculating the index in which the data is to be inserted.
	 * @return Returns the index that shows the size of the recursive subtree that has been read.
	 * Used for calculating the index in which the data is to be inserted.
	 ******************************************************************************************/
	public int binaryFill(long minLoc, long maxLoc, int startPixel, int endPixel, int minIndex)
	{
		int midPixel = (startPixel+endPixel)/2;
		if (midPixel == startPixel)
			return 0;
		
		long loc = findBoundedLocRAF(midPixel*pixelLength+startingTime, minLoc, maxLoc);
		double nextTime = masterBuff.getLong(loc);
		int cpid = masterBuff.getInt(loc+8);
		
		addSample(cpid, nextTime, minIndex);
		int addedLeft = binaryFill(minLoc, loc, startPixel, midPixel, minIndex);
		int addedRight = binaryFill(loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1);
		return (addedLeft+addedRight+1);
	}
	
	/*********************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid)
	 *	Precondition: the location of the trace data is between minLoc and maxLoc.
	 ********************************************************************************/
	public long findBoundedLocRAF(double time, long minLoc, long maxLoc)
	{
		if (minLoc == maxLoc)
			return minLoc;
		long low = (minLoc-minimumLocation-SIZE_OF_HEADER)/SIZE_OF_TRACE_RECORD;
		long high = (maxLoc-minimumLocation-SIZE_OF_HEADER)/SIZE_OF_TRACE_RECORD;
		long mid = (low+high)/2;
		
		double temp = 0;
		while(low < mid)
		{
			temp = masterBuff.getLong(mid*SIZE_OF_TRACE_RECORD+SIZE_OF_HEADER+minimumLocation);
			if (time > temp)
				low = mid;
			else
				high = mid;
			
			mid = (low+high)/2;
		}
		return low*SIZE_OF_TRACE_RECORD+minimumLocation+SIZE_OF_HEADER;
	}
	
	/*********************************************************************************************
	 * Removes unnecessary samples:
	 * i.e. if timeLine had three of the same cpid's in a row, the middle one would be superfluous,
	 * as we would know when painting that it should be the same color all the way through.
	 ********************************************************************************************/
	public void postProcess()
	{
		int len = times.size();
		for(int i = 0; i < len-2; i+=2)
		{
			while(i < len-1 && times.get(i).equals(times.get(i+1)))
			{
				times.remove(i+1);
				len--;
				timeLine.remove(i+1);
			}
			while(i < len-2 && timeLine.get(i).equals(timeLine.get(i+1)) && timeLine.get(i+1).equals(timeLine.get(i+2)))
			{
				times.remove(i+1);
				len--;
				timeLine.remove(i+1);
			}
		}
	}
	
	/****
	 * debugger output
	 * @param str
	 */
	private void debug(String str)
	{
		if (this.PTL_DEBUG)
		{
			System.out.println("PTL: " + str);
		}
	}
}