package edu.rice.cs.hpc.traceviewer.filter;

import edu.rice.cs.hpc.traceviewer.timeline.ITimeline;

public interface IFilterTimeline extends ITimeline {

	public void filter( String []data) ;
}
