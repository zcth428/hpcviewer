package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.File;

public class ThreadLevelDataManager {

	private File files[] = null;
	
	public void setFiles(File f[]) {
		this.files = f;
	}
	
	public boolean isDataAvailable() {
		if (files != null)
			return (files.length>0);
		return false;
	}
	
	
	public double[] getProcessIDs() {
		if (files == null)
			return null;
		
		double IDs[] = new double[files.length];
		for(int i=0; i<files.length; i++) {
			String sFilename = files[i].getName();
			String names[] = sFilename.split("-");
			int num_name = names.length;
			if (num_name > 4) {
				// java wierd lang spec: float cannot have .0 as decimal ?
				String id = names[num_name-5];
				if (Integer.valueOf(names[num_name-4])>0) {
					id = names[num_name-5] + "." + names[num_name-4];
				} 
				IDs[i] = Double.valueOf(id);
			}
		}
		return IDs;
	}
	
	public double[] getMetrics(long node_index, int metric_index, int num_metrics) {
		if (files == null)
			return null;
		
		ThreadLevelData objData = new ThreadLevelData();
		
		int num_files = files.length;
		double metrics[] = new double[num_files];
		
		for(int i=0; i<num_files; i++) {
			metrics[i] = objData.getMetric(files[i].getAbsolutePath(), node_index, metric_index, num_metrics);
		}
		return metrics;
	}
} 
