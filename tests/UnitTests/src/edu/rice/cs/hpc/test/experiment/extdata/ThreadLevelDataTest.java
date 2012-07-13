package edu.rice.cs.hpc.test.experiment.extdata;

import java.io.File;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;

import junit.framework.TestCase;

public class ThreadLevelDataTest extends TestCase {
	
	
	public void testBaseData() {
		final String expFilename = "data/gauss/experiment.xml";
		final String traceFilename = "data/experiment.mt";
		
		File file = new File(expFilename);
		assertTrue ("file is not readable",file.canRead());
		
		BaseExperiment experiment = new ExperimentWithoutMetrics();
		try {
			experiment.open(file, null);
			TraceAttribute traceAttributes = experiment.getTraceAttribute();
			
			assertNotNull("Trace attributes cannot be null", traceAttributes);
			
			File traceFile = new File(traceFilename);
			IBaseData dataTrace = new BaseData(traceFile.getAbsolutePath(), traceAttributes.dbHeaderSize);
			
			assertTrue(dataTrace.getListOfRanks().length == 4);

		} catch (Exception e) {
			assertFalse("Fail to open: " + expFilename, false);
		}
	}
	
}
