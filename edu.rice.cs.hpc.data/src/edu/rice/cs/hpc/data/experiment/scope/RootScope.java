//////////////////////////////////////////////////////////////////////////
//																		//
//	RootScope.java														//
//																		//
//	experiment.scope.RootScope -- root scope of an experiment			//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;




//////////////////////////////////////////////////////////////////////////
//	CLASS ROOT-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * The root scope of an HPCView experiment.
 *
 */


public class RootScope extends Scope
{


/** The name of the experiment's program. */
protected String programName;
protected String rootScopeName;
protected RootScopeType rootScopeType;
//public int MAX_LEVELS=0;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a RootScope.
 ************************************************************************/
	
public RootScope(BaseExperiment experiment, String prog, String name, RootScopeType rst)
{
	super(experiment, null, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, 0,0);	
	this.programName = prog;
	this.rootScopeName = name;
//	this.id = "RootScope";
	this.rootScopeType = rst;
	
}


public Scope duplicate() {
    return new RootScope(null, this.programName, this.rootScopeName, this.rootScopeType);
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this root scope.
 ************************************************************************/
	
public String getName()
{
	return "Experiment Aggregate Metrics";
}

public String getRootName()
{
	return rootScopeName;
}


public RootScopeType getType()
{
	return rootScopeType;
}

//////////////////////////////////////////////////////////////////////////
// support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

	
}

