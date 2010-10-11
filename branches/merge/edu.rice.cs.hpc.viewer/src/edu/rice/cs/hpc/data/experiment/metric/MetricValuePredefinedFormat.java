package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Formatter;

public class MetricValuePredefinedFormat implements IMetricValueFormat {
	private String format;
	
	public MetricValuePredefinedFormat(String sFormat) {
		this.format = sFormat;
	}
	
	
	public String format(MetricValue value) {
		Formatter format_str = new Formatter();
		format_str = format_str.format(format, value.value);
		if (format_str != null) {
			return format_str.toString();
		} else {
			return "";
		}
	}

}
