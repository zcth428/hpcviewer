package edu.rice.cs.hpc.traceviewer.painter;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.Frame;

/***********
 * Struct class to store attributes of a trace view
 * 
 *
 */
public class ImageTraceAttributes {
	
	private Frame frame;
	
	public int numPixelsH, numPixelsV;
	public int numPixelsDepthV;

	public ImageTraceAttributes()
	{
		frame = new Frame();
	}
	
	
	/*************************************************************************
	 * Asserts the process bounds to make sure they're within the actual
	 * bounds of the database, are integers, and adjusts the process zoom 
	 * button accordingly.
	 *************************************************************************/
	public void assertProcessBounds(int maxProcesses)
	{
		if (frame.begProcess < 0)
			frame.begProcess = 0;
		if (frame.endProcess > maxProcesses)
			frame.endProcess = maxProcesses;
	}
	
	/**************************************************************************
	 * Asserts the time bounds to make sure they're within the actual
	 * bounds of the database and adjusts the time zoom button accordingly.
	 *************************************************************************/
	public void assertTimeBounds(long maxTime)
	{
		if (frame.begTime < 0)
			frame.begTime = 0;
		if (frame.endTime > maxTime)
			frame.endTime = maxTime;
	}
	
	
	public void setFrame(Frame frame)
	{
		this.frame = frame;
	}
	
	public Frame getFrame()
	{
		return frame;
	}
	
	public void setProcess(int p1, int p2)
	{
		frame.begProcess = p1;
		frame.endProcess = p2;
		
		frame.fixPosition();
	}
	
	public int getProcessBegin()
	{
		return frame.begProcess;
	}
	
	public int getProcessEnd()
	{
		return frame.endProcess;
	}

	public int getProcessInterval()
	{
		return (frame.endProcess - frame.begProcess);
	}
	
	public void setTime(long t1, long t2)
	{
		frame.begTime = t1;
		frame.endTime = t2;
	}
	
	public long getTimeBegin()
	{
		return frame.begTime;
	}
	
	public long getTimeEnd()
	{
		return frame.endTime;
	}
	
	public long getTimeInterval()
	{
		long dt = frame.endTime - frame.begTime;
		// make sure we have positive time interval, even if users selects 0 time
		if (dt>0)
			return (frame.endTime - frame.begTime);
		else
			return 1;
	}
	
	public boolean sameTrace(ImageTraceAttributes other)
	{
		return ( frame.begTime==other.frame.begTime && frame.endTime==other.frame.endTime &&
				frame.begProcess==other.frame.begProcess && frame.endProcess==other.frame.endProcess &&
				 numPixelsH==other.numPixelsH && numPixelsV==other.numPixelsV);
	}
	
	public void setDepth(int depth)
	{
		frame.depth = depth;
	}
	
	public int getDepth()
	{
		return frame.depth;
	}
	
	public void setPosition(Position p)
	{
		frame.position = p;
	}
	
	public Position getPosition()
	{
		return frame.position;
	}
	
	/***
	 * Check if two attribute instances have the same depth attribute
	 * 
	 * @param other
	 * @return
	 */
	public boolean sameDepth(ImageTraceAttributes other)
	{
		return ( frame.begTime==other.frame.begTime && frame.endTime==other.frame.endTime &&
				 numPixelsH==other.numPixelsH && numPixelsDepthV==other.numPixelsDepthV);
	}
	
	/***
	 * Copy from another attribute
	 * @param other
	 */
	public void copy(ImageTraceAttributes other)
	{
		frame.begTime = other.frame.begTime;
		frame.endTime = other.frame.endTime;
		frame.begProcess = other.frame.begProcess;
		frame.endProcess = other.frame.endProcess;
		numPixelsH = other.numPixelsH;
		numPixelsV = other.numPixelsV;
		numPixelsDepthV = other.numPixelsDepthV;
	}
	
	public String toString()
	{
		return ("T [ " + frame.begTime + ","  + frame.endTime+ " ]" +
				"P [ " + frame.begProcess + "," + frame.endProcess + " ]" + 
				" PH: " + numPixelsH + " , PV: " + numPixelsV );
	}
}
