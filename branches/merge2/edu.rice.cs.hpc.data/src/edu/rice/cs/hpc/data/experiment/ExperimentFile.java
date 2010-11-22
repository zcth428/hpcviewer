//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentFile.java													//
//																		//
//	experiment.ExperimentFile -- interface for experiment files  		//
//	Last edited: June 10, 2001 at 11:59 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment;


import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;

import java.io.File;
import java.io.IOException;




//////////////////////////////////////////////////////////////////////////
//	INTERFACE EXPERIMENT-FILE											//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Interface that HPCView experiment files must implement.
 *
 */


public abstract class ExperimentFile
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an experiment file object of an appropriate subclass.
 *
 *	Currently the only available subclass is <code>ExperimentFileXML</code>.
 *
 *	@exception			IOException if experiment file can't be read.
 *	@exception			InvalExperimentException if file contents are
 *							not a valid experiment.
 *
 ************************************************************************/
	
public static ExperimentFile makeFile(File filename)
// laks: no need exception
/*throws
	IOException,
	InvalExperimentException*/
{
	return new ExperimentFileXML(filename);
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO FILE CONTENTS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Parses the file and returns the contained experiment subparts.
 *	The subparts are returned by adding them to given lists.
 *
 *	@param	experiment		Experiment object to own the parsed subparts.
 *	@exception				IOException if experiment file can't be read.
 *	@exception				InvalExperimentException if file contents are
 *								not a valid experiment.
 *
 ************************************************************************/
	
public abstract void parse(Experiment experiment)
throws
	IOException,
	InvalExperimentException;


public abstract void parse(Experiment experiment, boolean need_metrics)
throws
	IOException,
	InvalExperimentException;

}








