package edu.rice.cs.hpc.data.experiment;

import java.io.InputStream;

import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;

public class RemoteDatabaseRepresentation implements IDatabaseRepresentation 
{
	final private InputStream expStream;
	final private IUserData<String, String> userData;
	final private String name;
	final private BaseExperiment experiment;
	private ExperimentFileXML fileXML;
	
	public RemoteDatabaseRepresentation( BaseExperiment experiment,
			InputStream expStream, 
			IUserData<String, String> userData,
			String name)
	{
		this.expStream 	= expStream;
		this.userData  	= userData;
		this.name		= name;
		this.experiment = experiment;
	}
	
	@Override
	public ExperimentFileXML getXMLFile() {
		return null;
	}

	@Override
	public void open() throws Exception {
		
		if (fileXML == null) {
			fileXML = new ExperimentFileXML();
		}
		fileXML.parse(expStream, name, experiment, false, userData);
	}
}
