//////////////////////////////////////////////////////////////////////////
//																		//
//	ArrayScopeList.java													//
//																		//
//	experiment.scope.ArrayScopeList -- scope list based on ArrayList	//
//	Last edited: August 21, 2001 at 2:49 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.Experiment;
//import javax.swing.AbstractListModel;
import java.util.List;
import java.util.ArrayList;





//////////////////////////////////////////////////////////////////////////
//	CLASS SCOPE-LIST													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 *	A array-based list of scopes from an HPCView experiment.
 *
 *	@see edu.rice.cs.hpc.data.experiment.scope.ScopeList#ScopeList
 */


public class ArrayScopeList extends javax.swing.AbstractListModel
implements ScopeList
{

	private static final long serialVersionUID = 3L;
/** The experiment from which this scope list's elements are drawn. */
protected Experiment experiment;

/** The user visible name of this scope list. */
protected String name;

/** A Swing list holding the scopes. (TEMPORARY) */
protected List list;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an empty scope list.
 ************************************************************************/
	
public ArrayScopeList(Experiment experiment, String name)
{
	// creation arguments
	this.experiment = experiment;
	this.name = name;

	// scope list representation
	this.list = new ArrayList();
}




/*************************************************************************
 *	Creates a scope list containing the given list of scopes.
 ************************************************************************/
	
public ArrayScopeList(Experiment experiment, List scopes, String name)
{
	// creation arguments
	this.experiment = experiment;
	this.name = name;

	// scope list representation
	this.list = new ArrayList(scopes);
}




/*************************************************************************
 *	Creates a scope list of all the immediate subscopes of a given scope.
 ************************************************************************/
	
public ArrayScopeList(Experiment experiment, Scope scope)
{
	// creation arguments
	this.experiment = experiment;
	this.name = scope.getName() + " subscopes";

	// scope list representation
	int count = scope.getSubscopeCount();
	this.list = new ArrayList(count);
	for( int k = 0;  k < count;  k++ )
		this.list.add(scope.getSubscope(k));
}




//////////////////////////////////////////////////////////////////////////
//	LIST MODEL OPERATIONS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the scope at a given index as an <code>Object</code>,
 *	as required by the <code>ListModel</code> interface.
 ************************************************************************/
	
public Object getElementAt(int index)
{
	return this.getScopeAt(index);
}




/*************************************************************************
 *	Returns the number of scopes in the list.
 ************************************************************************/
	
public int getSize()
{
	return this.list.size();
}




//////////////////////////////////////////////////////////////////////////
//	SCOPE LIST OPERATIONS												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name of the scope list.
 ************************************************************************/
	
public String getName()
{
	return this.name;
}




/*************************************************************************
 *	Returns the scope at a given index as a <code>Scope</code>
 *	for convenience.
 ************************************************************************/
	
public Scope getScopeAt(int index)
{
	return (Scope) this.list.get(index);
}




/*************************************************************************
 *	Sets the scope at a given index.
 ************************************************************************/
	
public void setScopeAt(int index, Scope scope)
{
	this.list.set(index, scope);
}




/*************************************************************************
 *	Adds a scope to the end of the scope list.
 ************************************************************************/
	
public void addScope(Scope scope)
{
	this.list.add(scope);
}




/*************************************************************************
 *	Returns the index in the list of a given scope, or -1 if not in the list.
 ************************************************************************/
	
public int indexOfScope(Scope scope)
{
	return this.list.indexOf(scope);
}




}








