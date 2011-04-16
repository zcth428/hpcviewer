package edu.rice.cs.hpc.traceviewer.events;

import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpc.traceviewer.painter.Position;

/*********************************************************************
 * 
 * Base abstract class for managing events in traces. 
 * There are three events to be handled: 
 * - region: change of selected box
 * - depth: change of the current depth of call stack
 * - position: change of the cursor position (time, process)
 * 
 * @author laksonoadhianto
 *
 *********************************************************************/
public abstract class TraceEvents {

	private ITraceRegion listRegionListener[];
	private ITraceDepth listDepthListener[];
	private ITracePosition listPositionListener[];
	
	private int index_region;
	private int index_depth;
	private int index_position;
	
	public TraceEvents() {
		listRegionListener = new ITraceRegion[3];
		listDepthListener = new ITraceDepth[3];
		listPositionListener = new ITracePosition[3];
		
		index_region = 0;
		index_depth = 0;
		index_position = 0;
	}
	 
	/****
	 * register a listener of any region changes
	 * @param listener
	 */
	public void addRegionListener(ITraceRegion listener) {
		this.listRegionListener[index_region] = listener;
		index_region++;
	}
	
	/***
	 * tell the manager that an event has changed
	 * @param region
	 */
	public void updateRegion(Rectangle region) {
		
		for(ITraceRegion region_listener: listRegionListener) {
			if (region_listener != null)
				region_listener.setRegion(region);
		}
	}
	
	 
	public void addDepthListener(ITraceDepth listener) {
		this.listDepthListener[index_depth] = listener;
		index_depth++;
	}
	

	/***
	 * tell the manager that an event has changed
	 * @param region
	 */
	public void updateDepth(int depth) {
		
		setDepth(depth);
		for(ITraceDepth depth_listener: listDepthListener) {
			if (depth_listener != null)
				depth_listener.setDepth(depth);
		}
		
	}
	
	
	public void addPositionListener(ITracePosition position) {
		this.listPositionListener[index_position] = position;
		index_position++;
	}
	
	
	/***
	 * tell the manager that an event has changed
	 * @param region
	 */
	public void updatePosition(Position position) {
		
		setPosition(position);
		
		for(ITracePosition position_listener: listPositionListener) {
			if (position_listener != null) {
				position_listener.setPosition(position);
			}
		}
	}
	
	/***
	 * tell the child that we need to update the current depth
	 * @param depth
	 */
	public abstract void setDepth(int depth);
	
	public abstract void setPosition(Position position);
}
