//////////////////////////////////////////////////////////////////////////
//																		//
//	SourceLineMap.java													//
//																		//
//	experiment.scope.SourceLineMap -- map from line numbers to scopes	//
//	Last edited: January 29, 2001 at 12:21								//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ScopeList;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS SOURCE-LINE-MAP												//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 *	A map from line numbers to scope sets.
 *
 *	A <code>SourceLineMap</code> pertains to a single source file in an
 *	experiment. For each line in the source file, it can provide a list
 *	of scopes relating to that line. Depending on how the map was constructed,
 *	a line may map to a set of scopes which start on that line, contain that
 *	line, or end on that line. These sets are subsets of a scope list
 *	specified at construction time, which may not include all scopes in the
 *	experiment.
 *	<p>
 *	For convenience, the scope set corresponding to a line is returned as a
 *	<code>ScopeList</code> object. There is no class implementing "scope set"
 *	in the hpcviewer system.
 *
 */


public class SourceLineMap extends Object
{


/** The experiment owning the scopes in this map. */
protected Experiment experiment;

/** The source file whose lines are the domain of the map. */
protected SourceFile sourceFile;

/** The scope list whose subsets are the range of the map. */
protected ScopeList scopeList;

/** The mapping from line numbers to scope lists. */
protected ScopeList[] lineMap;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a SourceLineMap for a given scope list.
 ************************************************************************/
	
public SourceLineMap(Experiment experiment, SourceFile sourceFile, ScopeList scopeList)
{
	// creation arguments
	this.experiment = experiment;
	this.sourceFile = sourceFile;
	this.scopeList  = scopeList;

	// this.lineMap is initialized lazily
}




/*************************************************************************
 *	Creates a SourceLineMap for all scopes in the experiment.
 ************************************************************************/
	
public SourceLineMap(Experiment experiment, SourceFile sourceFile)
{
	//this(experiment, sourceFile, experiment.getScopeList());
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO MAP														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns a list of scopes related to a given line number,
 *	or <code>null</code> if there are none.
 ************************************************************************/
	
public ScopeList getScopesAt(int lineNumber)
{
	if( this.lineMap == null ) this.computeLineMap();
	return this.lineMap[lineNumber];
}




/*************************************************************************
 *	Computes and caches the mapping from line numbers to scope lists.
 ************************************************************************/
	
protected void computeLineMap()
{
	int lineCount = this.sourceFile.getLineCount();
	ScopeList[] map = new ScopeList[lineCount];

	int scopeCount = this.scopeList.getSize();
	for( int k = 0;  k < scopeCount;  k++ )
	{
		Scope s   = this.scopeList.getScopeAt(k);
		if( s.getSourceFile() == this.sourceFile )
		{
			int first = s.getFirstLineNumber();
			if( first != Scope.NO_LINE_NUMBER )
			{
				if( this.sourceFile.hasLine(first) )
				{
					if( map[first] == null )
						map[first] = new ArrayScopeList(this.experiment, this.sourceFile.getName() + ": " + first);
					map[first].addScope(s);
				}
				else
				{
					// TEMPORARY: should tell user that scopetree doesn't match source	
				}
			}
		}
	}

	this.lineMap = map;
}




}








