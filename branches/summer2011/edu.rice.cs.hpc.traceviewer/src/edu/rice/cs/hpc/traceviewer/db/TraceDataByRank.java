package edu.rice.cs.hpc.traceviewer.db;

import java.util.ArrayList;
import java.util.Vector;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;

public class TraceDataByRank {

	/**The size of one trace record in bytes (cpid (= 4 bytes) + timeStamp (= 8 bytes)).*/
	public final static byte SIZE_OF_TRACE_RECORD = 12;
	
	/**The new format for trace files has a 24-byte header.*/
	public final static byte SIZE_OF_HEADER = 24;

	
	final private BaseDataFile data;
	final private int rank;
	final private long minloc;
	final private long maxloc;
	final private int numPixelH;
	
	private Vector<TimeCPID> listcpid;
	
	/***
	 * Create a new instance of trace data for a given rank of process or thread 
	 * 
	 * @param _data
	 * @param _rank
	 * @param _numPixelH
	 */
	public TraceDataByRank(BaseDataFile _data, int _rank, int _numPixelH)
	{
		data = _data;
		rank = _rank;
		
		final long offsets[] = data.getOffsets();
		minloc = offsets[rank] + SIZE_OF_HEADER;
		maxloc = ( (rank+1<data.getNumberOfFiles())? offsets[rank+1] : data.getMasterBuffer().size()-1 )
				- SIZE_OF_TRACE_RECORD;
		
		numPixelH = _numPixelH;
		
		listcpid = new Vector<TimeCPID>(numPixelH);
	}
	
	public void getData(double timeStart, double timeRange, double pixelLength)
	{
		// get the start location
		final long startLoc = this.findBoundedLocRAF(timeStart, minloc, maxloc);
		
		// get the end location
		final double endTime = timeStart + timeRange;
		final long endLoc = Math.min(this.findBoundedLocRAF(endTime, minloc, maxloc)+SIZE_OF_TRACE_RECORD, maxloc );
		
		//fills in the rest of the data for this process timeline
		this.binaryFill(startLoc, endLoc, 0, numPixelH, 0, pixelLength, timeStart);
		
		// get the last data
		final TimeCPID dataLast = this.getData(endLoc);
		this.addSample(listcpid.size(), dataLast);
		
		// get the first data if necessary
		final TimeCPID dataFirst = this.getData(startLoc);
		if (listcpid.get(0).timestamp != dataFirst.timestamp) {
			this.addSample(0, dataFirst);
		}
		postProcess();

	}
	
	
	/**Gets the time that corresponds to the index sample in times.*/
	public double getTime(int sample)
	{
		if(sample<0)
			return 0;

		final int last_index = listcpid.size();
		if(sample>=last_index) {
			return listcpid.get(last_index-1).timestamp;
		}
		return listcpid.get(sample).timestamp;
	}
	
	/**Gets the cpid that corresponds to the index sample in timeLine.*/
	public int getCpid(int sample)
	{
		return listcpid.get(sample).cpid;
	}
	

	
	/**Shifts all the times in the ProcessTimeline to the left by lowestStartingTime.*/
	public void shiftTimeBy(double lowestStartingTime)
	{
		for(int i = 0; i<listcpid.size(); i++)
		{
			TimeCPID timecpid = listcpid.get(i);
			timecpid.timestamp = timecpid.timestamp - lowestStartingTime;
			listcpid.set(i,timecpid);
		}
	}

	
	
