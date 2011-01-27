package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import edu.rice.cs.hpc.data.experiment.Experiment;
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
	private ThreadLevelDataFile.ApplicationType application_type;
	
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
	
	
	public ThreadLevelDataFile.ApplicationType getApplicationType() {
		return this.application_type;
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
		
		int data_size = data.size();
		double[] metrics = new double[data_size];
		
		ThreadLevelData objData = new ThreadLevelData();
		
		debugln(System.out, "Series: " +  metric_glob_id + " node: " + node_index + " metric: " + metric.getRawID());
		String x[] = data.getValuesX();

		for(int i=0; i<data_size; i++) {
			metrics[i] = objData.getMetric(data.getFile(i).getAbsolutePath(), node_index, metric.getRawID(), 
					metric.getSize());
			debugln(System.out, "\t"+i + " " +  x[i] +"\t:"+metrics[i]);

		}
		debugln(System.out, "\nsize: " + data_size);
		return metrics;
	}

	
	public double getMetric(int rank_sequence, MetricRaw metric, long node_index)
	throws IOException {
		
		double value;
		ThreadLevelData objData = new ThreadLevelData();

		ThreadLevelDataFile data = this.data_file[ metric.getID() ];
		value = objData.getMetric( data.getFile(rank_sequence).getAbsolutePath(), node_index, 
					metric.getRawID(), metric.getSize() );
		
		return value;
	}
	
	
	public double[] getMetric(int rank_sequence, long node_index) throws IOException {
		
		MetricRaw metrics[] = experiment.getMetricRaw();
		double values[] = new double[metrics.length];
		ThreadLevelData objData = new ThreadLevelData();

		for (int i=0; i<metrics.length; i++) {
			MetricRaw metric = metrics[i];
			ThreadLevelDataFile data = this.data_file[ metric.getID() ];
			values[i] = objData.getMetric( data.getFile(rank_sequence).getAbsolutePath(), node_index, 
					metric.getRawID(), metric.getSize() );
		}
		
		return values;
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
		application_type = data_file[metric_raw_id].getApplicationType();
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
