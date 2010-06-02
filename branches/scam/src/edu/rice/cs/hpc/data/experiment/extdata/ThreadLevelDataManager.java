package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.util.Util;

/***
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataManager {

	private boolean flag_debug = false;

	private ThreadLevelDataFile data_file[];
	private Experiment experiment;
	
	public ThreadLevelDataManager(Experiment exp) {
		MetricRaw []metrics = exp.getMetricRaw();
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
			keys[i] = metrics_raw[i].getTitle();
		
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

	
	public double[] getProcessIDsDouble(int metric_raw_id) {
		String x[] = data_file[metric_raw_id].getValuesX();
		double xd[] = new double[x.length];
		for (int i=0; i<x.length; i++) {
			xd[i] = Double.valueOf(x[i]);
		}
		return xd;
	}

	
	/**
	 * 
	 * @param node_index: normalized node index
	 * @param metric_index: normalized metric index
	 * @param num_metrics
	 * @return
	 */
	public double[] getMetrics(int metric_raw_id, long node_index, int metric_index)
			throws IOException {
		if (this.data_file == null)
			return null;
		
		if (data_file[metric_raw_id] == null) {
			this.checkThreadsMetricDataFiles(metric_raw_id);
		}
		
		ThreadLevelDataFile data = this.data_file[metric_raw_id];
		
		int data_size = data.size();
		double[] metrics = new double[data_size];
		
		ThreadLevelData objData = new ThreadLevelData();
		
		debugln(System.out, "Series: " +  metric_raw_id + " node: " + node_index + " metric: " + metric_index);
		MetricRaw []metrics_raw = experiment.getMetricRaw();

		for(int i=0; i<data_size; i++) {
			metrics[i] = objData.getMetric(data.getFile(i).getAbsolutePath(), node_index, metric_index, 
					metrics_raw[metric_raw_id].getSize());
			//debug(System.out, " " + metrics[i] + " ");
		}
		debugln(System.out, "\tsize: " + data.size());
		return metrics;
	}

	
	//==============================================================================================
	// PRIVATE METHODS
	//==============================================================================================

	private void checkThreadsMetricDataFiles(int metric_raw_id) {
		
		if (data_file[metric_raw_id] != null)
			return; // it has been initialized
		
		
		File files = new File(experiment.getXMLExperimentFile().getPath());
		if (files.isFile())
			files = new File(files.getParent());
		
		MetricRaw metric = experiment.getMetricRaw()[metric_raw_id];
		File filesThreadsData[] = files.listFiles(new Util.FileThreadsMetricFilter( metric.getGlob()));
		data_file[metric_raw_id] = new ThreadLevelDataFile(filesThreadsData);
		
	}


	
	private void debugln(PrintStream stream, String s) {
		if (flag_debug) {
			stream.println(s);
		}
	}
	
	private void debug(PrintStream stream, String s) {
		if (flag_debug) {
			stream.print(s);
		}
	}
	

} 
