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
	
	
	public enum ApplicationType {MULTI_PROCESSES, MULTI_THREADING, HYBRID};
	private ApplicationType type = ApplicationType.HYBRID;
	
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
		
		this.type = this.checkApplicationType(f);
		
		this.data = new DataFile[f.length];

		for(int i=0; i<f.length; i++) {
			FileNameComposition composition = this.getFileComposition(f[i]);
			
			if (composition != null) {
				
				//--------------------------------------------------------------------
				// adding list of x-axis 
				//--------------------------------------------------------------------			
				
				String x_val;
				switch (this.type) {
					case HYBRID:
						x_val = String.valueOf(composition.rank) + "." + String.valueOf(composition.thread);
						break;
					case MULTI_PROCESSES:
						x_val = String.valueOf(composition.rank);
						break;
					case MULTI_THREADING:
						x_val = String.valueOf(composition.thread);
						break;
					default:
						x_val = "unknown";
							
				}
				this.data[i] = new DataFile(f[i], x_val);
				
			}
		}
	}

	
	/****
	 * retrieve the type of application (hybrid, mpi or openmp)
	 * @return ApplicationType
	 */
	public ApplicationType getApplicationType() {
		return this.type;
	}
	
	
	/****
	 * check the type of the application (mpi, openmp or hybrid)
	 * @param files
	 * @return
	 */
	private ApplicationType checkApplicationType(File files[]) {
		boolean with_all_processes = false; 
		boolean with_all_threads = false; 

		for(int i=0; i<files.length; i++) {
			FileNameComposition file_composition = this.getFileComposition(files[i]);
			with_all_processes |= (file_composition.rank>0);
			with_all_threads   |= (file_composition.thread>0);
			
			if (with_all_processes && with_all_threads)
				return ApplicationType.HYBRID;
		}
		
		if (with_all_processes)
			return ApplicationType.MULTI_PROCESSES;
		else 
			return ApplicationType.MULTI_THREADING;
	}
	
	
	/****
	 * get the composition of the raw metric file
	 * @param file
	 * @return
	 */
	private FileNameComposition getFileComposition(File file) {
		String s = file.getName();
		String parts[] = s.split(FILE_SEPARATOR);
		if (parts.length > 4) {
			return new FileNameComposition(Integer.valueOf(parts[parts.length - POSITION_PROC_ID[FILE_VERSION] ]),
										 Integer.valueOf(parts[parts.length - POSITION_THREAD_ID[FILE_VERSION]]));
		}
		return null;
	}
	
	
	/*******************************************************************
	 * Composition of the raw metric file class
	 * @author laksonoadhianto
	 *
	 *******************************************************************/
	private class FileNameComposition {
		public int thread;
		public int rank;
		
		FileNameComposition(int rank, int thread) {
			this.rank = rank;
			this.thread = thread;
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
