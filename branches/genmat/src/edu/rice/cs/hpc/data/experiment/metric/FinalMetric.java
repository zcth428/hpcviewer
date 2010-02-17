package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class FinalMetric extends Metric {
	

	public FinalMetric(Experiment experiment, String shortName,
			String nativeName, String displayName, boolean displayed, String format,
			boolean percent, String sampleperiod, MetricType metricType,
			int partnerIndex, BaseMetric.ValueType type) {
		super(experiment, shortName, nativeName, displayName, displayed, format,
				percent, sampleperiod, metricType, partnerIndex);
		this.type = type;
	}

	public FinalMetric(Experiment experiment, String shortName,
			String nativeName, String displayName, boolean displayed, String format,
			boolean percent, double sampleperiod, MetricType metricType,
			int partnerIndex) {
		super(experiment, shortName, nativeName, displayName, displayed, format,
				percent, sampleperiod, metricType, partnerIndex);
		// TODO Auto-generated constructor stub
	}

}
