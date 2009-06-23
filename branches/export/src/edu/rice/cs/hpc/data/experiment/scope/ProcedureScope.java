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
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
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
// we assume that all procedure scope has the information on load module it resides
protected LoadModuleScope objLoadModule;

/**
 * scope ID of the procedure frame. The ID is given by hpcstruct and hpcprof
 */
//protected int iScopeID;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a ProcedureScope.
 ************************************************************************/
	
public ProcedureScope(Experiment experiment, SourceFile file, int first, int last, String proc, boolean _isalien, int id)
{
	super(experiment, file, first, last, id);
	this.isalien = _isalien;
	this.procedureName = proc;
	this.objLoadModule = null;
}

public ProcedureScope(Experiment experiment, SourceFile file, int first, int last, String proc, boolean _isalien)
{
	this(experiment, file, first, last, proc, _isalien, Scope.idMax++);
}

/**
 * Laks 2008.08.25: We need a special constructor to accept the SID
 * @param experiment
 * @param file
 * @param first
 * @param last
 * @param proc
 * @param sid
 * @param _isalien
 */
public ProcedureScope(Experiment experiment, LoadModuleScope loadModule, SourceFile file, 
		int first, int last, String proc, int sid, boolean _isalien)
{
	this(experiment, file, first, last,proc,_isalien, sid);
	//this.iScopeID = sid;
	this.objLoadModule = loadModule;
}

public int hashCode() {
	int val = isalien ? 1 : 0;
	// Laks 2008.12.17: it is possible that routines with the same name are defined in different files.
	//		TODO: routine the same name with different modules in the same file
	//String sHashName = this.getSourceFile().getName() + "/" + this.procedureName;
	int iCode = this.id ^ val; //sHashName.hashCode() ^ val;
	return iCode;
	//return this.procedureName.hashCode() ^ val;
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
			this.objLoadModule,
			this.sourceFile, 
			this.firstLineNumber, 
			this.lastLineNumber,
			this.procedureName,
			this.id, // Laks 2008.08.26: add the sequence ID
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

//////////////////////////////////////////////////////////////////////////
//support for Flat visitors												//
//////////////////////////////////////////////////////////////////////////

public LoadModuleScope getLoadModule() {
	return this.objLoadModule;
}
/*
public int getSID() {
	return this.iScopeID;
} */


}


