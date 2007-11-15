//////////////////////////////////////////////////////////////////////////
//									//
//	ScopeTreeFilter.java						//
//									//
//	view.scope.ScopeTreeFilter -- tree filter for scope view	//
//	Last edited: October 11, 2001 at 10:06 pm			//
//									//
//	(c) Copyright 2001 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.viewer.scope;


import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.scope.MappingTreeFilter;

//////////////////////////////////////////////////////////////////////////
//	CLASS SCOPE-FILTER						//
//////////////////////////////////////////////////////////////////////////

/**
 *
 *	A tree filter for scope views.
 *
 */
 
public class ScopeTreeFilter 
{


/** The experiment whose metrics are being viewed. */
protected Experiment experiment;


/** The root of the model subtree being displayed.
    (Nodes outside this subtree are elided.) */
protected Scope.Node zoomModelNode;



/*************************************************************************
 *	Creates a scope tree filter without a client.
 ************************************************************************/
	
public ScopeTreeFilter(Experiment experiment)
{
	super();
	this.experiment = experiment;
	
	this.zoomModelNode = null;
	
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO FILTER													//
//////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////
//	FILTER DECISION FUNCTIONS											//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Computes the filter code for a given model tree node.
 *
 *	The filter code determines how the model node is portrayed in the
 *	filtered tree.
 *
 *	@return		one of the following constants:
 *					<code>SHOW</code>,
 *					<code>ELIDE</code>,
 *					<code>TRUNCATE</code>, or
 *					<code>FLATTEN</code>
 *
 ************************************************************************/

public int computeFilterCode(Scope.Node modelNode)
{
	Scope scope = modelNode.getScope();
	int code;
	
	//------------------------------------------------
	// zoom in & out operators
	//------------------------------------------------
	if( modelNode == this.zoomModelNode )
		code = MappingTreeFilter.ROOT;
	
	else if( (this.zoomModelNode != null) && (! modelNode.isNodeAncestor(this.zoomModelNode)) )
		code = MappingTreeFilter.ELIDE;
	
	//------------------------------------------------
	// hpcviewer flatten & unflatten operators
	//------------------------------------------------
	else if( modelNode.getFlattenThis() )
		code = MappingTreeFilter.FLATTEN;

	//------------------------------------------------
	// scope type filtering
	//------------------------------------------------
	else if( scope instanceof RootScope )
		code = MappingTreeFilter.SHOW;
	
	else if( scope instanceof LoadModuleScope )
		code = MappingTreeFilter.SHOW ;

	else if( scope instanceof GroupScope )
		   code = (1==1 ?
					   MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);
					   
	else if( scope instanceof FileScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);

	else if( scope instanceof ProcedureScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);

	else if( scope instanceof AlienScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);
	
	else if( scope instanceof CallSiteScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);

	else if( scope instanceof LoopScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);

	else if( scope instanceof StatementRangeScope )
		code = (1==1
					? MappingTreeFilter.SHOW : MappingTreeFilter.FLATTEN);

	else if( scope instanceof LineScope )
		code = (1==1 ?
					MappingTreeFilter.SHOW : MappingTreeFilter.ELIDE);

	
	else
	{
		code = MappingTreeFilter.SHOW;	// for compiler
	}

	return code;
}



//////////////////////////////////////////////////////////////////////////
// SCOPE TREE FLATTENING												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Flattens the display of a scope subtree by some number of levels.
 *
 *	TODO: explain exactly what this means. Scope tree
 *	nodes have a persistent "flatten count". 
 *
 ************************************************************************/
	
public void flattenView(Scope.Node node, int delta)
{
	this.flattenTo(node, node.getFlattenCount() + delta);
}




/*************************************************************************
 *	Unflattens the display of a scope subtree by some number of levels.
 *
 *	TODO: explain exactly what this means. Scope tree
 *	nodes have a persistent "flatten count". 
 *
 ************************************************************************/
	
public void unflattenView(Scope.Node node, int delta)
{
	this.flattenTo(node, node.getFlattenCount() - delta);
}




/*************************************************************************
 *	Flattens the display of a scope subtree to a given number of levels.
 *
 *	TODO: explain exactly what this means. Scope tree
 *	nodes have a persistent "flatten count". 
 *
 ************************************************************************/
	
public void flattenTo(Scope.Node node, int flattenCount)
{
	node.setFlattenCount(flattenCount);
	this.ensureFlattenThis(node);
}




/*************************************************************************
 *	Returns whether a given node can be unflattened by one level.
 *
 *	TODO: explain exactly what this means. Scope tree
 *	nodes have a persistent "flatten count". 
 *
 ************************************************************************/
	
public boolean canUnflatten(Scope.Node node)
{
	return (node.getFlattenCount() > 0);
}




/*************************************************************************
 *	Ensures that all nodes in the subtree rooted at <code>node</code> have
 *	correct <code>flattenThis</code> values.
 *
 *	Here "correct" means "consistent with the flatten count of <code>node</code>".
 *
 ************************************************************************/
	
protected void ensureFlattenThis(Scope.Node modelNode)
{
	// won't work to flatten from above the zoomed-in subtree
	if(this.zoomModelNode == null || this.zoomModelNode.isNodeAncestor(modelNode))
           System.err.println("ScopeTreeFilter::ensureFlattenThis");

	modelNode.setFlattenThis(false);
	
	int flattenCount = modelNode.getFlattenCount();
	this.ensureChildrenFlattenThis(modelNode, flattenCount);
}




/*************************************************************************
 *	Recursive helper function for <code>ensureFlattenThis(node)</code>.
 *
 *	The recursion invariant is that <code>modelNode</code> has the correct
 *	value for <code>flattenThis</code> and that <code>flattenCount > 0</code>.
 *
 ************************************************************************/
	
protected void ensureChildrenFlattenThis(Scope.Node modelNode, int flattenCount)
{
//	Dialogs.Assert( flattenCount > 0 );

	// ensure each child's subtree in turn
	int childCount = modelNode.getChildCount();
	for( int k = 0;  k < childCount;  k++ )
	{
		Scope.Node modelChild = (Scope.Node) modelNode.getChildAt(k);

		// Only apparently-interior nodes may be flattened by this operator:
		// a model node which is interior, but which is currently displayed as a leaf
		// due to filtering, should not be "flattened away" by this operator.
		
	
		if( modelChild.isLeaf() )
			modelChild.setFlattenThis(false);
		else
		{
			// compute child's filter code assuming no flattening
			modelChild.setFlattenThis(false);
			int code = this.computeFilterCode(modelChild);

			// children of elided or truncated nodes won't show, so need not be ensured
			if( code == MappingTreeFilter.ELIDE || code == MappingTreeFilter.TRUNCATE )
				modelChild.setFlattenThis(false);	// doesn't matter but set here for clarity

			// nodes flattened for some other reason don't count as a flattening level
			else if( code == MappingTreeFilter.FLATTEN )
			{
				modelChild.setFlattenThis(false);	// doesn't matter but set here for clarity
				this.ensureChildrenFlattenThis(modelChild, flattenCount);
			}
			else
			{
				// child is SHOW so should be flattened since flattenCount > 0
				// (child cannot properly be ROOT)
				modelChild.setFlattenThis(flattenCount > 0);

				// recursively ensure subchildren
				this.ensureChildrenFlattenThis(modelChild, flattenCount - 1);
			}
		}
	}
}




}	// end class ScopeTreeFilter







