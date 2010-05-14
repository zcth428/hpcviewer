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
	
	static private boolean debug = true;
	// from java spec: the size of a double is 8 bytes in all jvm
	static private final int DOUBLE_FIELD_SIZE   = 8;

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
			position = this.getFilePosition(nodeIndex, metric_index, num_metrics);
			file.seek(position);
			double metric = (double)file.readLong();
			file.close();
			debug_print("\tpos: " + position + " , m: " + metric);
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
			long position = this.getFilePosition(nodeIndex, num_metrics);
			file.seek(position);
			double metrics[] = new double[num_metrics];
			for(int i=0; i<num_metrics; i++) {
				metrics[i] = (double)file.readLong();
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

	
	/**
	 * get a file position for a given node ID
	 * @param nodeIndex
	 * @param num_metrics
	 * @return
	 */
	private long getFilePosition(long nodeIndex, int num_metrics) {
		return this.getFilePosition(nodeIndex, 0, num_metrics);
	}
	
	
	/**
	 * get a position for a specific node index and metric index
	 * @param nodeIndex
	 * @param metricIndex
	 * @param num_metrics
	 * @return
	 */
	private long getFilePosition(long nodeIndex, int metricIndex, int num_metrics) {
		return (nodeIndex * num_metrics * DOUBLE_FIELD_SIZE) + (metricIndex * DOUBLE_FIELD_SIZE);
	}
	
	private void debug_print(String s) {
		if (debug)
			System.out.println(s);
	}
}
