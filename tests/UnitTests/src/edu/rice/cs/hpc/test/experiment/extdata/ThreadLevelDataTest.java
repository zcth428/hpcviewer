package edu.rice.cs.hpc.test.experiment.extdata;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelData;
import junit.framework.TestCase;

public class ThreadLevelDataTest extends TestCase {
	static final private String FILENAME = "/Users/laksonoadhianto/work/data/scaled-data/madness/hpctoolkit-database-madness-laks/1.moldft-000000-001-7f0100-29612.hpcrun.hpcprof-metrics";
	static final private int NUM_METRICS = 64;
	
	private FileInputStream fileSeq;
	private DataInputStream streamSeq;
	
	public void testGetMetric() {
		ThreadLevelData file = new ThreadLevelData();
		double metrics1[] = file.getMetrics(FILENAME, 1, NUM_METRICS);
		
		this.fileSeqOpen();
		double metrics2[] = this.readSeqFile();
		
		// testing first node
		for(int i=0; i<metrics1.length; i++) {
			assertTrue(metrics1[i] == metrics2[i]);
		}
		
		// test access to second node
		metrics1 = file.getMetrics(FILENAME, 2, NUM_METRICS);
		metrics2 = this.readSeqFile();
		// testing first node
		for(int i=0; i<metrics1.length; i++) {
			assertTrue(metrics1[i] == metrics2[i]);
		}
		
		this.fileSeqClose();
	}
	
	public void testGetMetricRandom() {
		java.util.Random random = new java.util.Random();
		int index = random.nextInt(NUM_METRICS-1)+1;
		
		ThreadLevelData file = new ThreadLevelData();
		double metrics1 = file.getMetric(FILENAME, 1, index, NUM_METRICS);
		
		this.fileSeqOpen();
		double metrics2[] = this.readSeqFile();
		
		// testing first node
		assertTrue(metrics1 == metrics2[index]);
		
		// test access to second node
		metrics1 = file.getMetric(FILENAME, 2, index, NUM_METRICS);
		metrics2 = this.readSeqFile();
		// testing first node
		assertTrue(metrics1 == metrics2[index]);
		
		this.fileSeqClose();
		
	}
	
	private void fileSeqOpen() {
		try {
			fileSeq = new FileInputStream(FILENAME);
			streamSeq = new DataInputStream(fileSeq);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void fileSeqClose() {
		try {
			fileSeq.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	// testing using sequential read file
	private double[] readSeqFile() {
		double [] result = new double[NUM_METRICS];
		for(int i=0; i<NUM_METRICS; i++) {
			try {
				result[i] = (double) streamSeq.readLong();
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
				return null;
			}
		}
		return result;
	}

}
