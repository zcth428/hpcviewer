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
import edu.rice.cs.hpc.data.util.Grep;
import edu.rice.cs.hpc.data.util.IUserData;

import java.io.File;
import java.io.FileInputStream;
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





//////////////////////////////////////////////////////////////////////////
//	XML PARSING															//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Parses the file and returns the contained experiment subparts.
 *	The subparts are returned by adding them to given lists.
 *
 *	@param	experiment		Experiment object to own the parsed subparts.
 * @throws Exception 
 *
 ************************************************************************/
	
public void parse(File file, BaseExperiment experiment, boolean need_metrics, IUserData userData)
		throws	Exception
		{
	// get an appropriate input stream
	String name;
	InputStream stream;

	name = file.toString();

	// parse the stream
	final Builder builder;
	if (need_metrics)
	{
		stream = new FileInputStream(file);
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
		
		String callpathLoc = file.getParent() + "/callpath.xml";
		File callpathFile = new File(callpathLoc);
		if (!callpathFile.exists())
		{
			Grep.grep(file.getAbsolutePath(), callpathLoc, "<M ", false);
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
