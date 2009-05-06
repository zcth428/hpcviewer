//////////////////////////////////////////////////////////////////////////
//																		//
//	LineScope.java														//
//																		//
//	experiment.scope.LineScope -- a single-line scope in an experiment	//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LINE-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A single-line scope in an HPCView experiment.
 *
 */


public class LineScope extends Scope
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LineScope.
 ************************************************************************/
	
public LineScope(Experiment experiment, int sourceFile, int lineNumber)
{
	super(experiment, sourceFile, lineNumber, lineNumber);
//	this.id = "LineScope";
}




//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////





/*************************************************************************
 *	Returns the user visible name for this loop scope.
 ************************************************************************/
	
public String getName()
{
	return this.getSourceCitation();
}

public int hashCode() {
	return this.getName().hashCode();
}


/*************************************************************************
 *	Returns the short user visible name for this scope.
 *
 *	This name is only used in tree views where the scope's name appears
 *	in context with its containing scope's name.
 *
 *	Subclasses may override this to implement better short names.
 *
 ************************************************************************/
	
public String getShortName()
{
	return this.getLineNumberCitation();
}


public boolean isequal(LineScope ls)
{
	return ((this.firstLineNumber == ls.firstLineNumber) &&
		(this.lastLineNumber == ls.lastLineNumber) &&
		(this.idSourceFile == ls.idSourceFile));
}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO SCOPE														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the line number of this line scope.
 ************************************************************************/
	
public int getLineNumber()
{
	return this.firstLineNumber;
}


/*************************************************************************
 *	Return a duplicate of this line scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
	LineScope duplicatedScope = 
		new LineScope(this.experiment, 
				this.idSourceFile, 
				this.firstLineNumber);

	return duplicatedScope;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

}








