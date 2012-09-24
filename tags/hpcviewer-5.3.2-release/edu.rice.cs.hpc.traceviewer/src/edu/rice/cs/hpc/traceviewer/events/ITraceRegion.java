package edu.rice.cs.hpc.traceviewer.events;

import org.eclipse.swt.graphics.Rectangle;

/***
 * an interface to be fired when there is a change in the selected region
 *  within a trace database
 *  
 * $LastChangedData$ 
 * $Id$
 *
 */
public interface ITraceRegion {
	public void setRegion(Rectangle new_region);
	
}
