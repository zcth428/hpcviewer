//////////////////////////////////////////////////////////////////////////
//									//
//	LoadModuleScope.java						//
//									//
//	experiment.scope.LoadModuleScope -- a load module scope		//
//	Last edited: April 4, 2003 at 5:00 pm				//
//									//
//	(c) Copyright 2003 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LOADMODULE-SCOPE						//
//////////////////////////////////////////////////////////////////////////

/*
 *
 * A load module scope in an HPCView experiment.
 *
 */


public class LoadModuleScope extends Scope
{


/** The name of the load module. */
protected String loadModuleName;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION							//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LoadModuleScope.
 ************************************************************************/
	
public LoadModuleScope(Experiment experiment, String lmname, SourceFile file, int id)
{
	super(experiment, file, id);
	this.loadModuleName = lmname;
}

public LoadModuleScope(Experiment experiment, String lmname, SourceFile file)
{
	super(experiment, file, Scope.idMax++);
	this.loadModuleName = lmname;
}


public Scope duplicate() {
    return new LoadModuleScope(this.experiment, this.loadModuleName, this.sourceFile, this.id);
}

public int hashCode() {
	return this.loadModuleName.hashCode();
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
    return "Load module " +  this.loadModuleName;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

/**
 * retrieve the original name of the module
 * @return
 */
public String getModuleName() {
	return this.loadModuleName;
}

/**
 * Load module doesn't have source file, so it needs to return its name for the citation
 * @return the citation
 */
protected String getSourceCitation()
{
	return getName();  
}

}
