//////////////////////////////////////////////////////////////////////////
//									//
//	ExperimentFileXML.java						//
//									//
//	experiment.ExperimentFileXML -- a file containing an experiment	//
//	$LastChangedDate: 2011-11-29 17:10:53 -0600 (Tue, 29 Nov 2011) $			//
//									//
//	(c) Copyright 2011 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.xml;


import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.Grep;
import edu.rice.cs.hpc.data.util.IUserData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;




//////////////////////////////////////////////////////////////////////////
//	CLASS EXPERIMENT-FILE-XML											//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A file containing an HPCView experiment in XML representation.
 *
 */


public class ExperimentFileXML extends ExperimentFile
{

	private File file;

	public File getFile() 
	{
		return file;
	}
	
	public void setFile(File file)
	{
		this.file = file;
	}
	
	
//////////////////////////////////////////////////////////////////////////
//	XML PARSING															//
//////////////////////////////////////////////////////////////////////////

/**
 * Parses the file and returns the contained experiment subparts. The
 * subparts are returned by adding them to given lists.
 * 
 * This version is able to work with the file not being located on physical
 * disk, but rather a reference to an in-memory stream from a socket
 * (potentially compressed).
 * 
 * Note: The GREP code has been moved to the <code>SpaceTimeDataControllerLocal</code>
 * because it is only needed on the local version, and that is the last
 * place where the actual file (as opposed to a stream referring to the
 * file) is available.
 * 
 * @param name
 *            The name of the file. For now, this is hard coded, but it
 *            should be obvious from the file chosen in the remote browser
 *            UI
 * @param experiment
 *            Experiment object to own the parsed subparts.
 * @param userData
 *            I don't know why this is here, since it apparently isn't used.
 *            I'm leaving it for maximum compatibility.
 * @throws Exception
 */
public void parse(InputStream stream, String name,
		BaseExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
		throws Exception {
	final Builder builder;
	if (need_metrics) {
		builder = new ExperimentBuilder2(experiment, name, userData);
	} else {
		builder = new BaseExperimentBuilder(experiment, name, userData);
	}
	// We assume it has already been GREP-ed by the server if it needs to be

	Parser parser = new Parser(name, stream, builder);
	parser.parse();

	if (builder.getParseOK() == Builder.PARSER_OK) {
		// set the file the same as the name of the database
		setFile(new File(name));
		// parsing is done successfully
	} else
		throw new InvalExperimentException(
				"Parse error in Experiment XML at line " + 
				builder.getParseErrorLineNumber());
}


/*************************************************************************
 *	Parses the file and returns the contained experiment subparts.
 *	The subparts are returned by adding them to given lists.
 *
 *	@param	experiment		Experiment object to own the parsed subparts.
 * @throws Exception 
 *
 ************************************************************************/
	
public void parse(File file, BaseExperiment experiment, boolean need_metrics, IUserData<String, String> userData)
		throws	Exception
		{
	// get an appropriate input stream
	InputStream stream;
	String name = file.toString();

	// check if the argument "file" is really a file (old version) or a directory (new version)
	String directory, xmlFilePath;
	if (file.isDirectory()) {
		directory = file.getAbsolutePath(); // it's a database directory
		xmlFilePath = directory + File.separatorChar + Constants.DATABASE_FILENAME;
	} else {
		directory = file.getParent(); // it's experiment.xml file
		xmlFilePath = file.getAbsolutePath();
	}

	File XMLfile = new File(xmlFilePath);
	
	if (!XMLfile.canRead()) {
		throw new IOException("File does not exist or not readable: " + XMLfile.getAbsolutePath());
	}
	
	setFile(XMLfile);
	
	// parse the stream
	final Builder builder;
	if (need_metrics)
	{
		stream = new FileInputStream(XMLfile);
		builder = new ExperimentBuilder2(experiment, name, userData);
	}
	else
	{
		// if we don't need metrics, we should look at callpath.xml
		//	which is a light version of experiment.xml without metrics
		// 	sax parser is not good enough in reading large xml file since
		//	it uses old technique of reader line by line. we should come
		//	up with a better xml parser.
		// note: this is a quick hack to fix slow xml reader in ibm bg something
		
		String callpathLoc = directory + File.separatorChar + "callpath.xml";
		File callpathFile = new File(callpathLoc);
		if (!callpathFile.exists())
		{
			Grep.grep(xmlFilePath, callpathLoc, "<M ", false);
			callpathFile = new File(callpathLoc);
		}
		stream = new FileInputStream(callpathFile);
		builder = new BaseExperimentBuilder(experiment, name, userData);
	}
	
	Parser parser = new Parser(name, stream, builder);
	parser.parse();

	if( builder.getParseOK() == Builder.PARSER_OK ) {
		// parsing is done successfully
	} else
		throw new InvalExperimentException(builder.getParseErrorLineNumber());        	
		}


}
