/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

/**
 * @author laksonoadhianto
 *
 */
public class TestMetricFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Metric baseMetric = new Metric(null, "sn", "nn", "dn",
				true, true, "", MetricType.INCLUSIVE, 1);
		
		// test 1: 9.999 should be displayed as 1.0e+01 
		MetricValue mv = new MetricValue(9.999, .999);
		// test 2: 0.999 should be displayed as 9.99e-1
		MetricValue mv2 = new MetricValue(0.992, .009);
		MetricValue mv3 = new MetricValue(9.55, 0.0009);
		MetricValue mv4 = new MetricValue(9.5, -0.000005);
		MetricValue mv5 = new MetricValue(0.9999, -0.000005);
		MetricValue mv6 = new MetricValue(0.945, -0.000005);
		MetricValue mv7 = new MetricValue(0.95, -0.000005);
		MetricValue mv8 = new MetricValue(0.955, -0.095005);
		MetricValue mv9 = new MetricValue(0.9992, -0.095505);
		
		System.out.println("test 1: "+mv.getValue() +"\t= '"+baseMetric.getDisplayFormat().format(mv) +"'");
		System.out.println("test 2: "+mv2.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv2)+"'");
		System.out.println("test 3: "+mv3.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv3)+"'");
		System.out.println("test 4: "+mv4.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv4)+"'");
		System.out.println("test 5: "+mv5.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv5)+"'");
		System.out.println("test 6: "+mv6.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv6)+"'");
		System.out.println("test 7: "+mv7.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv7)+"'");
		System.out.println("test 8: "+mv8.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv8)+"'");
		System.out.println("test 9: "+mv9.getValue()+"\t= '"+baseMetric.getDisplayFormat().format(mv9)+"'");
	}

}
