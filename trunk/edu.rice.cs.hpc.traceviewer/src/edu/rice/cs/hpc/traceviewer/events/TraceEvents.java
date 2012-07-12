package edu.rice.cs.hpc.traceviewer.events;

import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpc.traceviewer.painter.Position;

/*********************************************************************
 * 
 * Base abstract class for managing events in traces. 
 * There are three events to be handled: 
 * - region: change of selected box
 * - depth: change of the current depth of call stack
 * - data:  change of current data object
 * - position: change of the cursor position (time, process)
 * 
 * @author laksonoadhianto
 *
 *********************************************************************/
public abstract class TraceEvents {

	private ITraceRegion listRegionListener[];
	private ITraceDepth listDepthListener[];
	private ITraceData listDataListener[];
	private ITracePosition listPositionListener[];
	
	private int index_region;
	private int index_depth;
	private int index_data;
	private int index_position;
	
	public TraceEvents() {
		listRegionListener = new ITraceRegion[4];
		listDepthListener = new ITraceDepth[4];
		listDataListener  = new ITraceData[4];
		listPositionListener = new ITracePosition[4];
		
		index_region = 0;
		index_depth = 0;
		index_data = 0;
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
	public void updateDepth(int depth, Object source) {
		setDepth(depth);
		for(ITraceDepth depth_listener: listDepthListener) {
			if (depth_listener != null && source != depth_listener)
				depth_listener.setDepth(depth);
		}
		
	}
	
	
	public void addDataListener(ITraceData listener) {
		this.listDataListener[index_data] = listener;
		index_data++;
	}

	/***
	 * tell the manager that an event has changed
	 * @param region
	 */
	public void updateData(int dataIdx, Object source) {
		setData(dataIdx);
		for(ITraceData data_listener: listDataListener) {
			if (data_listener != null && source != data_listener)
				data_listener.setData(dataIdx);
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
	
	public abstract void setData(int dataIdx);
	
	public abstract void setPosition(Position position);
}
