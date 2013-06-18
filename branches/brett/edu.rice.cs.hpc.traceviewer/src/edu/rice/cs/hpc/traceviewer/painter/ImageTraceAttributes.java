package edu.rice.cs.hpc.traceviewer.painter;


/***********
 * Struct class to store attributes of a trace view
 * 
 * @author laksono
 *
 */
public class ImageTraceAttributes {
	
	public long begTime, endTime;
	public int begProcess, endProcess;
	public int numPixelsH, numPixelsV;
	public int numPixelsDepthV;

	/*************************************************************************
	 * Asserts the process bounds to make sure they're within the actual
	 * bounds of the database, are integers, and adjusts the process zoom 
	 * button accordingly.
	 *************************************************************************/
	public void assertProcessBounds(int maxProcesses)
	{
		begProcess = (int)begProcess;
		endProcess = (int) Math.ceil(endProcess);
		
		if (begProcess < 0)
			begProcess = 0;
		if (endProcess > maxProcesses)
			endProcess = maxProcesses;
	}
	
	/**************************************************************************
	 * Asserts the time bounds to make sure they're within the actual
	 * bounds of the database and adjusts the time zoom button accordingly.
	 *************************************************************************/
	public void assertTimeBounds(long maxTime)
	{
		if (begTime < 0)
			begTime = 0;
		if (endTime > maxTime)
			endTime = maxTime;
	}
	
	
	public void setProcess(int p1, int p2)
	{
		begProcess = p1;
		endProcess = p2;
	}

	
	public void setTime(long t1, long t2)
	{
		begTime = t1;
		endTime = t2;
	}
	
	public boolean sameTrace(ImageTraceAttributes other)
	{
		return ( begTime==other.begTime && endTime==other.endTime &&
				 begProcess==other.begProcess && endProcess==other.endProcess &&
				 numPixelsH==other.numPixelsH && numPixelsV==other.numPixelsV);
	}
	
	/***
	 * Check if two attribute instances have the same depth attribute
	 * 
	 * @param other
	 * @return
	 */
	public boolean sameDepth(ImageTraceAttributes other)
	{
		return ( begTime==other.begTime && endTime==other.endTime &&
				 numPixelsH==other.numPixelsH && numPixelsDepthV==other.numPixelsDepthV);
	}
	
	/***
	 * Copy from another attribute
	 * @param other
	 */
	public void copy(ImageTraceAttributes other)
	{
		begTime = other.begTime;
		endTime = other.endTime;
		begProcess = other.begProcess;
		endProcess = other.endProcess;
		numPixelsH = other.numPixelsH;
		numPixelsV = other.numPixelsV;
		numPixelsDepthV = other.numPixelsDepthV;
	}
	
	public String toString()
	{
		return ("T [ " + begTime + ","  + endTime+ " ]" +
				"P [ " + begProcess + "," + endProcess + " ]" + 
				" PH: " + numPixelsH + " , PV: " + numPixelsV );
	}
}
