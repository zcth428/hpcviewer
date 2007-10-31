//////////////////////////////////////////////////////////////////////////
//									//
//	AlienScope.java						        //
//									//
//	experiment.scope.AlienScope -- an alien scope		        //
//	Last edited: August 10, 2001 at 2:22 pm				//
//									//
//	(c) Copyright 2001 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS PROCEDURE-SCOPE						//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A procedure scope in an HPCView experiment.
 *
 */


public class AlienScope extends Scope
{


/** The name of the file and procedure. */
protected String fileName;
protected String procedureName;


//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a AlienScope.
 ************************************************************************/
	
public AlienScope(Experiment experiment, SourceFile file, 
		  String fileName, String procName, 
		  int first, int last)
{
	super(experiment, file, first, last);
	this.fileName = fileName;
	this.procedureName = procName;
	this.id = "AlienScope";
}



//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
    return "alien [" + this.procedureName + "]";
}


/*************************************************************************
 *	Return a duplicate of this procedure scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
	return new AlienScope(this.experiment, 
			      this.sourceFile, 
			      this.fileName,
			      this.procedureName,
			      this.firstLineNumber, 
			      this.lastLineNumber);

}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

}
