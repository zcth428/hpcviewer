//////////////////////////////////////////////////////////////////////////
//																		//
//	MappingTreeFilter.java												//
//																		//
//	view.filter.MappingTreeFilter -- abstract node-mapping tree filter	//
//	Last edited: October 9, 2001 at 1:06 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.viewer.scope;


import edu.rice.cs.hpc.data.experiment.scope.Scope;			// sigh
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;




//////////////////////////////////////////////////////////////////////////
//	CLASS MAPPING-TREE-FILTER											//
//////////////////////////////////////////////////////////////////////////

/**
 *
 *	A <code>TreeFilter</code> which filters via an explicit mapping between
 *	model tree nodes and filtered tree nodes.
 *
 *	The mapping is stored as an explicit filtered tree structure, lazily
 *	constructed, with explicit pointers from filtered nodes to their model
 *	originals and with hash linking from model nodes to their filtered
 *	counterparts. The filtered tree and cross links are computed based on
 *	two customizable decision-makers. The method <code>computeFilterCode</code>
 *	determines how individual model nodes are to be filtered and is a subclass
 *	responsibility. The object <code>this.comparator</code> determines the
 *	order in which each node's children are sorted; it is supplied to filter
 *	constructors and may be changed later with <code>setComparator</code>. The
 *	comparator may be <code>null</code>, in which case children are not sorted.
 *	<p>
 *	TODO: describe the lazy filtering strategy.
 *
 *	@see edu.rice.cs.hpcview.filter.MappingTreeFilter#computeFilterCode
 *
 */
 
