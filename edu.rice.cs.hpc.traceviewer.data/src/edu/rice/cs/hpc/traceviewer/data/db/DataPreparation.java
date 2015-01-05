package edu.rice.cs.hpc.traceviewer.data.db;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpc.traceviewer.data.graph.CallPath;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

/***********************************************************************
 * 
 * Basic abstract class to prepare data for trace view and depth view
 * 
 * we will use an abstract method to finalize the data preparation since 
 * 	depth view has slightly different way to paint compared to
 * 	trace view
 * 
 ***********************************************************************/
public abstract class DataPreparation
{
	final protected ProcessTimeline ptl;
	final protected int depth;
	final protected int height;
	final protected double pixelLength;
	final protected ColorTable colorTable;
	final private long begTime;
	final private boolean usingMidpoint;
		
	/****
	 * Abstract class constructor to paint a line (whether it's detail view or depth view) 
	 * 
	 * @param _colorTable : color table
	 * @param _ptl : the time line manager of a process
	 * @param _spp : the painter
	 * @param _begTime : time start of the current view
	 * @param _depth : the current depth
	 * @param _height : the height (in pixel) of the line to paint
	 * @param _pixelLength : the length (in pixel)
	 * @param usingMidpoint : flag whether we should use midpoint or not
	 */
	public DataPreparation(ColorTable _colorTable, ProcessTimeline _ptl, 
			long _begTime, int _depth, int _height, double _pixelLength, boolean _usingMidpoint)
	{
		this.ptl = _ptl;
		this.depth = _depth;
		this.height = _height;
		this.pixelLength = _pixelLength;
		this.colorTable = _colorTable;
		this.begTime = _begTime;
		this.usingMidpoint = _usingMidpoint;
	}
	
	/**Painting action*/
	public void collect()
	{
		int succSampleMidpoint = (int) Math.max(0, (ptl.getTime(0)-begTime)/pixelLength);

		CallPath cp = ptl.getCallPath(0, depth);
		if (cp==null)
			return;
		
		String succFunction = cp.getScopeAt(depth).getName(); 
		Color succColor = colorTable.getColor(succFunction);
		int last_ptl_index = ptl.size() - 1;
		

		for (int index = 0; index < ptl.size(); index++)
		{
			final int currDepth = cp.getMaxDepth(); 
			int currSampleMidpoint = succSampleMidpoint;
			
			//-----------------------------------------------------------------------
			// skipping if the successor has the same color and depth
			//-----------------------------------------------------------------------
			boolean still_the_same = true;
			int indexSucc = index;
			int end = index;

			final Color currColor = succColor;
			
			while (still_the_same && (++indexSucc <= last_ptl_index))
			{
				cp = ptl.getCallPath(indexSucc, depth);
				Assert.isNotNull(cp);

				succFunction = cp.getScopeAt(depth).getName(); 
				succColor = colorTable.getColor(succFunction);
				
				// the color will be the same if and only if the two regions have the save function name
				// regardless they are from different max depth and different call path.
				// This can be misleading, but at the moment it is a good approximation
				
				still_the_same = (succColor.equals(currColor));
				if (still_the_same)
					end = indexSucc;
			}
			
			if (end < last_ptl_index)
			{
				// --------------------------------------------------------------------
				// start and middle samples: the rightmost point is the midpoint between
				// 	the two samples
				// -------------------------------------------------------------------
				// laksono 2014.11.04: previously midpoint(ptl.getTime(end),ptl.getTime(end+1)) : ptl.getTime(end);
				
				// assuming a range of three samples p0, p1 and p2 where p0 is the beginning, p1 is the last sample
				//		that has the same color as p0, and p2 is the sample that has different color as p0 (and p1)
				//		in time line: p0 < p1 < p2
				// a non-midpoint policy then should have a range of p0 to p2 with p0 color.
				
				double succ = usingMidpoint ? midpoint(ptl.getTime(end),ptl.getTime(end+1)) : ptl.getTime(end+1);
				succSampleMidpoint = (int) Math.max(0, ((succ-begTime)/pixelLength));
			}
			else
			{
				// --------------------------------------------------------------------
				// for the last iteration (or last sample), we don't have midpoint
				// 	so the rightmost point will be the time of the last sample
				// --------------------------------------------------------------------
				// succSampleMidpoint = (int) Math.max(0, ((ptl.getTime(index+1)-begTime)/pixelLength)); 
				// johnmc: replaced above because it doesn't seem correct
				succSampleMidpoint = (int) Math.max(0, ((ptl.getTime(end)-begTime)/pixelLength)); 
			}
			
			finishLine(currSampleMidpoint, succSampleMidpoint, currDepth, currColor, end - index + 1);
			index = end;
		}			
	}
	 //This is potentially vulnerable to overflows but I think we are safe for now.
	/**Returns the midpoint between x1 and x2*/
	private static long midpoint(long x1, long x2)
	{
		return (x1 + x2)/2;
	}

	public abstract TimelineDataSet getList();

	/***
	 * Abstract method to finalize the painting given its range, depth and the function name
	 * 
	 * @param currSampleMidpoint : current sample
	 * @param succSampleMidpoint : next sample
	 * @param currDepth : current depth
	 * @param functionName : name of the function (for coloring purpose)
	 * @param sampleCount : the number of "samples"
	 */
	public abstract void finishLine(int currSampleMidpoint, int succSampleMidpoint, int currDepth, Color color, int sampleCount);
}
