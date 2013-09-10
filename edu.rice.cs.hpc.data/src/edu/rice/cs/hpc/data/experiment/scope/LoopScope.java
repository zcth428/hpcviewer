//////////////////////////////////////////////////////////////////////////
//																		//
//	LoopScope.java														//
//																		//
//	experiment.scope.LoopScope -- a loop scope							//
//	Last edited: August 10, 2001 at 2:22 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LOOP-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A loop scope in an HPCView experiment.
 *
 */


public class LoopScope extends Scope
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LoopScope.
 ************************************************************************/
	
public LoopScope(BaseExperiment experiment, SourceFile file, int first, int last, int cct_id, int flat_id)
{
	super(experiment, file, first, last, cct_id, flat_id);
//	this.id = "LoopScope";
}

public LoopScope(BaseExperiment experiment, SourceFile file, int first, int last)
{
	super(experiment, file, first, last, Scope.idMax, Scope.idMax);
	Scope.idMax++;
//	this.id = "LoopScope";
}

public Scope duplicate() {
    return new LoopScope(this.experiment,  this.sourceFile,  
    		this.firstLineNumber,  this.lastLineNumber, this.cct_node_index, this.flat_node_index);
}


//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	return "loop at " + this.getSourceCitation();
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
	return "loop at " + this.getLineNumberCitation();
}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}


}








