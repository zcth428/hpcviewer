//All functionality merged into TraceDataByRank, Eclipse just can't delete this file...
/*package edu.rice.cs.hpc.traceviewer.db;

import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;
import edu.rice.cs.hpc.traceviewer.util.Debugger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TraceDataByRankLocal extends TraceDataByRank {


	
	final private IBaseData data;
	final private int rank;
	final private long minloc;
	final private long maxloc;
	final private int numPixelH;
	
	*//***
	 * Create a new instance of trace data for a given rank of process or thread 
	 * 
	 * @param _data
	 * @param _rank
	 * @param _numPixelH
	 *//*
	public TraceDataByRankLocal(IBaseData _data, int _rank, int _numPixelH, final int header_size)
	{
		super(_data, _rank, _numPixelH);
		data = _data;
		rank = _rank;
		
		final long offsets[] = data.getOffsets();
		minloc = offsets[rank] + header_size;
		maxloc = data.getMaxLoc(_rank, header.RecordSz);
		
		numPixelH = _numPixelH;
		
		listcpid = new Vector<TraceDataByRank.Record>(numPixelH);
	}
	
	
	*//***
	 * reading data from file
	 * 
	 * @param timeStart
	 * @param timeRange
	 * @param pixelLength : number of records
	 *//*
	public void getData(double timeStart, double timeRange, double pixelLength)
	{
		Debugger.printDebug(1, "getData loc [" + minloc+","+ maxloc + "]");
		
		// get the start location
		final long startLoc = this.findTimeInInterval(timeStart, minloc, maxloc);
		
		// get the end location
		final double endTime = timeStart + timeRange;
		final long endLoc = Math.min(this.findTimeInInterval(endTime, minloc, maxloc)+header.RecordSz, maxloc );

		// get the number of records data to display
		final long numRec = 1+this.getNumberOfRecords(startLoc, endLoc);
		
		// --------------------------------------------------------------------------------------------------
		// if the data-to-display is fit in the display zone, we don't need to use recursive binary search
		//	we just simply display everything from the file
		// --------------------------------------------------------------------------------------------------
		if (numRec<=numPixelH) {
			
			// display all the records
			for(long i=startLoc;i<=endLoc; ) {
				listcpid.add(getData(i));
				// one record of data contains of an integer (cpid) and a long (time)
				i =  i + header.RecordSz;
			}
			
		} else {
			
			// the data is too big: try to fit the "big" data into the display
			
			//fills in the rest of the data for this process timeline
			this.sampleTimeLine(startLoc, endLoc, 0, numPixelH, 0, pixelLength, timeStart);
			
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the last data if necessary: the rightmost time is still less then the upper limit
		// 	I think we can add the rightmost data into the list of samples
		// --------------------------------------------------------------------------------------------------
		if (endLoc < maxloc) {
			final Record dataLast = this.getData(endLoc);
			this.addSample(listcpid.size(), dataLast);
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the first data if necessary: the leftmost time is still bigger than the lower limit
		//	similarly, we add to the list 
		// --------------------------------------------------------------------------------------------------
		if ( startLoc > minloc ) {
			final Record dataFirst = this.getData(startLoc - header.RecordSz);
			this.addSample(0, dataFirst);
		}

		//postProcess();
		
	}

	public BaseDataFile getTraceData()
	{
		return this.data;
	}
	
	
	*//*******************************************************************************************
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
	 ******************************************************************************************//*
	private int sampleTimeLine(long minLoc, long maxLoc, int startPixel, int endPixel, int minIndex, 
			double pixelLength, double startingTime)
	{
		int midPixel = (startPixel+endPixel)/2;
		if (midPixel == startPixel)
			return 0;
		
		long loc = findTimeInInterval(midPixel*pixelLength+startingTime, minLoc, maxLoc);
		
		final Record nextData = this.getData(loc);
		
		addSample(minIndex, nextData);
		
		int addedLeft = sampleTimeLine(minLoc, loc, startPixel, midPixel, minIndex, pixelLength, startingTime);
		int addedRight = sampleTimeLine(loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1, pixelLength, startingTime);
		
		return (addedLeft+addedRight+1);
	}
	
	*//*********************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid)
	 *	Precondition: the location of the trace data is between minLoc and maxLoc.
	 * @param time: the time to be found
	 * @param left_boundary_offset: the start location. 0 means the beginning of the data in a process
	 * @param right_boundary_offset: the end location.
	 ********************************************************************************//*
	private long findTimeInInterval(double time, long left_boundary_offset, long right_boundary_offset)
	{
		if (left_boundary_offset == right_boundary_offset) return left_boundary_offset;


		long left_index = getRelativeLocation(left_boundary_offset);
		long right_index = getRelativeLocation(right_boundary_offset);
		
		double left_time = data.getLong(left_boundary_offset);
		double right_time = data.getLong(right_boundary_offset);
		
		// apply "Newton's method" to find target time
		while (right_index - left_index > 1) {
			long predicted_index;
			double rate = (right_time - left_time) / (right_index - left_index);
			double mtime = (right_time - left_time) / 2;
			if (time <= mtime) {
				predicted_index = Math.max((long) ((time - left_time) / rate) + left_index, left_index);
			} else {
				predicted_index = Math.min((right_index - (long) ((right_time - time) / rate)), right_index); 
				if (tmp_index<0) {
					predicted_index = Math.max(tmp_index, left_index);
				} else {
					// original code: predicted_index = Math.min((right_index - (long) ((right_time - time) / rate)), right_index);
					predicted_index = Math.min(tmp_index, right_index);
				}
				
			}
			
			// adjust so that the predicted index differs from both ends
			// except in the case where the interval is of length only 1
			// this helps us achieve the convergence condition
			if (predicted_index <= left_index) 
				predicted_index = left_index + 1;
			if (predicted_index >= right_index)
				predicted_index = right_index - 1;

			double temp = data.getLong(getAbsoluteLocation(predicted_index));
			if (time >= temp) {
				left_index = predicted_index;
				left_time = temp;
			} else {
				right_index = predicted_index;
				right_time = temp;
			}
		}
		long left_offset = getAbsoluteLocation(left_index);
		long right_offset = getAbsoluteLocation(right_index);

		left_time = data.getLong(left_offset);
		right_time = data.getLong(right_offset);

		// return the closer sample or the maximum sample if the 
		// time is at or beyond the right boundary of the interval
		final boolean is_left_closer = Math.abs(time - left_time) < Math.abs(right_time - time);
		if ( is_left_closer ) return left_offset;
		else if (right_offset < this.maxloc) return right_offset;
		else return this.maxloc;
	}
	
	private long getAbsoluteLocation(long relativePosition)
	{
		return data.getMinLoc(rank) + (relativePosition * header.RecordSz);
	}
	
	private long getRelativeLocation(long absolutePosition)
	{
		return (absolutePosition-data.getMinLoc(rank)) / header.RecordSz;
	}
	
	
	*//**Adds a sample to times and timeLine.*//*
	public void addSample( int index, Record datacpid)
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

	
	private Record getData(long location)
	{
		final double time = data.getLong(location);
		final int cpId = data.getInt(location + Constants.SIZEOF_LONG);
		int metricId = edu.rice.cs.hpc.traceviewer.util.Constants.dataIdxNULL;
		if (header.isDataCentric) {
			metricId = data.getInt(location + Constants.SIZEOF_LONG + Constants.SIZEOF_INT);
		}
		
		return new Record(time, cpId, metricId);
	}
	
	private long getNumberOfRecords(long start, long end)
	{
		return (end-start) / (header.RecordSz);
	}

	*//*********************************************************************************************
	 * Removes unnecessary samples:
	 * i.e. if timeLine had three of the same cpid's in a row, the middle one would be superfluous,
	 * as we would know when painting that it should be the same color all the way through.
	 * Philip's Note: Is this necessary? Does it actually improve performance? It messes things up on the remote side.
	 ********************************************************************************************//*
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
*/