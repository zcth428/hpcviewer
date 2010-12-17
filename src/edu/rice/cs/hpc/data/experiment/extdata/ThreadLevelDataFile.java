package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;


/*****************************************
 * class to manage data on thread level of a specific experiment
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataFile {

	//-----------------------------------------------------------
	// CONSTANTS
	//-----------------------------------------------------------
	private int FILE_VERSION;		// version of the raw metric file 
	final private String FILE_SEPARATOR = "-";		// common separator for the filename
	final private int POSITION_PROC_ID[] = {5,6};	// position of the process ID in the filename
	final private int POSITION_THREAD_ID[] = {4,5};	// position of the thread ID in the filename
	
	/**
	 * list of x axis names (process.thread) and its files
	 */
	private DataFile []data;
	
	
	/**
	 * constructor: initialize all data
	 */
	public ThreadLevelDataFile(File files[]) {
		
		FILE_VERSION = 1;
		
		if (files != null  &&  files.length>0 ) {
			
			//---------------------------------------------
			// test file version
			//---------------------------------------------
			String filename = files[0].getName();
			String items[] = filename.split(FILE_SEPARATOR);

			try {
				Double.valueOf(items[items.length-3]);
				
			} catch (Exception e) {
				// old version of the metric file
				FILE_VERSION = 0;
			}
			
			this.setData(files);

		}
	}

	/****
	 * retrieve the number of datas (x values)
	 * @return
	 */
	public int size() {
		if (data == null)
			return 0;
		
		return data.length;
	}
	
	
	/******
	 * retrieve the file 
	 * @param rank sequence id (not the process ID)
	 * @return file descriptor that contains the raw metric
	 */
	public File getFile(int id) {
		if (data != null) {
			return data[id].file;
		}
		return null;
	}
	
	
	/***
	 * retrieve the array of process IDs
	 * @return
	 */
	public String []getValuesX() {
		if (data != null) {
			String []values = new String[size()];
			for (int i=0; i<size(); i++) {
				values[i] = data[i].x_value;
			}
			return values;
		}
		return null;
	}
	
	
	/***
	 * assign data
	 * @param f: array of files
	 */
	private void setData(File f[]) {
		
		data = new DataFile[f.length];

		for(int i=0; i<f.length; i++) {
			String filename = f[i].getName();
			String parts[] = filename.split(FILE_SEPARATOR);
			if (parts.length > 4) {
				
				//--------------------------------------------------------------------
				// adding list of x-axis 
				//--------------------------------------------------------------------
				String x_val = parts[parts.length - POSITION_PROC_ID[FILE_VERSION] ] + "." 
						+ parts[parts.length-POSITION_THREAD_ID[FILE_VERSION]];
				data[i] = new DataFile(f[i], x_val);
			}
		}
	}

	
	/*******************************************************************
	 * 
	 * @author laksonoadhianto
	 *
	 *******************************************************************/
	private class DataFile {
		
		public String x_value;	// value of x axis
		public File file;		// file
		
		public DataFile(File f, String v) {
			this.x_value = v;
			this.file = f;
		}
	}
}
