package edu.rice.cs.hpc.test.experiment.extdata;

import java.io.File;

import org.junit.Test;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;

import junit.framework.TestCase;

public class ThreadLevelDataTest extends TestCase {
	
	private BaseExperiment experiment;
	private IBaseData dataTrace;
	
	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()  {
		final String expFilename = "data/gauss/experiment.xml";
		final String traceFilename = "experiment.mt";
		
		File file = new File(expFilename);
		assertTrue ("file is not readable",file.canRead());
		
		experiment = new ExperimentWithoutMetrics();
		try {
			experiment.open(file, null);
			TraceAttribute traceAttributes = experiment.getTraceAttribute();
			
			assertNotNull("Trace attributes cannot be null", traceAttributes);
			
			File traceFile = new File(traceFilename);
			assertTrue(traceFile.canRead());
			
			dataTrace = new BaseData(traceFile.getAbsolutePath(), traceAttributes.dbHeaderSize);

		} catch (Exception e) {
			assertFalse("Fail to open: " + expFilename, false);
		}
	}
	
	@Test	
	public void testRanks() {
		// the number of ranks should match
		final String[] ranks = dataTrace.getListOfRanks();
		
		assertTrue(dataTrace.getListOfRanks().length == dataTrace.getNumberOfRanks());
		assertTrue(4 == dataTrace.getNumberOfRanks());
		
		// the name of the ranks should match
		assertEquals("0", ranks[0]);
		assertEquals("1", ranks[1]);
		assertEquals("2", ranks[2]);
		assertEquals("3", ranks[3]);
	}
	
	@Test
	public void testLocation() {
		long l0 = dataTrace.getMinLoc(0);
		assertTrue(96 == l0);
		
		int headerSize = dataTrace.getHeaderSize();
		assertTrue(24 == headerSize);
		
		long l1 = dataTrace.getMaxLoc(0, headerSize);
		assertTrue(106416 == l1);
	}
}