	/**Returns the number of elements in this ProcessTimeline.*/
	public int size()
	{
		return listcpid.size();
	}

	
	/**Finds the sample to which 'time' most closely corresponds in the ProcessTimeline.
	 * @param time: the requested time
	 * @return the index of the sample if the time is within the range, -1 otherwise 
	 * */
	public int findMidpointBefore(double time)
	{
		int low = 0;
		int high = listcpid.size() - 1;
		
		// do not search the sample if the time is out of range
		if (time < listcpid.get(low).timestamp || time>listcpid.get(high).timestamp) 
			return -1;
		
		int mid = ( low + high ) / 2;
		
		while( low != mid )
		{
			final double time_current = getTimeMidPoint(mid,mid+1);
			
			if (time > time_current)
				low = mid;
			else
				high = mid;
			mid = ( low + high ) / 2;
			
		}
		if (time >= getTimeMidPoint(low,low+1))
			return low+1;
		else
			return low;
	}

	
	public BaseDataFile getTraceData()
	{
		return this.data;
	}
	
	
	private double getTimeMidPoint(int left, int right) {
		return (listcpid.get(left).timestamp + listcpid.get(right).timestamp) / 2.0;
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
	private int binaryFill(long minLoc, long maxLoc, int startPixel, int endPixel, int minIndex, 
			double pixelLength, double startingTime)
	{
		int midPixel = (startPixel+endPixel)/2;
		if (midPixel == startPixel)
			return 0;
		
		long loc = findBoundedLocRAF(midPixel*pixelLength+startingTime, minLoc, maxLoc);
		
		final TimeCPID nextData = this.getData(loc);
		
		addSample(minIndex, nextData);
		
		int addedLeft = binaryFill(minLoc, loc, startPixel, midPixel, minIndex, pixelLength, startingTime);
		int addedRight = binaryFill(loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1, pixelLength, startingTime);
		
		return (addedLeft+addedRight+1);
	}
	
	/*********************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid)
	 *	Precondition: the location of the trace data is between minLoc and maxLoc.
	 * @param time: the time to be found
	 * @param leftloc: the relative start location. 0 means the beginning of the data in a process
	 * @param rightloc: the relative end location.
	 ********************************************************************************/
	private long findBoundedLocRAF(double time, long leftLoc, long rightLoc)
	{
		if (leftLoc == rightLoc)
			return leftLoc;
		long low = getRelativeLocation(leftLoc);
		long high = getRelativeLocation(rightLoc);
		long mid = (low+high)/2;
		final LargeByteBuffer masterBuff = data.getMasterBuffer();
		
		double temp = 0;
		long midAbsPos;
		while(low < mid)
		{
			midAbsPos = getAbsoluteLocation(mid);
			temp = masterBuff.getLong(midAbsPos);
			if (time > temp)
				low = mid;
			else
				high = mid;
			
			mid = (low+high)/2;
		}
		return getAbsoluteLocation(low);
	}
	
	
	private long getAbsoluteLocation(long relativePosition)
	{
		return minloc + (relativePosition*SIZE_OF_TRACE_RECORD);
	}
	
	private long getRelativeLocation(long absolutePosition)
	{
		return (absolutePosition-minloc)/SIZE_OF_TRACE_RECORD;
	}
	
	
	/**Adds a sample to times and timeLine.*/
	public void addSample( int index, TimeCPID datacpid)
	{		
		if (index == listcpid.size())
		{
			this.listcpid.add(datacpid);
		}
		else
		{
			this.listcpid.add(index, datacpid);
		}
	}

	
	public Vector<TimeCPID> getListOfData()
	{
		return this.listcpid;
	}
	
	
	public void setListOfData(Vector<TimeCPID> anotherList)
	{
		this.listcpid = anotherList;
	}
	
	
	private TimeCPID getData(long location) {
		
		final LargeByteBuffer masterBuff = data.getMasterBuffer();
		final double time = masterBuff.getLong(location);
		final int cpid = masterBuff.getInt(location+Constants.SIZEOF_LONG);

		return new TimeCPID(time,cpid);
	}
	

	/*********************************************************************************************
	 * Removes unnecessary samples:
	 * i.e. if timeLine had three of the same cpid's in a row, the middle one would be superfluous,
	 * as we would know when painting that it should be the same color all the way through.
	 ********************************************************************************************/
	private void postProcess()
	{
		int len = listcpid.size();
		for(int i = 0; i < len-2; i++)
		{
			while(i < len-1 && listcpid.get(i).timestamp==(listcpid.get(i+1).timestamp))
			{
				listcpid.remove(i+1);
				len--;
			}
		}
	}
	

	/***
	 * struct object of time and CPID pair
	 * 
	 * @author laksonoadhianto
	 *
	 */
	private class TimeCPID 
	{
		public double timestamp;
		public int cpid;
		
		public TimeCPID(double _timestamp, int _cpid) {
			this.timestamp = _timestamp;
			this.cpid = _cpid;
		}
	}

}
