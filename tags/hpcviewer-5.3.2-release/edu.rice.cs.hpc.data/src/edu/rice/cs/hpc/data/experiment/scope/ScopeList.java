//////////////////////////////////////////////////////////////////////////
//																		//
//	ScopeList.java														//
//																		//
//	experiment.scope.ScopeList -- a list of scopes from an experiment	//
//	Last edited: Jul 24, 2001 at 10:38 am								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


//import javax.swing.ListModel;





//////////////////////////////////////////////////////////////////////////
//	INTERFACE SCOPE-LIST												//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 *	A list of scopes from an HPCView experiment.
 *
 *	Scope lists implement the Swing <code>ListModel</code> interface.
 *	In addition, scope lists may be created in various useful ways and
 *	they may be sorted and filtered.
 *	<p>
 *	<em> (Sorting and filtering are not yet implemented.)</em>
 *	<p>
 *	Because scope lists may be very long, implementations of this
 *	interface will eventually use one or more compact representations which
 *	exploit the specific ways in which a scope list comes into being and
 *	which only make sense with respect to a particular experiment.
 *
 */


public interface ScopeList extends javax.swing.ListModel
{




//////////////////////////////////////////////////////////////////////////
//	LIST MODEL OPERATIONS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the scope at a given index as an <code>Object</code>,
 *	as required by the <code>ListModel</code> interface.
 ************************************************************************/
	
public abstract Object getElementAt(int index);




/*************************************************************************
 *	Returns the number of scopes in the list.
 ************************************************************************/
	
public abstract int getSize();




//////////////////////////////////////////////////////////////////////////
//	SCOPE LIST OPERATIONS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name of the scope list.
 ************************************************************************/
	
public abstract String getName();




/*************************************************************************
 *	Returns the scope at a given index as a <code>Scope</code>
 *	for convenience.
 ************************************************************************/
	
public abstract Scope getScopeAt(int index);




/*************************************************************************
 *	Sets the scope at a given index.
 ************************************************************************/
	
public abstract void setScopeAt(int index, Scope scope);




/*************************************************************************
 *	Adds a scope to the end of the scope list.
 ************************************************************************/
	
public abstract void addScope(Scope scope);




/*************************************************************************
 *	Returns the index in the list of a given scope.
 ************************************************************************/
	
public abstract int indexOfScope(Scope scope);




}








