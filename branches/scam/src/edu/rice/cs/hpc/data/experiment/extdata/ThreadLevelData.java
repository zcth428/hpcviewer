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
	 * @param nodeIndex
	 * @param metric_index
	 * @param num_metrics
	 * @return
	 **---------------------------------------------------------------------------------------**/
	public double getMetric(String sFilename, long nodeIndex, int metric_index, int num_metrics) {
		double []metrics = this.getMetrics(sFilename, nodeIndex, num_metrics);
		return metrics[metric_index];
	}
	
	
	/**---------------------------------------------------------------------------------------**
	 * return list of metrics for a specific node 
	 * @param sFilename
	 * @param nodeIndex
	 * @param num_metrics
	 * @return
	 **---------------------------------------------------------------------------------------**/
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
		return nodeIndex * num_metrics * DOUBLE_FIELD_SIZE;
	}
	
	
	private void debug_print(String s) {
		if (debug)
			System.out.println(s);
	}
}
