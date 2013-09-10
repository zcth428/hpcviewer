package edu.rice.cs.hpc.data.experiment.extdata;

import java.util.ArrayList;

public class Filter {
	private ArrayList<String> patterns;
	private boolean toShow; 
	
	public Filter() {
		// default is to hide the matching processes
		toShow = false;
	}
	
	public void setPatterns(ArrayList<String> patterns) {
		this.patterns = patterns;
	}
	
	public void setShowMode(boolean toShow) {
		this.toShow = toShow;
	}
	
	public boolean isShownMode() {
		return toShow;
	}
	
	public ArrayList<String> getPatterns()
	{
		return patterns;
	}
} 
