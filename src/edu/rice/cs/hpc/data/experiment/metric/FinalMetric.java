package edu.rice.cs.hpc.data.experiment.metric;


public class FinalMetric extends Metric {

	public FinalMetric(String shortName,
			String nativeName, String displayName, boolean displayed, String format,
			boolean percent, String sampleperiod, MetricType metricType,
			int partnerIndex) {
		super(shortName, nativeName, displayName, displayed, format,
				percent, sampleperiod, metricType, partnerIndex);
		// TODO Auto-generated constructor stub
	}

	public FinalMetric(String shortName,
			String nativeName, String displayName, boolean displayed, String format,
			boolean percent, double sampleperiod, MetricType metricType,
			int partnerIndex) {
		super(shortName, nativeName, displayName, displayed, format,
				percent, sampleperiod, metricType, partnerIndex);
		// TODO Auto-generated constructor stub
	}

}
