package edu.rice.cs.hpc.viewer.metric;

import java.io.IOException;
import org.eclipse.jface.action.IStatusLineManager;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
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
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics, IStatusLineManager statusMgr) {
	
		final double []metrics = new double[this.getNumberOfFiles()];
		TimelineProgressMonitor monitor = null;
		if (statusMgr != null) {
			monitor = new TimelineProgressMonitor(statusMgr);
		}

		final int numWork = this.getNumberOfFiles();
		final int num_threads = Math.min(numWork, Runtime.getRuntime().availableProcessors());
		final int numWorkPerThreads = numWork / num_threads;
		final DataReadThread threads[] = new DataReadThread[num_threads];
		
		if (monitor != null) {
			monitor.beginProgress(numWork, "Reading data ...", "Metric raw data", Util.getActiveShell());
		}
		
		// --------------------------------------------------------------
		// assign each thread for a range of files to gather the data
		// --------------------------------------------------------------
		for (int i=0; i<num_threads; i++) {
			
			final int start = i * numWorkPerThreads;
			final int end = Math.min(start+numWorkPerThreads, numWork);
			threads[i] = new DataReadThread(nodeIndex, metricIndex, numMetrics, start, end,
					monitor, metrics);
			threads[i].start();
		}
		
		// --------------------------------------------------------------
		// wait until all threads finish
		// --------------------------------------------------------------
		try {
			for (int threadNum = 0; threadNum < threads.length; threadNum++) {
				while (threads[threadNum].isAlive()) {
					Thread.sleep(30);
					if (monitor != null) {
						monitor.reportProgress();
					}
				}
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (monitor != null) {
			monitor.endProgress();
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
	
	
	/***
	 * Thread helper class to read a range of files
	 *
	 */
	private class DataReadThread extends Thread 
	{
		final private long _nodeIndex;
		final private int _metricIndex;
		final private int _numMetrics;
		final private int _indexFileStart, _indexFileEnd;
		final private TimelineProgressMonitor _monitor;
		final private double _metrics[];
		
		/***
		 * Initialization for reading a range of file from indexFileStart to indexFileEnd
		 * The caller has to create a thread and collect the output from metrics[] variable
		 * 
		 * Note: the output metrics has to have the same range as indexFileStart ... indexFileEnd
		 * 
		 * @param nodeIndex:	cct node index
		 * @param metricIndex:	metric index
		 * @param numMetrics:	number of metrics
		 * @param indexFileStart:	the beginning of file index
		 * @param indexFileEnd:		the end of file index
		 * @param monitor:		monitor for long process
		 * @param metrics:		output to gather metrics
		 */
		public DataReadThread(long nodeIndex, int metricIndex, int numMetrics,
				int indexFileStart, int indexFileEnd, TimelineProgressMonitor monitor,
				double metrics[]) {
			_nodeIndex = nodeIndex;
			_metricIndex = metricIndex;
			_numMetrics = numMetrics;
			_indexFileStart = indexFileStart;
			_indexFileEnd = indexFileEnd;
			_monitor = monitor;
			_metrics = metrics;
		}
		
		public void run() {
			final long pos_relative = getFilePosition(_nodeIndex, _metricIndex, _numMetrics);
			final LargeByteBuffer masterBuff = getMasterBuffer();
			final long offsets[] = getOffsets();
			
			for (int i=_indexFileStart; i<_indexFileEnd; i++) {
				final long pos_absolute = offsets[i] + pos_relative;
				_metrics[i] = (double)masterBuff.getDouble(pos_absolute);
				if (_monitor != null) {
					_monitor.announceProgress();
				}
			}
		}
	}

}
