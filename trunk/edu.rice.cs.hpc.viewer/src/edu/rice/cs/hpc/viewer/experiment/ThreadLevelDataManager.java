package edu.rice.cs.hpc.viewer.experiment;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataFile;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.util.IProgressReport;
import edu.rice.cs.hpc.data.util.MergeDataFiles;

/***
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataManager {

	private ThreadLevelDataFile data_file[];
	private Experiment experiment;
	
	public ThreadLevelDataManager(Experiment exp) {
		final MetricRaw []metrics = exp.getMetricRaw();
		if (metrics!=null)
			data_file = new ThreadLevelDataFile[metrics.length];
		this.experiment = exp;
	}
	
	
	//==============================================================================================
	// PUBLIC METHODS
	//==============================================================================================

	/**
	 * check data availability
	 * @return true if the data is ready and available
	 */
	public boolean isDataAvailable() {
		if (data_file != null)
			return (data_file.length>0);
		return false;
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
		return data_file[metric_raw_id].getValuesX();
	}
	
	
	/**
	 * get the list of processor IDs. The ID has to a number. Otherwise it throws an exception 
	 * 
	 * @param metric_raw_id
	 * @return
	 * @throws NumberFormatException (in case the processor ID is not a number)
	 */
	public double[] getProcessIDsDouble(int metric_raw_id) throws NumberFormatException {
		
		String x[] = data_file[metric_raw_id].getValuesX();
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
			this.checkThreadsMetricDataFiles(metric_glob_id);
		}
		
		ThreadLevelDataFile data = this.data_file[metric_glob_id];
		return data.getMetrics(node_index, metric.getRawID(), metric.getSize());
	}

	
	public ThreadLevelDataFile getThreadLevelDataFile(int metric_id) {
		return this.data_file[metric_id];
	}
	
	//==============================================================================================
	// PRIVATE METHODS
	//==============================================================================================

	private void checkThreadsMetricDataFiles(int metric_raw_id) {
		
		if (data_file[metric_raw_id] != null)
			return; // it has been initialized
		
		
		File directory = new File(experiment.getXMLExperimentFile().getPath());
		if (directory.isFile())
			directory = new File(directory.getParent());
		
		MetricRaw metric = experiment.getMetricRaw()[metric_raw_id];
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPartSite site = window.getActivePage().getActivePart().getSite();
		
		IStatusLineManager statusLine = null;
		
		// --------------------------------------------------------------
		// the current active site can be either editor or view
		// if none of them is active, then we have nothing
		// --------------------------------------------------------------
		if (site instanceof IViewSite)
			statusLine = ((IViewSite)site).getActionBars().getStatusLineManager();
		else if (site instanceof IEditorPart)
			statusLine = ((IEditorSite)site).getActionBars().getStatusLineManager();
		
		try {
			// the compact method will return the name of the compacted files.
			// if the file doesn't exist, it will be created automatically
			final String file = MergeDataFiles.merge(directory, metric.getGlob(), "mdb", new ProgressReport(statusLine));
			
			data_file[metric_raw_id] = new ThreadLevelDataFile(file);
			
		} catch (IOException e) {
			e.printStackTrace();
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
			statusLine.setMessage("Starting " + title);
			statusLine.getProgressMonitor().beginTask(title, num_tasks);
		}

		public void advance() {
			statusLine.getProgressMonitor().worked(1);
		}

		public void end() {
			statusLine.getProgressMonitor().done();
		}
		
	}

} 
