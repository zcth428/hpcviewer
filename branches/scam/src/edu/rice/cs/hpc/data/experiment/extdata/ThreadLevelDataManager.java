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

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.util.Util;

/***
 * 
 * @author laksonoadhianto
 *
 */
public class ThreadLevelDataManager {

	private ThreadLevelDataFile data_file;
	private boolean flag_debug = true;
	private int num_metrics = 0;

	
	public ThreadLevelDataManager(String sPath, BaseMetric metrics[]) {
		this.checkThreadsMetricDataFiles(sPath);
		num_metrics = metrics.length >> 3;
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
			return (data_file.files.size()>0);
		return false;
	}
	
	
	/**
	 * thread level data may contain some experiment instances. 
	 * This will retrieve the name of all instances
	 * @return
	 */
	public String[] getSeriesName() {
		if (data_file.x_values == null || (data_file.x_values.size()==0))
			return null;
		Set<String> set = data_file.x_values.keySet();
		String[] keys = new String[data_file.x_values.size()];
		set.toArray(keys);
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
	public ArrayList<String> getProcessIDs(String series) {
		return data_file.x_values.get(series);
	}
	
	
	/**
	 * 
	 * @param node_index: normalized node index
	 * @param metric_index: normalized metric index
	 * @param num_metrics
	 * @return
	 */
	public double[] getMetrics(String series, long node_index, int metric_index)
			throws IOException {
		if (this.data_file == null)
			return null;
		
		ThreadLevelData objData = new ThreadLevelData();
		
		ArrayList<File> files = data_file.files.get(series);
		double[] metrics = new double[files.size()];
		
		debugln(System.out, "Series: " +  series + " node: " + node_index + " metric: " + metric_index);
		for(int i=0; i<files.size(); i++) {
			metrics[i] = objData.getMetric(files.get(i).getAbsolutePath(), node_index, metric_index, num_metrics);
			debug(System.out, " " + metrics[i] + " ");
		}
		debugln(System.out, "\tsize: " + files.size());
		return metrics;
	}

	
	//==============================================================================================
	// PRIVATE METHODS
	//==============================================================================================

	private void checkThreadsMetricDataFiles(String sPath) {
		File files = new File(sPath);
		if (files.isFile())
			files = new File(files.getParent());
		
		File filesThreadsData[] = files.listFiles(new Util.FileThreadsMetricFilter());
		if (filesThreadsData != null) {
			if (filesThreadsData.length > 0) {
				this.setFiles(filesThreadsData);
			}
		}
	}

	
	private void setFiles(File f[]) {
		if (data_file == null)
			data_file = new ThreadLevelDataFile();
		
		for(int i=0; i<f.length; i++) {
			String filename = f[i].getName();
			String parts[] = filename.split("-");
			if (parts.length > 4) {
				String key = parts[parts.length - 2];
				
				//--------------------------------------------------------------------
				// adding list of files
				//--------------------------------------------------------------------
				ArrayList<File> file = data_file.files.get(key);
				if (file == null) {
					file = new ArrayList<File>();
				}
				file.add(f[i]);
				data_file.files.put(key, file);
				
				//--------------------------------------------------------------------
				// adding list of x-axis 
				//--------------------------------------------------------------------
				ArrayList<String> x_value = data_file.x_values.get(key);
				if (x_value == null) {
					x_value = new ArrayList<String>();
				}
				String x_val = parts[parts.length-5] + "." + parts[parts.length-4];
				x_value.add(x_val);
				data_file.x_values.put(key, x_value);
			}
		}
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
