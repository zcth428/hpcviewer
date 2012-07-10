package edu.rice.cs.hpc.traceviewer.filter;

import java.util.ArrayList;

public class Filter {
	ArrayList<String> patterns;
	
	public void setPatterns(ArrayList<String> patterns) {
		this.patterns = patterns;
	}
	
	
	public ArrayList<String> getPatterns()
	{
		return patterns;
	}
} 
