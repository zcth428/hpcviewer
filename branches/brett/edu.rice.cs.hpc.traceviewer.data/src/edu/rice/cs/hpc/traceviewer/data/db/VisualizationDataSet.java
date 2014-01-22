package edu.rice.cs.hpc.traceviewer.data.db;

import java.util.ArrayList;
import java.util.List;

public class VisualizationDataSet {

	final private List<BaseDataVisualization> list;
	final private int height;
	
	public VisualizationDataSet( int initSize, int height ) {
	 	
		list = new ArrayList<BaseDataVisualization>(initSize);
	 	this.height = height; 
	}
	
	public void add( BaseDataVisualization data ) {
		list.add(data);
	}
	
	public List<BaseDataVisualization> getList() {
		return list;
	}
	
	public int getHeight() {
		return height;
	}
}
