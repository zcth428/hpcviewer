//////////////////////////////////////////////////////////////////////////
//																		//
//	GroupScope.java														//
//																		//
//	experiment.scope.GroupScope -- a scope with arbitrary scope			//
//									types as children					//
//	Last edited: February 8, 2005 										//
//																		//
//	(c) Copyright 2005 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;




//////////////////////////////////////////////////////////////////////////
//	CLASS GROUP-SCOPE													//
//////////////////////////////////////////////////////////////////////////

/*
 *
 * A group scope in an HPCView experiment.
 *
 */


public class GroupScope extends Scope
{


/** The name of the group scope. */
protected String groupName;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a GroupScope.
 ************************************************************************/
	
public GroupScope(BaseExperiment experiment, String groupname)
{
	super(experiment, null, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, Scope.idMax++, Scope.idMax);
	this.groupName = groupname;
//	this.id = "GroupScope";
}


public Scope duplicate() {
    return new GroupScope(this.experiment, this.groupName);
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	return "Group " +  this.groupName;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}


}
