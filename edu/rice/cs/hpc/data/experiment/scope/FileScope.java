//////////////////////////////////////////////////////////////////////////
//																		//
//	FileScope.java														//
//																		//
//	experiment.scope.FileScope -- a source file in an experiment		//
//	Last edited: August 10, 2001 at 3:20 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.util.*;




//////////////////////////////////////////////////////////////////////////
//	CLASS FILE-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A file scope in an HPCView experiment.
 *
 */


public class FileScope extends Scope
{


/** The mapping from the file's line numbers to subscopes. */
protected ScopeList[] lineMap;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a FileScope.
 *
 *	The <code>lineMap</code> instance variable is not initialized here
 *	because it is computed on demand.
 *
 ************************************************************************/
	
public FileScope(Experiment experiment, SourceFile sourceFile)
{
	super(experiment, sourceFile);
	this.id = "FileScope";
}

public Scope duplicate() {
    return new FileScope(this.experiment, this.sourceFile);
}

public int hashCode() {
	return this.sourceFile.getName().hashCode();
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this file scope.
 ************************************************************************/
	
public String getName()
{
	return this.getSourceCitation();
}




/*************************************************************************
 *	Returns the tool tip for this scope.
 ************************************************************************/
	
public String getToolTip()
{
	boolean available = this.sourceFile.isAvailable();
	return (available ? Strings.SOURCE_FILE_AVAILABLE : Strings.SOURCE_FILE_UNAVAILABLE);
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO SCOPE														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns a list indicating which subscopes start on each line of the
 *	source file.
 ************************************************************************/
	
public ScopeList[] getLineMap()
{
	if( this.lineMap == null )
		this.computeLineMap();

	return this.lineMap;
}




/*************************************************************************
 *	Computes and caches a list indicating which subscopes start on each
 *	line of the source file.
 ************************************************************************/
	
protected void computeLineMap()
{
	int lineCount = this.sourceFile.getLineCount();
	ScopeList[] map = new ScopeList[lineCount];

	int subscopeCount = this.getSubscopeCount();
	for( int k = 0;  k < subscopeCount;  k++ )
	{
		Scope s   = this.getSubscope(k);
		int first = s.getFirstLineNumber();
		if( map[first] == null )
			map[first] = new ArrayScopeList(this.experiment, this.getName() + ": " + first);
		map[first].addScope(s);
	}

	this.lineMap = map;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

}