public abstract class MappingTreeFilter
{


/** A comparison operator used to sort the children of each tree node. */
protected Comparator comparator;

/** The root of the filtered tree. */
protected MappingTreeFilter.FNode filteredRoot;

/** A map from model tree nodes to filtered tree nodes. */
protected Map map;




//////////////////////////////////////////////////////////////////////////
//	PRIVATE CONSTANTS													//
//////////////////////////////////////////////////////////////////////////




/** Filter code indicating that a model node should be portrayed normally. */
protected static final int SHOW = 1;

/** Filter code indicating that a model node should be elided completely. */
protected static final int ELIDE = 2;

/** Filter code indicating that a model node's children should be elided. */
protected static final int TRUNCATE = 3;

/** Filter code indicating that a model node should be "flattened" by eliding
    the node but adding its children to the portrayal of its parent. */
protected static final int FLATTEN = 4;

/** Filter code indicating that a model node should be treated as the root
    of the filtered tree. */
protected static final int ROOT = 5;

/** Special object indicating "filtered out" internally, used instead of
    <code>Filter.NO_OBJECT</code> for convenience because it can be assigned
    to variables of class <code>FNode</code>. */
protected static final FNode NO_FNODE = new FNode(null, null, 0, null);




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a mapping tree filter without a client.
 ************************************************************************/
	
public MappingTreeFilter()
{
	super();

	this.comparator = null;
	this.filteredRoot = null;
	this.map = null;
}


/*************************************************************************
 *	Sets the filter's comparator to one which compares two list elements directly.
 *
 *	@param comparator	a <code>Comparator</code> which takes two list elements
 *						as parameters and returns an <code>int</code>
 *						comparison result
 *
 ************************************************************************/
	
public void setComparator(Comparator comparator)
{
	this.comparator = comparator;
}





/*************************************************************************
 *	Maps a filtered tree node to a model tree node.
 *
 *	@param filterNode	the node to be mapped.
 *
 *	@return				the model node which corresponds to
 *						<code>filterNode</code>.
 *
 ************************************************************************/

public Object filterToModel(Object filterNode)
{
	return ((FNode) filterNode).getModelNode();
}




//////////////////////////////////////////////////////////////////////////
//	TREE MODEL METHODS													//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the root node of the (filtered) tree.
 *
 *	This method returns <code>null</code> if the tree is empty.
 *
 ************************************************************************/

public Object getRoot()
{
	return this.filteredRoot;
}




/*************************************************************************
 *	Returns a node's child with a given index.
 ************************************************************************/

public Object getChild(Object parent, int index)
{
	FNode fnParent = (FNode) parent;
	this.ensureChildReady(fnParent);
	return fnParent.getChildAt(index);
}




/*************************************************************************
 *	Returns the number of children a given node has.
 ************************************************************************/

public int getChildCount(Object parent)
{
	FNode fnParent = (FNode) parent;
	this.ensureChildReady(fnParent);
	return fnParent.getChildCount();
}




/*************************************************************************
 *	Returns whether a given node is a leaf.
 ************************************************************************/

public boolean isLeaf(Object node)
{
	FNode fnNode = (FNode) node;
	this.ensureChildReady(fnNode);
	return fnNode.isLeaf();
}




/*************************************************************************
 *	Returns the index of child in parent.
 ************************************************************************/

public int getIndexOfChild(Object parent, Object child)
{
	FNode fnParent = (FNode) parent;
	FNode fnChild  = (FNode) child;
	this.ensureChildReady(fnParent);
	return fnParent.getIndex(fnChild);
}






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

public abstract int computeFilterCode(Scope.Node modelNode);


/*************************************************************************
 *	Returns <code>this.map.get(modelNode)</code> cast to a <code>FNode</code>.
 ************************************************************************/

public FNode mapGet(Scope.Node modelNode)
{
	return (FNode) this.map.get(modelNode);
}




/*************************************************************************
 *	Ensures that a model node's filtered counterpart has been computed.
 *
 *	It turns out to be cleaner to allow calling this method with a null
 *	argument and to say that the filtered counterpart of <code>null</code>
 *	is <code>null</code>.
 *
 *	@param modelNode	a node in the tree model or <code>null</code>
 *
 *	@return				the filtered node corresponding to the model node,
 *						or <code>Filter.NO_OBJECT</code> if the model node
 *							is filtered out,
 *						or <code>null</code> if it is <code>null</code>.
 *
 ************************************************************************/

public FNode getFilteredNode(Scope.Node modelNode)
{	
	FNode filterNode;
	
	// special case for null => null
	if( modelNode == null )
		filterNode = null;
	else
	{
		// check whether we already know the answer
		filterNode = this.mapGet(modelNode);
		if( filterNode == null )
		{
			// explicitly filtered-out node doesn't need ancestor check
			int code = this.computeFilterCode(modelNode);
			if( code == ELIDE || code == FLATTEN )
				filterNode = NO_FNODE;
			else
			{				
				// get filtered parent of the model node
				FNode filterParent;
				if( code == ROOT )
					filterParent = null;
				else
				{
					// find effective model parent with flattening taken into account
					Scope.Node modelParent = (Scope.Node) modelNode.getParent();
					while( modelParent != null && this.computeFilterCode(modelParent) == FLATTEN )
						modelParent = (Scope.Node) modelParent.getParent();
						
					// recursively get filtered parent from it
					filterParent = this.getFilteredNode(modelParent);
				}
				
				// check whether model node is implicitly filtered out
				if( filterParent == null )
					filterNode = new FNode(this, modelNode, code, filterParent);	// modelNode is root
				else if( filterParent == NO_FNODE )
					filterNode = NO_FNODE;
				else if( filterParent.getCode() == TRUNCATE )
					filterNode = NO_FNODE;
				else
					filterNode = new FNode(this, modelNode, code, filterParent);
			}

			// cache the answer for later
			this.map.put(modelNode, filterNode);
		}
	}
		
	return filterNode;
}




/*************************************************************************
 *	Ensures that a model node's filtered child nodes have been computed.
 ************************************************************************/

public void ensureChildReady(FNode filterNode)
{
	if( ! filterNode.isChildReady() )
	{
		int code = filterNode.getCode();
		if( code == TRUNCATE )
			/* done, we want no filtered children */ ;
		else
		{
			List filterChildren = new ArrayList();

			// gather a list of filtered children
			Scope.Node modelNode = filterNode.getModelNode();
			this.getFilteredChildren(modelNode, filterChildren);

			// sort the children list
			if( this.comparator != null )
				Collections.sort(filterChildren, this.comparator);

			// add sorted children to the filtered node
			Iterator iter = filterChildren.iterator();
			while( iter.hasNext() )
			{
				// this is tricky: 'filterChild' may be a preexisting node created while
				// doing a get-filtered-parent chain, and if so 'filterChild' will already
				// have its parent set to 'filterNode'. But then 'add' will try to remove
				// 'filterChild' first, which won't work because 'filterChild' isn't in
				// 'filterNode's children vector. Setting 'filterChild's parent field to
				// null is always the right thing to do, whether or not it was a
				// preexisting node.

				FNode filterChild = (FNode) iter.next();
				filterChild.setParent(null);
				filterNode.add(filterChild);
			}
		}

		filterNode.setChildReady(true);
	}
}




/*************************************************************************
 *	Computes the sequence of filtered children for a given model node,
 *	taking flattening into account.
 *
 *	The children are appended, in order, to a given list. Passing the same
 *	list along during recursion is an easy optimization to avoid a lot of
 *	list concatenation operations.
 *
 ************************************************************************/

public void getFilteredChildren(Scope.Node modelNode, List filterChildren)
{	
	int count = modelNode.getChildCount();
	for( int k = 0;  k < count;  k++ )
	{
		Scope.Node modelChild = (Scope.Node) modelNode.getChildAt(k);
		int code = this.computeFilterCode(modelChild);
		if( code == FLATTEN )
			this.getFilteredChildren(modelChild, filterChildren);
		else
		{
			FNode filterChild = this.getFilteredNode(modelChild);
			if( filterChild != NO_FNODE )
				filterChildren.add(filterChild);
		}
	}
}




//////////////////////////////////////////////////////////////////////////
//	INNER CLASS FILTERED-NODE											//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Class of nodes used to represent the filtered tree.
 *
 *	<code>FNode</code> instances also cache the results from calling
 *	<code>computeFilterCode</code> on their corresponding model nodes. And to
 *	support lazy construction of the filtered tree, a filtered node records
 *	whether or not its parent and children have been computed.
 *
 *	@see edu.rice.cs.hpcview.filter.MappingTreeFilter.FNode#getFilterCode
 *	@see edu.rice.cs.hpcview.filter.MappingTreeFilter.FNode#isChildReady
 *
 ************************************************************************/

