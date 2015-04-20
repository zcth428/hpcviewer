package edu.rice.cs.hpc.data.experiment;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.filter.IFilterData;
import edu.rice.cs.hpc.data.util.IUserData;

/*************************************
 * 
 * Experiment class without metrics
 *
 *************************************/
public class ExperimentWithoutMetrics extends BaseExperiment 
{

	public void open(File fileExperiment, IUserData<String, String> userData)
			throws	Exception
	{
		super.open(fileExperiment, userData, false);
	}


	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#duplicate()
	 */
	public ExperimentWithoutMetrics duplicate() {

		ExperimentWithoutMetrics copy = new ExperimentWithoutMetrics();

		copy.configuration 	= configuration;
		copy.fileXML 		= fileXML;
		
		return copy;
	}


	@Override
	protected void filter_finalize(RootScope rootMain, RootScope rootCCT,
			IFilterData filter) {	}

}
