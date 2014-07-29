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

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.AbstractFinalizeMetricVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.util.IUserData;




//////////////////////////////////////////////////////////////////////////
//	CLASS PROCEDURE-SCOPE						//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A procedure scope in an HPCView experiment.
 *
 */


public class ProcedureScope extends Scope  implements IMergedScope 
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
	
public ProcedureScope(BaseExperiment experiment, SourceFile file, int first, int last, 
		String proc, boolean _isalien, int cct_id, int flat_id, IUserData<String,String> userData)
{
	super(experiment, file, first, last, cct_id, flat_id);
	this.isalien = _isalien;
	this.procedureName = proc;

	if (userData != null) {
		String newName = userData.get(proc);
		if (newName != null) 
			procedureName = newName;
	}
	this.objLoadModule = null;
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
public ProcedureScope(BaseExperiment experiment, LoadModuleScope loadModule, SourceFile file, 
		int first, int last, String proc, boolean _isalien, int cct_id, int flat_id, IUserData<String,String> userData)
{
	this(experiment, file, first, last,proc,_isalien, cct_id, flat_id, userData);
	//this.iScopeID = sid;
	this.objLoadModule = loadModule;
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
		String TheProcedureWhoShouldNotBeNamed = "-";
		if (procedureName.equals(TheProcedureWhoShouldNotBeNamed)) {
			return "inlined at " + this.getSourceCitation();
		} 
		String name = procedureName.isEmpty() ? "" : "[I] " + procedureName;
		return name;
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
			this.isalien,
			this.cct_node_index, // Laks 2008.08.26: add the sequence ID
			this.flat_node_index,
			null);

}

public boolean isAlien() {
	return this.isalien;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
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

public Object[] getAllChildren(AbstractFinalizeMetricVisitor finalizeVisitor, PercentScopeVisitor percentVisitor,
		MetricValuePropagationFilter inclusiveOnly,
		MetricValuePropagationFilter exclusiveOnly) {

	return this.getChildren();
}


}


