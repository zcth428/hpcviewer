package edu.rice.cs.hpc.traceviewer.ui;

public interface ITraceViewAction {

	public void home();

	public void timeZoomIn();
	public void timeZoomOut();
	
	public void processZoomIn();
	public void processZoomOut();

	public void save();
	public void open();
	
	public void goNorth();
	public void goSouth();
	public void goEast();
	public void goWest();
	
}
