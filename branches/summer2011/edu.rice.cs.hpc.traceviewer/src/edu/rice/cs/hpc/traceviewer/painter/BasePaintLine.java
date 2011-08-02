package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ProcessTimeline;

/***********************************************************************
 * 
 * Basic abstract class to paint for trace view and depth view
 * 
 * we will use an abstract method to finalize the painting since 
 * 	depth view has slightly different way to paint compared to
 * 	trace view
 * 
 ***********************************************************************/
public abstract class BasePaintLine
{
	protected ProcessTimeline ptl;
	protected SpaceTimeSamplePainter spp;
	protected int depth;
	protected int height;
	protected double pixelLength;
	protected ColorTable colorTable;
	private long begTime;
		
	public BasePaintLine(ColorTable _colorTable, ProcessTimeline _ptl, SpaceTimeSamplePainter _spp, 
			long _begTime, int _depth, int _height, double _pixelLength)
	{
		this.ptl = _ptl;
		this.spp = _spp;
		this.depth = _depth;
		this.height = _height;
		this.pixelLength = _pixelLength;
		this.colorTable = _colorTable;
		this.begTime = _begTime;
	}
	
	/**Painting action*/
	public void paint()
	{
		int succSampleMidpoint = (int) Math.max(0, (ptl.getTime(0)-begTime)/pixelLength);
		//CallStackSample succSample = ptl.getCallStackSample(0);
		//int succDepth = Math.min(depth, succSample.getSize()-1);
		//String succFunction = succSample.getFunctionName(succDepth);
		CallPath cp = ptl.getCallPath(0, depth);
		int succDepth = cp.getCurrentDepth();
		String succFunction = cp.getCurrentDepthScope().getName();
		Color succColor = colorTable.getColor(succFunction);

		for (int index = 0; index < ptl.size(); index++)
		{
			int currDepth = succDepth;
			int currSampleMidpoint = succSampleMidpoint;
			
			//-----------------------------------------------------------------------
			// skipping if the successor has the same color and depth
			//-----------------------------------------------------------------------
			boolean still_the_same = true;
			int indexSucc = index;
			final String functionName = succFunction;
			final Color currColor = succColor;
			
			while(still_the_same && (indexSucc < ptl.size()-1))
			{
				indexSucc++;
				cp = ptl.getCallPath(indexSucc, depth);
				if(cp != null)
				{
					succDepth = cp.getCurrentDepth();
					succFunction = cp.getCurrentDepthScope().getName();
					succColor = colorTable.getColor(succFunction);
					
					still_the_same = (succDepth == currDepth) && (succColor.equals(currColor));
					if (still_the_same)
						index = indexSucc;
				}
			};
			
			if (index<ptl.size()-1)
			{
				// --------------------------------------------------------------------
				// start and middle samples: the rightmost point is the midpoint between
				// 	the two samples
				// --------------------------------------------------------------------
				succSampleMidpoint = (int) Math.max(0, ((midpoint(ptl.getTime(index),ptl.getTime(index+1))-begTime)/pixelLength));

			}
			else
			{
				// --------------------------------------------------------------------
				// for the last iteration (or last sample), we don't have midpoint
				// 	so the rightmost point will be the time of the last sample
				// --------------------------------------------------------------------
				succSampleMidpoint = (int) Math.max(0, ((ptl.getTime(index+1)-begTime)/pixelLength));
			}
			
			//Debugger.printTrace(" i: " + index + "\tcs: " + currSampleMidpoint + "\tss: " + succSampleMidpoint + "\td: " + currDepth);
			this.finishPaint(currSampleMidpoint, succSampleMidpoint, currDepth, functionName);
		}			
	}
	 
	/**Returns the midpoint between x1 and x2*/
	private static double midpoint(double x1, double x2)
	{
		return (x1 + x2)/2.0;
	}


	/***
	 * Abstract method to finalize the painting given its range, depth and the function name
	 * 
	 * @param currSampleMidpoint
	 * @param succSampleMidpoint
	 * @param currDepth
	 * @param functionName
	 */
	public abstract void finishPaint(int currSampleMidpoint, int succSampleMidpoint, int currDepth, String functionName);
}
