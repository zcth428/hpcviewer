package edu.rice.cs.hpc.viewer.metric;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.util.IProgressReport;
import edu.rice.cs.hpc.data.util.MergeDataFiles;

/***
 * manager class to handle raw metrics and its read to file
 * All access to raw metrics (aka thread level data) has to use this file
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataManager {

	private ThreadLevelDataFile data_file[];
	private Experiment experiment;
	private ThreadLevelDataCompatibility thread_data;

	public ThreadLevelDataManager(Experiment exp) {
		final MetricRaw []metrics = exp.getMetricRaw();
		if (metrics!=null)
			data_file = new ThreadLevelDataFile[metrics.length];
		this.experiment = exp;
		
		thread_data = new ThreadLevelDataCompatibility();

	}
	
	
	//==============================================================================================
	// PUBLIC METHODS
	//==============================================================================================

	/**
	 * check data availability
	 * @return true if the data is ready and available
	 */
	public boolean isDataAvailable() {
		return (data_file != null && data_file.length>0);
	}
	
	
	
	/**
	 * thread level data may contain some experiment instances. 
	 * This will retrieve the name of all instances
	 * @return
	 */
	public String[] getSeriesName() {
		MetricRaw []metrics_raw = experiment.getMetricRaw();

		if (metrics_raw == null)
			return null;
		
		String keys[] = new String[metrics_raw.length];
		for (int i=0; i<metrics_raw.length; i++)
			keys[i] = metrics_raw[i].getDisplayName();
		
		return keys;
	}
	
	
	
	/**
	 * Example of file names:
	 * 	1.t1-threads.BLD-lm-lpthread-000000-019-7f0100-25111.hpcrun.hpcprof-metrics
		1.t1-threads.BLD-lm-lpthread-000000-019-7f0100-25361.hpcrun.hpcprof-metrics
		1.t1-threads.BLD-lm-lpthread-000000-019-7f0100-25493.hpcrun.hpcprof-metrics
		1.t1-threads.BLD-lm-lpthread-000000-020-7f0100-25111.hpcrun.hpcprof-metrics
		1.t1-threads.BLD-lm-lpthread-000000-020-7f0100-25361.hpcrun.hpcprof-metrics
		1.t1-threads.BLD-lm-lpthread-000000-020-7f0100-25493.hpcrun.hpcprof-metrics

	 * @return
	 */
	public String[] getProcessIDs(int metric_raw_id) {
		return data_file[metric_raw_id].getRankLabels();
	}
	
	
	/**
	 * get the list of processor IDs. The ID has to a number. Otherwise it throws an exception 
	 * 
	 * @param metric_raw_id
	 * @return
	 * @throws NumberFormatException (in case the processor ID is not a number)
	 */
	public double[] getProcessIDsDouble(int metric_raw_id) throws NumberFormatException {
		
		String x[] = data_file[metric_raw_id].getRankLabels();
		double xd[] = new double[x.length];
		for (int i=0; i<x.length; i++) {
			xd[i] = Double.valueOf(x[i]);
		}
		return xd;
	}


	/**
	 * retrive an array of raw metric value of a given node and raw metric
	 * @param metric: raw metric
	 * @param node_index: normalized node index
	 * 
	 * @return array of doubles of metric value
	 */
	public double[] getMetrics(MetricRaw metric, long node_index)
			throws IOException {
		if (this.data_file == null)
			return null;
			
		int metric_glob_id = metric.getID();
		
		if (data_file[metric_glob_id] == null) {
			checkThreadsMetricDataFiles(metric_glob_id);
		}
		
		ThreadLevelDataFile data = this.data_file[metric_glob_id];
		if (data != null)
			return data.getMetrics(node_index, metric.getRawID(), metric.getSize(), Util.getActiveStatusLineManager());
		else
			return null;
	}

	
	public ThreadLevelDataFile getThreadLevelDataFile(int metric_id) {
		return this.data_file[metric_id];
	}
	
	
	public void dispose() {
		if (data_file != null) {
			for (ThreadLevelDataFile data: data_file) {
				if (data != null)
					data.dispose();
			}
		}
	}
	
	//==============================================================================================
	// PRIVATE METHODS
	//==============================================================================================
	private void checkThreadsMetricDataFiles(int metric_raw_id) throws IOException {
		
		File directory = experiment.getDefaultDirectory();
		if (directory.isFile())
			directory = new File(directory.getParent());

		{
			String file = thread_data.getMergedFile(directory, metric_raw_id);
			if (file == null)
				throw new IOException("Data file does not exist in " + directory.getAbsolutePath());
			
			// keep it for later uses
			data_file[metric_raw_id] = new ThreadLevelDataFile();
			data_file[metric_raw_id].open(file);
		}
			
	}

	
	
	/**
	 * class to cache the name of merged thread-level data files. 
	 * We will ask A LOT the name of merged files, thus keeping in cache will avoid us to check to often
	 * if the merged file already exist or not
	 * 
	 * The class also check compatibility with the old version.
	 *
	 */
	private class ThreadLevelDataCompatibility {
		
		private HashMap<String, String> listOfFiles;
		
		public ThreadLevelDataCompatibility() {
			listOfFiles = new HashMap<String, String>();
		}
		
		/**
		 * method to find the name of file for a given metric ID. 
		 * If the files are not merged, it will be merged automatically
		 * 
		 * The name of the merge file will depend on the glob pattern
		 * 
		 * @param directory
		 * @param metric_raw_id
		 * @return
		 * @throws IOException
		 */
		public String getMergedFile(File directory, int metric_raw_id) throws IOException {
			
			final MetricRaw metric = experiment.getMetricRaw()[metric_raw_id];
			final String globInputFile = metric.getGlob();
			
			// assuming the number of merged experiments is less than 10
			final char experiment_char = globInputFile.charAt(0);
			int experiment_id = 1;
			
			if (experiment_char>='0' && experiment_char<='9') {
				experiment_id = experiment_char - '0';
			}
			
			// ------------------------------------------------------------------------------------
			// given the metric raw id, reconstruct the name of raw metric data file
			// for instance, if raw metric id = 1, then the file should be experiment-1.mdb
			// ------------------------------------------------------------------------------------
			final String outputFile = directory.getAbsolutePath() + File.separatorChar + 
					"experiment-" + experiment_id + ".mdb";

			// check if the file is already merged
			String cacheFileName = this.listOfFiles.get(outputFile);
			
			if (cacheFileName == null) {
				
				// ----------------------------------------------------------
				// the file doesn't exist, we need to merge metric-db files
				// ----------------------------------------------------------
				// check with the old version of thread level data
				this.checkOldVersionOfData(directory);
				
				final ProgressReport progress= new ProgressReport( Util.getActiveStatusLineManager() );
				
				// ------------------------------------------------------------------------------------
				// the compact method will return the name of the compacted files.
				// if the file doesn't exist, it will be created automatically
				// ------------------------------------------------------------------------------------
				MergeDataFiles.MergeDataAttribute att = MergeDataFiles.merge(directory, 
						globInputFile, outputFile, progress);
				
				if (att == MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
					// ------------------------------------------------------------------------------------
					// the data doesn't exist. Let's try to use experiment.mdb for compatibility with the old version
					// ------------------------------------------------------------------------------------
					cacheFileName =  directory.getAbsolutePath() + File.separatorChar + "experiment.mdb";
					att = MergeDataFiles.merge(directory, globInputFile, cacheFileName, progress);
					
					if (att == MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA)
						return null;
				} else {
					cacheFileName = outputFile;
				}
				this.listOfFiles.put(outputFile, cacheFileName);

			}
			return cacheFileName;
		}
		
		private void checkOldVersionOfData(File directory) {
			
			String oldFile = directory.getAbsolutePath() + File.separatorChar + "experiment.mdb"; 
			File file = new File(oldFile);
			
			if (file.canRead()) {
				// old file already exist, needs to warn the user
				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog.openWarning(window.getShell(), "Warning ! Old version of metric data file",
						"hpcviewer has detected the presence of an old version of metric data file:\n 'experiment.mdb'\n in the directory:\n "
						+ directory.getPath() + "\nIt is highly suggested to remove the file and replace it with the original *.metric-db files from hpcprof-mpi.");
			}
		}
	}
	
	
	/*******************
	 * Progress bar
	 * @author laksonoadhianto
	 *
	 */
	private class ProgressReport implements IProgressReport 
	{
		final private IStatusLineManager statusLine;

		public ProgressReport(IStatusLineManager statusMgr)
		{
			statusLine = statusMgr;
		}
		
		public void begin(String title, int num_tasks) {
			if (statusLine != null) {
				statusLine.setMessage(title);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().update();
				statusLine.getProgressMonitor().beginTask(title, num_tasks);
			}
		}

		public void advance() {
			if (statusLine != null) 
				statusLine.getProgressMonitor().worked(1);
		}

		public void end() {
			if (statusLine != null) 
				statusLine.getProgressMonitor().done();
		}
		
	}

} 
