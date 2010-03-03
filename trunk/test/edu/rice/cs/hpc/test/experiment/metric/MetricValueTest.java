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
		assertTrue(value.isAvailable() == false);
	}

	public void testMetricValueDoubleDouble() {
		value = new MetricValue(1.0,100.0);
		assertTrue(value.isAvailable() == true);
		assertTrue(value.getValue() == 1.0);
		assertTrue(value.getPercentValue() == 100.0);
	}

	public void testMetricValueDouble() {
		value = new MetricValue(1.0);
		assertTrue(value.isAvailable() == true);
		assertTrue(value.getValue() == 1.0);
	}

	public void testIsAvailable() {
		value = new MetricValue();
		assertTrue(value.isAvailable() == false);
		value.setValue(1.0);
		assertTrue(value.isAvailable() == true);
	}

	public void testGetValue() {
		value = new MetricValue();
		value.setValue(1.0);
		assertTrue(value.getValue() == 1.0);
	}

	public void testSetValue() {
		testGetValue();
	}

	public void testIsPercentAvailable() {
		value = new MetricValue();
		value.setPercentValue(100.0);
		assertTrue(value.isPercentAvailable() == true);
	}

	public void testGetPercentValue() {
		value = new MetricValue();
		value.setPercentValue(100.0);
		assertTrue(value.getPercentValue() == 100.0);
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
		assertTrue(m1.compareTo(m2) == 0);
		
		m1.setValue(2.0);
		assertTrue(m1.compareTo(m2) == 1);
		
		m1.setValue(0.0); // make it not available
		m2.setValue(1.0);
		assertTrue(m1.compareTo(m2) == -1);
		
		m1.setValue(2.0);
		m2.setValue(1.0);
		assertTrue(m1.compareTo(m2) == 1);
		
		m1.setValue(0.5);
		assertTrue(m1.compareTo(m2) == -1);
		
		m2.setValue(0.5);
		assertTrue(m1.compareTo(m2) == 0);
	}

}
