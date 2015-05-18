package edu.rice.cs.hpc.data.experiment;

import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;

public interface IDatabaseRepresentation 
{
	public ExperimentFileXML getXMLFile();
	public void open() throws	Exception;
	public IDatabaseRepresentation duplicate();
}
