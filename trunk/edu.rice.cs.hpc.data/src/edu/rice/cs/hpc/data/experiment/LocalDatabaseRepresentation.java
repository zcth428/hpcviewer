package edu.rice.cs.hpc.data.experiment;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;

public class LocalDatabaseRepresentation implements IDatabaseRepresentation 
{
	final private File fileExperiment;
	final private IUserData<String, String> userData; 
	final private boolean need_metric;
	final private BaseExperiment experiment;
	private ExperimentFileXML fileXML;

	public LocalDatabaseRepresentation(File fileExperiment, 
			BaseExperiment experiment,
			IUserData<String, String> userData, 
			boolean need_metric)
	{
		this.fileExperiment = fileExperiment;
		this.userData		= userData;
		this.need_metric	= need_metric;
		this.experiment		= experiment;
	}
	
	@Override
	public ExperimentFileXML getXMLFile() {
		return fileXML;
	}

	@Override
	public void open() throws Exception
	{		
		if (fileXML == null) {
			fileXML = new ExperimentFileXML();
		}
		fileXML.parse(fileExperiment, experiment, need_metric, userData);	
	}
}
