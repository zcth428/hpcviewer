package edu.rice.cs.hpc.traceviewer.data.db;

import java.util.Arrays;
import java.util.Vector;

import edu.rice.cs.hpc.data.experiment.extdata.IBaseData2;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;

public class TraceDataCollector implements ITraceDataCollector
{
	final private int numPixelH;

	//These must be initialized in local mode. They should be considered final unless the data is remote.
	private IBaseData2 data;
	
	protected Vector<DataRecord> listcpid;
	
	/***
	 * Create a new instance of trace data for a given rank of process or thread 
	 * Used only for local
	 * @param _data
	 * @param _rank
	 * @param _numPixelH
	 */
	public TraceDataCollector(IBaseData2 reader, int numPixelH)
	{
		this.numPixelH 	= numPixelH;
		data			= reader;
	}
	
	public boolean isEmpty() {
		return listcpid == null || listcpid.size()==0;
	}
	
	public TraceDataCollector(DataRecord[] data) {
		listcpid  = new Vector<DataRecord>(Arrays.asList(data));
		numPixelH = 0;
	}
	
	/***
	 * reading data from file
	 * 
	 * @param timeStart
	 * @param timeRange
	 * @param pixelLength : number of records
	 */
	public void readInData(int rank, long timeStart, long timeRange, double pixelLength)
	{
			
		long minIndex = data.getMinLoc(rank);
		long maxIndex = data.getMaxLoc(rank);
		
		Debugger.printDebug(4, "getData loc [" + minIndex+","+ maxIndex + "]");
		
		// get the start location
		final long startIndex = this.findTimeInInterval(rank, timeStart, minIndex, maxIndex);
		
		// get the end location
		final long endTime = timeStart + timeRange;
		final long index_for_endTime = findTimeInInterval(rank, endTime, minIndex, maxIndex);
		final long endIndex = Math.min(index_for_endTime, maxIndex );

		// get the number of records data to display
		final long numRec = 1+this.getNumberOfRecords(startIndex, endIndex);
		
		// --------------------------------------------------------------------------------------------------
		// if the data-to-display is fit in the display zone, we don't need to use recursive binary search
		//	we just simply display everything from the file
		// --------------------------------------------------------------------------------------------------
		if (numRec<=numPixelH) {
			
			// display all the records
			for(long i=startIndex;i<=endIndex; ) {
				listcpid.add(getData(rank, i));
				// one record of data contains of an integer (cpid) and a long (time)
				i =  i + data.getRecordSize();
			}
		} else {
			// the data is too big: try to fit the "big" data into the display			
			//fills in the rest of the data for this process timeline
			this.sampleTimeLine(rank, startIndex, endIndex, 0, numPixelH, 0, pixelLength, timeStart);
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the last data if necessary: the rightmost time is still less then the upper limit
		// 	I think we can add the rightmost data into the list of samples
		// --------------------------------------------------------------------------------------------------
		if (endIndex < maxIndex) {
			final DataRecord dataLast = this.getData(rank, endIndex);
			this.addSample(listcpid.size(), dataLast);
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the first data if necessary: the leftmost time is still bigger than the lower limit
		//	similarly, we add to the list 
		// --------------------------------------------------------------------------------------------------
		if ( startIndex > minIndex ) {
			final DataRecord dataFirst = this.getData(rank, startIndex);
			this.addSample(0, dataFirst);
		}

		
		postProcess();
		
	}
	
	
	/**Gets the time that corresponds to the index sample in times.*/
	public long getTime(int sample)
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
		return listcpid.get(sample).cpId;
	}
	
	// Intentionally remove an unused method
	// Nathan: could you please derive your own class to add additional feature ?
	/*public int getMetricId(int sample)
	{
		return 0;//listcpid.get(sample).metricId;
	}*/

	
	/**Shifts all the times in the ProcessTimeline to the left by lowestStartingTime.*/
	public void shiftTimeBy(long lowestStartingTime)
	{
		for(int i = 0; i<listcpid.size(); i++)
		{
			DataRecord timecpid = listcpid.get(i);
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
	 * @param usingMidpoint 
	 * @param time: the requested time
	 * @return the index of the sample if the time is within the range, -1 otherwise 
	 * */
	public int findClosestSample(long time, boolean usingMidpoint)
	{
		if (listcpid.size()==0)
			return 0;

		int low = 0;
		int high = listcpid.size() - 1;
		
		long timeMin = listcpid.get(low).timestamp;
		long timeMax = listcpid.get(high).timestamp;
		
		// do not search the sample if the time is out of range
		if (time<timeMin  || time>timeMax) 
			return -1;
		
		int mid = ( low + high ) / 2;
		
		while( low != mid )
		{
			final long time_current = (usingMidpoint ? getTimeMidPoint(mid,mid+1) : listcpid.get(mid).timestamp);
			
			if (time > time_current)
				low = mid;
			else
				high = mid;
			mid = ( low + high ) / 2;
			
		}
		if (usingMidpoint)
		{
			if (time >= getTimeMidPoint(low,low+1))
				return low+1;
			else
				return low;
		} else 
		{
			// without using midpoint, we adopt the leftmost sample approach.
			// this means whoever on the left side, it will be the painted
			return low;
		}
	}

	
	
	private long getTimeMidPoint(int left, int right) {
		return (listcpid.get(left).timestamp + listcpid.get(right).timestamp) / 2;
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
	private int sampleTimeLine(int rank, long minLoc, long maxLoc, int startPixel, int endPixel, int minIndex, 
			double pixelLength, long startingTime)
	{
		int midPixel = (startPixel+endPixel)/2;
		if (midPixel == startPixel)
			return 0;
		
		long loc = findTimeInInterval(rank, (long)(midPixel*pixelLength)+startingTime, minLoc, maxLoc);
		
		final DataRecord nextData = this.getData(rank, loc);
		
		addSample(minIndex, nextData);
		
		int addedLeft = sampleTimeLine(rank, minLoc, loc, startPixel, midPixel, minIndex, pixelLength, startingTime);
		int addedRight = sampleTimeLine(rank, loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1, pixelLength, startingTime);
		
		return (addedLeft+addedRight+1);
	}
	
	/*********************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid)
	 *	Precondition: the location of the trace data is between minLoc and maxLoc.
	 * @param time: the time to be found
	 * @param left_boundary_offset: the start location. 0 means the beginning of the data in a process
	 * @param right_boundary_offset: the end location.
	 ********************************************************************************/
	private long findTimeInInterval(int rank, long time, long left_index, long right_index)
	{
		if (left_index == right_index) return left_index;
		
		long left_time = data.getLong(rank, left_index);
		long right_time = data.getLong(rank, right_index);
		
		// apply "Newton's method" to find target time
		while (right_index - left_index > 1) {
			long predicted_index;
			double rate = (right_time - left_time) / (right_index - left_index);
			long mtime = (right_time - left_time) / 2;
			if (time <= mtime) {
				predicted_index = Math.max((long) ((time - left_time) / rate) + left_index, left_index);
			} else {
				predicted_index = Math.min((right_index - (long) ((right_time - time) / rate)), right_index); 
			}
			
			// adjust so that the predicted index differs from both ends
			// except in the case where the interval is of length only 1
			// this helps us achieve the convergence condition
			if (predicted_index <= left_index) 
				predicted_index = left_index + 1;
			if (predicted_index >= right_index)
				predicted_index = right_index - 1;

			long temp = data.getLong(rank, predicted_index);
			if (time >= temp) {
				left_index = predicted_index;
				left_time = temp;
			} else {
				right_index = predicted_index;
				right_time = temp;
			}
		}
		left_time = data.getLong(rank, left_index);
		right_time = data.getLong(rank, right_index);

		// return the closer sample or the maximum sample if the 
		// time is at or beyond the right boundary of the interval
		final boolean is_left_closer = Math.abs(time - left_time) < Math.abs(right_time - time);
		long maxIndex = data.getMaxLoc(rank);
		
		if ( is_left_closer ) return left_index;
		else if (right_index < maxIndex) return right_index;
		else return maxIndex;
	}
	
	
	/**Adds a sample to times and timeLine.*/
	public void addSample( int index, DataRecord datacpid)
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

	
	public Vector<DataRecord> getListOfData()
	{
		return this.listcpid;
	}
	
	
	public void setListOfData(Vector<DataRecord> anotherList)
	{
		this.listcpid = anotherList;
	}
	
	private DataRecord getData(int rank, long location)
	{
		final long time = data.getLong(rank, location);
		final int cpId = data.getInt(rank, location + Constants.SIZEOF_LONG);
		int metricId = edu.rice.cs.hpc.traceviewer.data.util.Constants.dataIdxNULL;
		
		return new DataRecord(time, cpId, metricId);
	}
	
	private long getNumberOfRecords(long start, long end)
	{
		return (end-start) / (data.getRecordSize());
	}
	
	public void duplicate(ITraceDataCollector traceData)
	{
		this.listcpid = ((TraceDataCollector)traceData).listcpid;
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
}
