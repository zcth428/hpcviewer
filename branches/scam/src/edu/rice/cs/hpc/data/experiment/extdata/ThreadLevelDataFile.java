package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ThreadLevelDataFile {
	/**
	 * List of series and its X-axis values
	 */
	public HashMap<String, ArrayList<String>> x_values;
	
	/**
	 * list of series and its files
	 */
	public HashMap<String, ArrayList<File>> files;

	
	/**
	 * constructor: initialize all data
	 */
	public ThreadLevelDataFile() {
		this.x_values = new HashMap<String, ArrayList<String>>();
		this.files = new HashMap<String, ArrayList<File>>();
	}

}
