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
	
public void parse(File file, Experiment experiment, boolean need_metrics)
		throws	Exception
		{
	// get an appropriate input stream
	String name;
	InputStream stream;

	name = file.toString();
	stream = new FileInputStream(file);

	// parse the stream
	Builder builder = new ExperimentBuilder2(experiment, name, need_metrics);
	Parser parser = new Parser(name, stream, builder);
	parser.parse();

	if( builder.getParseOK() == Builder.PARSER_OK ) {
		// parsing is done successfully
	} else
		throw new InvalExperimentException(builder.getParseErrorLineNumber());        	
		}

}
