package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;

public class BaseDataFile {


	//-----------------------------------------------------------
	// Global variables
	//-----------------------------------------------------------
	
	private int type = Constants.MULTI_PROCESSES | Constants.MULTI_THREADING; // default is hybrid
	
	private LargeByteBuffer masterBuff;
	
	private int numFiles = 0;
	private String valuesX[];
	private long offsets[];

	public BaseDataFile(String filename)  throws IOException 
	{
		
		if (filename != null) {
			
			//---------------------------------------------
			// test file version
			//---------------------------------------------
			
			this.setData(filename);
		}
	}
	
	/***
	 * retrieve the array of process IDs
	 * 
	 * @return
	 */
	public String []getValuesX() {
		return valuesX;
	}
	

	public int getNumberOfFiles() 
	{
		return this.numFiles;
	}
	
	public long[] getOffsets() 
	{
		return this.offsets;
	}
	
	public LargeByteBuffer getMasterBuffer()
	{
		return this.masterBuff;
	}
	
	/***
	 * assign data
	 * @param f: array of files
	 * @throws IOException 
	 */
	private void setData(String filename) throws IOException {
		
		final FileChannel f = new RandomAccessFile(filename, "r").getChannel();
		masterBuff = new LargeByteBuffer(f);

		this.type = masterBuff.getInt(0);
		this.numFiles = masterBuff.getInt(Constants.SIZEOF_INT);
		
		valuesX = new String[numFiles];
		offsets = new long[numFiles];
		
		long current_pos = Constants.SIZEOF_INT * 2;
		
		// get the procs and threads IDs
		for(int i=0; i<numFiles; i++) {

			final int proc_id = masterBuff.getInt(current_pos);
			current_pos += Constants.SIZEOF_INT;
			final int thread_id = masterBuff.getInt(current_pos);
			current_pos += Constants.SIZEOF_INT;
			
			offsets[i] = masterBuff.getLong(current_pos);
			current_pos += Constants.SIZEOF_LONG;
			
			//--------------------------------------------------------------------
			// adding list of x-axis 
			//--------------------------------------------------------------------			
			
			String x_val;
			if (this.isHybrid()) 
			{
				x_val = String.valueOf(proc_id) + "." + String.valueOf(thread_id);
			} else if (isMultiProcess()) 
			{
				x_val = String.valueOf(proc_id);					
			} else if (isMultiThreading()) 
			{
				x_val = String.valueOf(thread_id);
			} else {
				x_val = "unknown";
			}
			
			valuesX[i] = x_val;
		}
	}

	/**
	 * Check if the application is a multi-processing program (like MPI)
	 * 
	 * @return true if this is the case
	 */
	public boolean isMultiProcess() {
		return (type & Constants.MULTI_PROCESSES) != 0;
	}
	
	/**
	 * Check if the application is a multi-threading program (OpenMP for instance)
	 * 
	 * @return
	 */
	public boolean isMultiThreading() {
		return (type & Constants.MULTI_THREADING) != 0;
	}
	
	/***
	 * Check if the application is a hybrid program (MPI+OpenMP)
	 * 
	 * @return
	 */
	public boolean isHybrid() {
		return (isMultiProcess() && isMultiThreading());
	}

}