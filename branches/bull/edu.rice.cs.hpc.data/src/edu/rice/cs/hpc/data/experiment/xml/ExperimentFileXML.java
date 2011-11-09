//////////////////////////////////////////////////////////////////////////
//									//
//	ExperimentFileXML.java						//
//									//
//	experiment.ExperimentFileXML -- a file containing an experiment	//
//	Last edited: October 14, 2001 at 7:15 pm			//
//									//
//	(c) Copyright 2001 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.xml;


import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;




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


/** The file containing the experiment. */
protected File file;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an XML experiment file with a new temporary filename.
 ************************************************************************/
	
public ExperimentFileXML()
{
	Dialogs.notImplemented("ExperimentFileXML() constructor");
}




/*************************************************************************
 *	Creates an an XML experiment file with a given filename.
 *
 *	@param filename		A path to the file.
 *	@exception			IOException if file can't be opened for reading.
 *	@exception			InvalExperimentException if file contents are
 *							not a valid experiment.
 *
 ************************************************************************/
	
public ExperimentFileXML(File filename)
// laks: no need to throw exception
/*throws
	IOException,
	InvalExperimentException  */ 
{
	Dialogs.temporary("ExperimentFileXML(File) constructor");

	this.file = filename;

	// check for existence etc...
}




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
	
public void parse(Experiment experiment, boolean need_metrics)
throws
	Exception
	{
	// get an appropriate input stream
	String name;
	InputStream stream;
	
        name = this.file.toString();
        stream = new FileInputStream(this.file);
    
	// parse the stream
	//ExperimentBuilder builder = new ExperimentBuilder(experiment, name);
        Builder builder = new ExperimentBuilder2(experiment, name, need_metrics);
        Parser parser = new Parser(name, stream, builder);
        parser.parse();

        if( builder.getParseOK() == Builder.PARSER_OK ) {
        	// parsing is done successfully
        } else
        	throw new InvalExperimentException(builder.getParseErrorLineNumber());        	
	}

}








