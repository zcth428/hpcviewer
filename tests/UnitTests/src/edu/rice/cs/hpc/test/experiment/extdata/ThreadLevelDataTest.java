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
		assertTrue(dataTrace.getListOfRanks().length == dataTrace.getNumberOfRanks());
		assertTrue(4 == dataTrace.getNumberOfRanks());
	}
	
}
