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


import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;




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

protected int iFlattenLevel = -1;
//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a RootScope.
 ************************************************************************/
	
public RootScope(Experiment experiment, String prog, String name, RootScopeType rst)
{
	super(experiment);	
	this.programName = prog;
	this.rootScopeName = name;
//	this.id = "RootScope";
	this.rootScopeType = rst;
	
}


public Scope duplicate() {
    return new RootScope(null, this.programName, this.rootScopeName, this.rootScopeType);
}

public int hashCode() {
	return this.rootScopeName.hashCode();
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

public void accept(ScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

//////////////////////-----------------------------------------
//Support for flattening the kids
//////////////////////-----------------------------------------

	/**
	 * Table of list of flattened node. We need to keep it in memory to avoid
	 * recomputation of the flattening nodes
	 */
	private java.util.Hashtable<Integer, ArrayOfNodes> tableNodes;
	
	//=====================================
	private void addChildren(Scope.Node node, ArrayOfNodes arrNodes) {
		int nbChildren = node.getChildCount();
		for(int i=0;i<nbChildren;i++) {
			// Laksono 2009.03.04: do not add call site !
			Scope.Node nodeKid = ((Scope.Node) node.getChildAt(i));
			if (nodeKid.getScope() instanceof CallSiteScope) {
				// the kid is a callsite: do nothing
			} else {
				// otherwise add the kid into the list of scopes to display
				arrNodes.add(nodeKid);
			}
		}
	}

	/**
	 * Return the list of flattened node.
	 * Algo: 
	 *  - browse the tree. If the tree node has children, then add the children into the table
	 *    Otherwise, add the node itself.
	 * @param iLevel: level of flattened nodes, 0 is the root
	 * @return
	 */
	private ArrayOfNodes getFlatten(int iLevel) {
		if (iLevel<0)
			return null; // TODO: should return an exception instead
		ArrayOfNodes arrNodes;
		Integer objLevel = Integer.valueOf(iLevel);
		if(iLevel == 0) {
			if(this.tableNodes == null) {
				this.tableNodes = new java.util.Hashtable<Integer, ArrayOfNodes>();
				arrNodes = new ArrayOfNodes(iLevel);
				this.addChildren(this.getTreeNode(), arrNodes);
				this.tableNodes.put(objLevel, arrNodes);
			} else {
				arrNodes = this.tableNodes.get(objLevel);
			}
		}  else  {
			// check if the flattened node already exist in our database
			if(this.tableNodes.containsKey(objLevel)) {
				arrNodes = this.tableNodes.get(objLevel);
 			} else {
 				// create the list of flattened node
 				ArrayOfNodes arrParentNodes = this.tableNodes.get(Integer.valueOf(iLevel - 1));
 				arrNodes = new ArrayOfNodes(iLevel);
 				boolean hasKids = false;
 				for (int i=0;i<arrParentNodes.size();i++) {
 					Scope.Node node = arrParentNodes.get(i);
 					if(node.getChildCount()>0) {
 						// this node has children, add the children
 						this.addChildren(node, arrNodes);
 						hasKids = true;
 					} else {
 						// no children: add the node itself !
 						arrNodes.add(node);
 					}
 				}
 				if(hasKids)
 					this.tableNodes.put(objLevel, arrNodes);
 				else {
 					// no more kids !
 					return null;
 				}
 			}
		}
		this.iFlattenLevel = iLevel;
		return arrNodes;
	}
	//=====================================
	/**
	 * Return the maximum depth of the tree
	 * @return
	 */
	/*
	public int getMaxLevel() {
		// the method getDepth is computed based on 1 as the root, while in
		// our case, we prefer based on 0, so let decrease the value by 1
		return this.getTreeNode().getDepth() - 1;
	} */
	
	/**
	 * Return the list of flattened nodes
	 * We will flatten the current node by one level
	 * @return
	 */
	public ArrayOfNodes getFlatten() {
		if(this.iFlattenLevel<0)
			this.getFlatten(0);
		return this.getFlatten(this.iFlattenLevel + 1);
	}
	
	/**
	 * Return the unflattened list of nodes
	 * The tree has to be flattened, otherwise it return null
	 * @return
	 */
	public ArrayOfNodes getUnflatten() {
		return this.getFlatten(this.iFlattenLevel - 1);
	}
	
	/**
	 * Return the current level of flatten node
	 * @return
	 */
	public int getFlattenLevel() {
		return this.iFlattenLevel;
	}
	
}

