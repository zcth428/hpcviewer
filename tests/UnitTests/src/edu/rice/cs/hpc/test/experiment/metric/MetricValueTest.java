package edu.rice.cs.hpc.test.experiment.metric;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import junit.framework.TestCase;

public class MetricValueTest extends TestCase {

	private MetricValue value;
	
	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testMetricValue() {
		value = new MetricValue();
		assertTrue(MetricValue.isAvailable(value) == false);
	}

	public void testMetricValueDoubleDouble() {
		value = new MetricValue(1.0,100.0);
		assertTrue(MetricValue.isAvailable(value) == true);
		assertTrue(MetricValue.getValue(value) == 1.0);
		assertTrue(MetricValue.getAnnotationValue(value) == 100.0);
	}

	public void testMetricValueDouble() {
		value = new MetricValue(1.0);
		assertTrue(MetricValue.isAvailable(value) == true);
		assertTrue(MetricValue.getValue(value) == 1.0);
	}

	public void testIsAvailable() {
		value = new MetricValue();
		assertTrue(MetricValue.isAvailable(value) == false);
		MetricValue.setValue(value,1.0);
		assertTrue(MetricValue.isAvailable(value) == true);
	}

	public void testGetValue() {
		value = new MetricValue();
		MetricValue.setValue(value,1.0);
		assertTrue(MetricValue.getValue(value) == 1.0);
	}

	public void testSetValue() {
		testGetValue();
	}

	public void testIsPercentAvailable() {
		value = new MetricValue();
		MetricValue.setAnnotationValue(value,100.0);
		assertTrue(MetricValue.isAnnotationAvailable(value) == true);
	}

	public void testGetPercentValue() {
		value = new MetricValue();
		MetricValue.setAnnotationValue(value,100.0);
		assertTrue(MetricValue.getAnnotationValue(value) == 100.0);
	}

	public void testSetPercentValue() {
		testGetPercentValue();
	}

	public void testCompareTo() {
		// type of tests:
		// - m & m2 are not available
		// - m1 is available 2 is not
		// - m1 is not available 2 is
		// - m1 is bigger than m2
		// - m1 is less than m2
		// - m1 == m2
		MetricValue m1, m2;
		m1 = new MetricValue();
		m2 = new MetricValue();
		assertTrue(MetricValue.compareTo(m1,m2) == 0);
		
		MetricValue.setValue(m1,2.0);
		assertTrue(MetricValue.compareTo(m1,m2) == 1);
		
		MetricValue.setValue(m1,0.0); // make it not available
		MetricValue.setValue(m2,1.0);
		assertTrue(MetricValue.compareTo(m1,m2) == -1);
		
		MetricValue.setValue(m1,2.0);
		MetricValue.setValue(m2,1.0);
		assertTrue(MetricValue.compareTo(m1,m2) == 1);
		
		MetricValue.setValue(m1,0.5);
		assertTrue(MetricValue.compareTo(m1,m2) == -1);
		
		MetricValue.setValue(m2,0.5);
		assertTrue(MetricValue.compareTo(m1,m2) == 0);
	}

}