	public static class FNode extends Scope.Node
	{
		private static final long serialVersionUID = 7L;
		
		/** The model node corresponding to this filtered node. */
		protected MappingTreeFilter filter;

		/** The model node corresponding to this filtered node. */
		protected Scope.Node modelNode;
		
		/** The filter code for the node's corresponding model node. */
		protected int filterCode;
		
		/** Whether the filtered node's children have been computed. */
		protected boolean childReady;


		/** Constructs a new filtered node. */
		FNode(MappingTreeFilter filter, Scope.Node modelNode, int filterCode,
                     FNode filterParent)
		{
			super();
			
			// creation arguments
			this.filter     = filter;
			this.modelNode  = modelNode;
			this.filterCode = filterCode;
			this.setParent(filterParent);
			
			// lazy tree-building status
			this.childReady = false;
		};
		
		public String toString()
		{
			return this.getScope().getName();
		}


		/** Returns the model node corresponding to the filtered node. */
		Scope.Node getModelNode()
		{
			return this.modelNode;
		};
		
		
		/** Overrides superclass to return the model node's user object. */
		public Object getUserObject()
		{
			return this.modelNode.getUserObject();
		};
		
		
		/** Sets the filter code for the node's corresponding model node. */
		void setCode(int filterCode)
		{
			this.filterCode = filterCode;
		};
		
		
		/** Returns the filter code for the node's corresponding model node. */
		int getCode()
		{
			return this.filterCode;
		};
		
		
		/** Sets whether the filtered node's children have been computed. */
		void setChildReady(boolean ready)
		{
			this.childReady = ready;
		};
		
		
		/** Returns whether the filtered node's children have been computed. */
		boolean isChildReady()
		{
			return this.childReady;
		};
				
	};




}	// end class MappingTreeFilter









