package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**********************************************************************************************
 * class managing thread level data
 * @author laksonoadhianto
 *
 **********************************************************************************************/
public class ThreadLevelData {
	
	static private boolean debug = false;
	// from java spec: the size of a double is 8 bytes in all jvm
	static private final int DOUBLE_FIELD_SIZE   = 8;
	// header bytes to skip
	static private final int HEADER_LONG	=	32;

	/****
	 * get a specific metric for a specific node ID
	 * @param sFilename
	 * @param nodeIndex
	 * @param metric_index
	 * @param num_metrics
	 * @return
	 ****/
	public double getMetric(String sFilename, long nodeIndex, int metric_index, int num_metrics) 
			throws IOException {
		long position = 0 ;
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(sFilename, "r");
			double metric = this.readMetric(file, nodeIndex, metric_index, num_metrics);
			file.close();
			return metric;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			String msg = "Cannot find file " + sFilename +
						"\n node_index: " + nodeIndex + "\n Metric: " + metric_index + 
						" Nb metrics: " + num_metrics + " \n" +
						e.getMessage();
			throw new IOException(msg);
		} catch (IOException e) {
			e.printStackTrace();
			String msg = "Unable to access file " + sFilename + "\n position: "+ position +
						"(bytes)\n node_index: " + nodeIndex + "\n Metric: " + metric_index + 
						" Nb metrics: " + num_metrics + 
						"\n File length: " + file.length() + " (bytes)\n" + e.getMessage();
			throw new IOException(msg);
		}
	}
	
	
	/****
	 * return list of metrics for a specific node 
	 * @param sFilename
	 * @param nodeIndex
	 * @param num_metrics
	 * @return
	 ****/
	public double[] getMetrics(String sFilename, long nodeIndex, int num_metrics) {
		try {
			RandomAccessFile file = new RandomAccessFile(sFilename, "r");
			double metrics[] = new double[num_metrics];
			for(int i=0; i<num_metrics; i++) {
				metrics[i] = this.readMetric(file, nodeIndex, i, num_metrics);
				debug_print(i + ": " + metrics[i]);
			}
			file.close();
			return metrics;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/****
	 * read a raw metric (in double at the moment but can be changed in the future)
	 * @param file
	 * @param nodeIndex
	 * @param metricIndex
	 * @param num_metrics
	 * @return
	 */
	private double readMetric(RandomAccessFile file, long nodeIndex, int metricIndex, int num_metrics) {
		
		long pos = this.getFilePosition(nodeIndex, metricIndex, num_metrics);
		try {
			file.seek(pos);						// position the pointer into a certain position
			long metric_long = file.readLong();	// read the metric with 64 bits (long)
			debug_print( " \tpos: " + pos + " , m: " + metric_long + "/" + (double) metric_long);
			return (double) metric_long;		// convert in double
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
	
	/**
	 * get a position for a specific node index and metric index
	 * @param nodeIndex
	 * @param metricIndex
	 * @param num_metrics
	 * @return
	 */
	private long getFilePosition(long nodeIndex, int metricIndex, int num_metrics) {
		return (nodeIndex * num_metrics * DOUBLE_FIELD_SIZE) + (metricIndex * DOUBLE_FIELD_SIZE) +
			// header to skip
			HEADER_LONG;
	}
	
	private void debug_print(String s) {
		if (debug)
			System.out.println(s);
	}
}
