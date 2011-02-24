package edu.rice.cs.hpc.traceviewer.events;


/*****
 * 
 *  Event fired up when there is an update for the depth of the call stack
 * $LastChangedData$ 
 * $Id$
 * 
 * @author laksonoadhianto
 *
 */
public interface ITraceDepth {

	public void setDepth(int new_depth);
}
