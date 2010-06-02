package edu.rice.cs.hpc.viewer.graph;

import java.util.Arrays;

public class Histogram {
	private double freq[];
	private double axis_x[];
	
	
	public Histogram(int n_bins, double data[]) {

		double sorted_data[] = data.clone();
		Arrays.sort(sorted_data);
		double data_min = sorted_data[0];
		double data_max = sorted_data[data.length-1];
		
		double width = ((data_max - data_min) / n_bins);
		
		freq = new double[n_bins];
		axis_x = new double[n_bins];
		
		// init frequency
		for (int i=0; i<n_bins; i++) {
			freq[i] = 0.0;
			axis_x[i] = i * width;
		}
		
		for (int i=0; i<data.length; i++) {
			int pos = (int) ( (data[i]-data_min) / width);
			if (pos >= n_bins)
				pos = n_bins - 1;
			freq[pos]++;
		}
		
	}
	
	public double[] getAxisX() {
		return this.axis_x;
	}
	
	public double[] getAxisY() {
		return this.freq;
	}
	
}