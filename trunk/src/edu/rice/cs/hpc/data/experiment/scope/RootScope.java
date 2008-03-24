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
	this.id = "RootScope";
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
	/**
	 * Method to retrieve the list of nodes 
	 * @return
	 */
	/*
	public java.util.Hashtable<Integer, ArrayOfNodes> getTableOfNodes() {
		return this.tableNodes;
	}*/
	/**
	 * method to generate the list of nodes
	 * This method should be called when an experiment has been loaded
	 */
	/*
	public void createFlattenNode() {
		this.tableNodes =  new java.util.Hashtable<Integer, ArrayOfNodes>();
		this.createFlattenNode(this.treeNode, 0, " ");
	} */
	
	/**
	 * variable to store the leaves in a given level
	 * The list of the leaves is in form of ArrayOfNodes objects, which is an 
	 *  array list of nodes
	 */
	//private java.util.Hashtable<Integer, ArrayOfNodes> tblLeaves = new java.util.Hashtable<Integer, ArrayOfNodes>();
	//private java.util.ArrayList<Scope.Node[]> tblFlattenNodes;

	
	//=====================================
	private void addChildren(Scope.Node node, ArrayOfNodes arrNodes) {
		int nbChildren = node.getChildCount();
		for(int i=0;i<nbChildren;i++) {
			arrNodes.add((Scope.Node)node.getChildAt(i));
		}
	}

	/**
	 * Return the maximum depth of the tree
	 * @return
	 */
	public int getMaxLevel() {
		// the method getDepth is computed based on 1 as the root, while in
		// our case, we prefer based on 0, so let decrease the value by 1
		return this.getTreeNode().getDepth() - 1;
	}
	
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
	
	/**
	 * Return the list of flattened node
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
	 * recursive private method to walk through the tree to get the list of nodes
	 * @param node
	 * @param iLevel
	 * @param str
	 */
	/*
	private void createFlattenNode(Scope.Node node, int iLevel, String str) {
		if(node != null) {
			Integer objLevel = Integer.valueOf(iLevel);
			ArrayOfNodes listOfNodes ;
			// verify if the list of nodes of this level already exists
			if(this.tableNodes.containsKey(objLevel)) {
				listOfNodes = this.tableNodes.get(objLevel);
			} else 
				listOfNodes = new ArrayOfNodes(iLevel);

			// browse all the node's children
			int nbChildren = node.getChildCount();
			if(nbChildren>0){
				for(int i=0;i<nbChildren;i++) {
					Scope.Node nodeKid = (Scope.Node)node.getChildAt(i);
					this.createFlattenNode(nodeKid, iLevel + 1, str + " ");
				}
			} else {
				ArrayOfNodes listOfLeaves = tblLeaves.get(objLevel);
				// this node is the leaf
				if(listOfLeaves == null) {
					listOfLeaves = new ArrayOfNodes(iLevel);
				}
				listOfLeaves.add(node);	// add this node to the list of leaves
				this.tblLeaves.put(objLevel, listOfLeaves); // put it "back"
			}
			node.iLevel = iLevel;
			// find the maximum level
			if(this.MAX_LEVELS<iLevel) {
				this.MAX_LEVELS = iLevel;
			}
			listOfNodes.add(node);
			this.tableNodes.put(objLevel, listOfNodes);
		}
	}
	*/
}

