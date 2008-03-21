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
	
public LoadModuleScope(Experiment experiment, String lmname)
{
	super(experiment);
	this.loadModuleName = lmname;
	this.id = "LoadModuleScope";
}

public Scope duplicate() {
    return new LoadModuleScope(this.experiment, this.loadModuleName);
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


}
