package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/*****************************************
 * class to manage data on thread level of a specific experiment
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataFile {

	
	/**
	 * list of x axis names (process.thread) and its files
	 */
	private DataFile []data;

	
	/**
	 * constructor: initialize all data
	 */
	public ThreadLevelDataFile(File files[]) {
		if (files != null  &&  files.length>0 ) {
			data = new DataFile[files.length];
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
	 * @param id
	 * @return
	 */
	public File getFile(int id) {
		if (data != null) {
			return data[id].file;
		}
		return null;
	}
	
	
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
	
	private void setData(File f[]) {
		
		for(int i=0; i<f.length; i++) {
			String filename = f[i].getName();
			String parts[] = filename.split("-");
			if (parts.length > 4) {
				
				//--------------------------------------------------------------------
				// adding list of x-axis 
				//--------------------------------------------------------------------
				String x_val = parts[parts.length-5] + "." + parts[parts.length-4];
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
