/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.widgets.CoolBar;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public class FlatScopeViewActions extends ScopeViewActions {
	private int iFlattenLevel = -1;
	
	/**
	 * Table of list of flattened node. We need to keep it in memory to avoid
	 * recomputation of the flattening nodes
	 */
	private java.util.Hashtable<Integer, Scope> tableNodes;

	private java.util.Stack<Object[]> stackStates;
	
	
	//-----------------------------------------------------------------------
	// 					METHODS
	//-----------------------------------------------------------------------

	/**
	 * @param viewSite
	 * @param parent
	 */
	public FlatScopeViewActions(Shell shell, IWorkbenchWindow window, Composite parent, CoolBar coolbar) {
		super(shell, window, parent, coolbar);
	}

	/**
	 * Create your own specific GUI
	 * @param parent
	 * @return
	 */
	protected Composite createGUI(Composite parent, CoolBar coolbar) {
		this.objActionsGUI = new FlatScopeViewActionsGUI(this.objShell, this.objWindow, parent, this);
		this.objActionsGUI.buildGUI(parent, coolbar);
		return parent;
	}

	//-----------------------------------------------------------------------
	// 					FLATTEN: PUBLIC INTERFACES
	//-----------------------------------------------------------------------
	

	/**
	 * Return the current level of flatten node
	 * @return
	 */
	public int getFlattenLevel() {
		return this.iFlattenLevel;
	}


	/**
	 * Flatten the tree one level more
	 */
	public void flatten() {
		this.pushElementStates();
		
		Scope objFlattenedNode;
		
		if(this.iFlattenLevel<0)
			objFlattenedNode = this.getFlatten(0);
		objFlattenedNode = this.getFlatten(this.iFlattenLevel + 1);
		
		if(objFlattenedNode != null) {
			this.treeViewer.getTree().setRedraw(false);
			// we update the data of the table
			this.treeViewer.setInput(objFlattenedNode);
			// refreshing the table to take into account a new data
			this.treeViewer.refresh();
			// post processing: inserting the "aggregate metric" into the top row of the table
			((FlatScopeViewActionsGUI) this.objActionsGUI).updateFlattenView(getFlattenLevel(), true);

			// bug fix: attempt to expand elements 
			Object []arrNodes = this.stackStates.peek();
			this.treeViewer.setExpandedElements(arrNodes);

			this.treeViewer.getTree().setRedraw(true);
			
		} else {
			// either there is something wrong or we cannot flatten anymore
			System.err.println("hpcviewer: cannot flatten the tree");
		}
	}

	/**
	 * Unflatten flattened tree (tree has to be flattened before)
	 */
	public void unflatten() {
		Scope objParentNode = this.getFlatten(this.iFlattenLevel - 1);
		if(objParentNode != null) {
			this.treeViewer.setInput(objParentNode);
			((FlatScopeViewActionsGUI) this.objActionsGUI).updateFlattenView(getFlattenLevel(), true);
			this.popElementStates();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.IToolbarManager#checkStates(edu.rice.cs.hpc.data.experiment.scope.Scope.Node)
	 */
	public void checkStates(Scope nodeSelected) {
		boolean bCanZoomIn = objZoom.canZoomIn(nodeSelected);
		objActionsGUI.enableHotCallPath( bCanZoomIn );
		if (bCanZoomIn) {
			bCanZoomIn = !( (FlatScopeViewActionsGUI) objActionsGUI ).shouldUnflattenBeEnabled();
		}
		objActionsGUI.enableZoomIn( bCanZoomIn );
		objActionsGUI.enableZoomOut( objZoom.canZoomOut() );
		((FlatScopeViewActionsGUI) objActionsGUI).checkFlattenButtons();
	}

	
	//-----------------------------------------------------------------------
	// 					FLATTEN: PRIVATE METHODS
	//-----------------------------------------------------------------------

	/**
	 * store the current expanded elements into a stack
	 */
	private void pushElementStates() {
		Object []arrNodes = this.treeViewer.getExpandedElements();
		if (stackStates == null)
			stackStates = new java.util.Stack<Object[]>();

		stackStates.push(arrNodes);
	}
	
	/**
	 * recover the latest expanded element into stack
	 */
	private void popElementStates() {
		Object []arrNodes = stackStates.pop();
		this.treeViewer.setExpandedElements(arrNodes);
	}
	
	/**
	 * Return the list of flattened node.
	 * Algo: 
	 *  - browse the tree. If the tree node has children, then add the children into the table
	 *    Otherwise, add the node itself.
	 * @param iLevel: level of flattened nodes, 0 is the root
	 * @return
	 */
	private Scope getFlatten(int iLevel) {
		if (iLevel<0)
			return null; // TODO: should return an exception instead
		Scope objFlattenedNode;
		Integer objLevel = Integer.valueOf(iLevel);
		if(iLevel == 0) {
			if(this.tableNodes == null) {
				this.tableNodes = new java.util.Hashtable<Integer, Scope>();
				objFlattenedNode = this.myRootScope;
				this.tableNodes.put(objLevel, objFlattenedNode);
				
			} else {
				objFlattenedNode = this.tableNodes.get(objLevel);
			}
		}  else  {
			// check if the flattened node already exist in our database
			if(this.tableNodes.containsKey(objLevel)) {
				objFlattenedNode = this.tableNodes.get(objLevel);
			} else {
				// create the list of flattened node
				Scope objParentNode = this.tableNodes.get(Integer.valueOf(iLevel - 1));
				objFlattenedNode = (objParentNode.duplicate());
				boolean hasKids = false;
				for (int i=0;i<objParentNode.getChildCount();i++) {
					Scope node =  (Scope) objParentNode.getChildAt(i);
					if(node.getChildCount()>0) {
						// this node has children, add the children
						this.addChildren(node, objFlattenedNode);
						hasKids = true;
					} else {
						// no children: add the node itself !
						objFlattenedNode.add(node);
					}
				}
				if(hasKids)
					this.tableNodes.put(objLevel, objFlattenedNode);
				else {
					// no more kids !
					return null;
				}
			}
		}

		this.iFlattenLevel = iLevel;
		return objFlattenedNode;
	}


	private void addChildren(Scope node, Scope arrNodes) {
		int nbChildren = node.getChildCount();
		for(int i=0;i<nbChildren;i++) {
			// Laksono 2009.03.04: do not add call site !
			Scope nodeKid = ((Scope) node.getChildAt(i));
			if (nodeKid instanceof CallSiteScope) {
				// the kid is a callsite: do nothing
			} else {
				// otherwise add the kid into the list of scopes to display
				arrNodes.add(nodeKid);
			}
		}
	}


}
