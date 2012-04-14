//////////////////////////////////////////////////////////////////////////
//									//
//	CallSiteScope.java						//
//									//
//	experiment.scope.CallSiteScope -- a function callsite scope	//
//	Last edited: February 15, 2006 					//
//	(c) Copyright 2001 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;




//////////////////////////////////////////////////////////////////////////
//	CLASS CALLSITE-SCOPE                                                //
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A callsite scope in an CSProf experiment.
 *
 */


public class CallSiteScope extends Scope
{

// Laksono 2009.09.10: get rid off unused methods
//private boolean CsprofLeaf=false;

protected LineScope lineScope;

protected ProcedureScope procScope;

protected CallSiteScopeType type;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION	
//////////////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY	
//////////////////////////////////////////////////////////////////////////




public CallSiteScope(LineScope scope, ProcedureScope scope2, 
		CallSiteScopeType csst, int cct_id, int flat_id) 
{
	super(scope2.experiment,scope2.sourceFile,scope2.firstLineNumber,
			scope2.lastLineNumber, cct_id, flat_id);
	this.lineScope = scope;
	this.procScope = scope2;
	this.type = csst;
//	this.id = "CallSiteScope";
}

public Scope duplicate() {
    return new CallSiteScope(
    		(LineScope) lineScope.duplicate(), 
    		(ProcedureScope) procScope.duplicate(), 
    		type, this.cct_node_index, this.flat_node_index);
}


/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	return this.procScope.getName();
}
/*
public int hashCode() {
	return procScope.hashCode() ^ lineScope.hashCode();
}*/

public ProcedureScope getProcedureScope()
{
	return this.procScope;
}

public LineScope getLineScope()
{
	return this.lineScope;
}

/* Laksono 2009.09.10: get rid off unused methods
public boolean getCsprofLeaf()
{
   return this.CsprofLeaf;
}


public void setCsprofLeaf(boolean value) 
{
   this.CsprofLeaf=value;
} 
*/
//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

public CallSiteScopeType getType() {
	return this.type;
}

}


