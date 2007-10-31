//////////////////////////////////////////////////////////////////////////
//									//
//	ProcedureScope.java						//
//									//
//	experiment.scope.ProcedureScope -- a procedure scope		//
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


public class ProcedureScope extends Scope
{


/** The name of the procedure. */
protected String procedureName;
protected boolean isalien;


//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a ProcedureScope.
 ************************************************************************/
	
public ProcedureScope(Experiment experiment, SourceFile file, int first, int last, String proc, boolean _isalien)
{
	super(experiment, file, first, last);
	this.isalien = _isalien;
	this.procedureName = proc;
	this.id = "ProcedureScope";
}

public int hashCode() {
	int val = isalien ? 1 : 0;
	return this.procedureName.hashCode() ^ val;
}

public boolean equals(Object obj) {
	if (obj instanceof ProcedureScope) {
		ProcedureScope p = (ProcedureScope) obj;
		return this.getName().equals(p.getName()) && this.getSourceFile().getName().equals(p.getSourceFile().getName());
	} else return false;
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	if (isalien) {
		// return "[I] " + procedureName;
		return "inlined from " + this.getSourceCitation();
	} else return this.procedureName;
}


/*************************************************************************
 *	Return a duplicate of this procedure scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
	return new ProcedureScope(this.experiment, 
			this.sourceFile, 
			this.firstLineNumber, 
			this.lastLineNumber,
			this.procedureName,
			this.isalien);

}

public boolean isAlien() {
	return this.isalien;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

}


