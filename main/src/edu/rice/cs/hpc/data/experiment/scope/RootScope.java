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

	private java.util.Hashtable<Integer, ArrayOfNodes> tableNodes;
	
	/**
	 * Method to retrieve the list of nodes 
	 * @return
	 */
	public java.util.Hashtable<Integer, ArrayOfNodes> getTableOfNodes() {
		return this.tableNodes;
	}
	/**
	 * method to generate the list of nodes
	 * This method should be called when an experiment has been loaded
	 */
	public void createFlattenNode() {
		this.tableNodes =  new java.util.Hashtable<Integer, ArrayOfNodes>();
		this.createFlattenNode(this.treeNode, 0, " ");
	}
	
	/**
	 * recursive private method to walk through the tree to get the list of nodes
	 * @param node
	 * @param iLevel
	 * @param str
	 */
	private void createFlattenNode(Scope.Node node, int iLevel, String str) {
		if(node != null) {
			Integer objLevel = Integer.valueOf(iLevel);
			ArrayOfNodes listOfNodes ;
			if(this.tableNodes.containsKey(objLevel)) {
				listOfNodes = this.tableNodes.get(objLevel);
			} else 
				listOfNodes = new ArrayOfNodes(iLevel);
			//System.err.println(str+"FTNS "+ iLevel + " " + node.getScope().getShortName()+" :" +node.getChildCount());
			for(int i=0;i<node.getChildCount();i++) {
				Scope.Node nodeKid = (Scope.Node)node.getChildAt(i);
				this.createFlattenNode(nodeKid, iLevel + 1, str + " ");
			}
			node.iLevel = iLevel;
			listOfNodes.add(node);
			this.tableNodes.put(objLevel, listOfNodes);
		}
	}
	
	/**
	 * Debugger for printing the list of nodes
	 */
	public void printFlattenNodes(){
		if(this.tableNodes != null) {
			for(int i=0;i<this.tableNodes.size();i++) {
				ArrayOfNodes listOfNodes = this.tableNodes.get(Integer.valueOf(i));
				for(int j=0;j<listOfNodes.size();j++) {
					Scope.Node node = listOfNodes.get(j);
					System.out.println("nodes:"+node.getScope().getShortName()+" -> " + node.getChildCount());
				}
			}
		}
	}

}

