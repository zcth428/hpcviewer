package edu.rice.cs.hpc.data.experiment.metric;

import java.util.Formatter;


/**********************************************************
 * 
 * @author laksonoadhianto
 *
 **********************************************************/
public class MetricValuePredefinedFormat implements IMetricValueFormat {
	private String format;
	
	public MetricValuePredefinedFormat(String sFormat) {
		this.format = sFormat;
	}
	
	
	public String format(MetricValue value) {
		String strFormat = "";
		try {
			Formatter format_str = new Formatter();
			format_str.format(format, MetricValue.getValue(value));

			if (format_str != null) {
				strFormat = format_str.toString();
			}
			format_str.close();
		} catch (java.util.IllegalFormatConversionException e) {
			System.err.println("Illegal format conversion: " + format.toString() + "\tFrom value: " + MetricValue.getValue(value));
			e.printStackTrace();
		}
		return strFormat;
	}

}
