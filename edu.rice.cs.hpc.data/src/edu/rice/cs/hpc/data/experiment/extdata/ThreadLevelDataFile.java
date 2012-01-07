package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;


/*****************************************
 * class to manage data on thread level of a specific experiment
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataFile extends BaseDataFile {

	// header bytes to skip
	static private final int HEADER_LONG	=	32;


	public ThreadLevelDataFile(String filename) throws IOException {
		super(filename);
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
	
		final double []metrics = new double[this.getNumberOfFiles()];
		final long offsets[] = this.getOffsets();
		final LargeByteBuffer masterBuff = this.getMasterBuffer();
		
		for (int i=0; i<this.getNumberOfFiles(); i++) {
			
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
