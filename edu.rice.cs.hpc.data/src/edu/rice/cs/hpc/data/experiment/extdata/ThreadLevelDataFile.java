package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;


/*****************************************
 * class to manage data on thread level of a specific experiment
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataFile {

	// header bytes to skip
	static private final int HEADER_LONG	=	32;

	//-----------------------------------------------------------
	// Global variables
	//-----------------------------------------------------------
	
	private int type = Constants.MULTI_PROCESSES | Constants.MULTI_THREADING; // default is hybrid
	
	private LargeByteBuffer masterBuff;
	
	private int numFiles = 0;
	private String valuesX[];
	private long offsets[];

	/**
	 * constructor: initialize all data
	 * @throws IOException 
	 */
	public ThreadLevelDataFile(String filename, MetricRaw m) throws IOException {
		
		if (filename != null) {
			
			//---------------------------------------------
			// test file version
			//---------------------------------------------
			
			this.setData(filename, m.getID());
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
	
	
	/***
	 * assign data
	 * @param f: array of files
	 * @throws IOException 
	 */
	private void setData(String filename, int metricID) throws IOException {
		
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

	/**
	 * return all metric values of a specified node and metric index
	 * 
	 * @param nodeIndex: normalized node index 
	 * @param metricIndex: the index of the metrics
	 * @param numMetrics: the number of metrics in the experiment
	 * @return
	 */
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) {
	
		double []metrics = new double[this.numFiles];
		
		for (int i=0; i<this.numFiles; i++) {
			
			final long pos_relative = getFilePosition(nodeIndex, metricIndex, numMetrics);
			final long pos_absolute = offsets[i] + pos_relative;
			metrics[i] = (double)masterBuff.getDouble(pos_absolute);
		}
		return metrics;
	}
	
	/**
	 * get a position for a specific node index and metric index
	 * @param nodeIndex
	 * @param metricIndex
	 * @param num_metrics
	 * @return
	 */
	private long getFilePosition(long nodeIndex, int metricIndex, int num_metrics) {
		return ((nodeIndex-1) * num_metrics * Constants.SIZEOF_LONG) + (metricIndex * Constants.SIZEOF_LONG) +
			// header to skip
			HEADER_LONG;
	}
	

}
