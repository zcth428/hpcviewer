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

	/**---------------------------------------------------------------------------------------**
	 * get a specific metric for a specific node ID
	 * @param sFilename
	 * @param node_index
	 * @param metric_index
	 * @param num_metrics
	 * @return
	 **---------------------------------------------------------------------------------------**/
	public double getMetric(String sFilename, int node_index, int metric_index, int num_metrics) {
		double []metrics = this.getMetrics(sFilename, node_index, num_metrics);
		return metrics[metric_index];
	}
	
	
	/**---------------------------------------------------------------------------------------**
	 * return list of metrics for a specific node 
	 * @param sFilename
	 * @param node_index
	 * @param num_metrics
	 * @return
	 **---------------------------------------------------------------------------------------**/
	public double[] getMetrics(String sFilename, int node_index, int num_metrics) {
		try {
			RandomAccessFile file = new RandomAccessFile(sFilename, "r");
			long position = this.getFilePosition(node_index, num_metrics);
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
	 * @param node_index
	 * @param num_metrics
	 * @return
	 */
	private long getFilePosition(int node_index, int num_metrics) {
		return node_index * num_metrics * DOUBLE_FIELD_SIZE;
	}
	
	
	private void debug_print(String s) {
		if (debug)
			System.out.println(s);
	}
}
