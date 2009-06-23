/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 * Class to manage zoom-in and zoom out of a scope
 */
public class ScopeZoom {
	// --------------------------------------------------------------------
	//	ATTRIBUTES
	// --------------------------------------------------------------------
	private ScopeTreeViewer viewer;
	private ScopeViewActionsGUI objActionsGUI;
	
    private java.util.Stack<Scope.Node> stackRootTree;
	private java.util.Stack<Object[]> stackTreeStates;

	// --------------------------------------------------------------------
	//	CONSTRUCTORS
	// --------------------------------------------------------------------
	/**
	 * Constructor to prepare zooms
	 * @param treeViewer
	 * @param objGUI
	 */
	public ScopeZoom ( ScopeTreeViewer treeViewer, ScopeViewActionsGUI objGUI ) {
		this.viewer = treeViewer;
		this.objActionsGUI = objGUI;
		stackRootTree = new java.util.Stack<Scope.Node>();
		stackTreeStates = new java.util.Stack<Object[]>();
	}
	
	// --------------------------------------------------------------------
	//	METHODS
	// --------------------------------------------------------------------
	/**
	 * Zoom in from "old" scope to "new" scope, store the tree description (expanded items) 
	 * if necessary
	 * @param current
	 * @param old
	 */
	public void zoomIn (Scope.Node current, Scope.Node old) {
		// ---------------------- save the current view
		this.stackRootTree.push(old); // save the node for future zoom-out
		Object treeStates[] = viewer.getExpandedElements();
		this.stackTreeStates.push(treeStates);
		// ---------------------- 

		viewer.setInput(current);
		// we need to insert the selected node on the top of the table
		// FIXME: this approach is not elegant, but we don't have any choice
		// 			at the moment
		this.objActionsGUI.insertParentNode(current);
		//this.objActionsGUI.updateButtons( canZoomIn(current), canZoomOut() );
		//this.objActionsGUI.checkZoomButtons(current);
	}
	
	/**
	 * zoom out
	 */
	public void zoomOut () {
		Scope.Node parent; 
		if(this.stackRootTree.size()>0) {
			// the tree has been zoomed
			parent = this.stackRootTree.pop();
		} else {
			// case where the tree hasn't been zoomed
			// FIXME: there must be a bug if the code comes to here !
			parent = (Scope.Node)viewer.getInput();
			throw( new java.lang.RuntimeException("ScopeViewActions - illegal zoomout"+parent));
		}

		viewer.setInput( parent );
		// Bug fix: we need to insert the parent on the top of the table
		this.objActionsGUI.insertParentNode(parent);

		// return the previous expanded tree items
		if(this.stackTreeStates.size()>0) {
			Object o[] = this.stackTreeStates.pop();
			viewer.setExpandedElements(o);
		}
	}
	
	/**
	 * Verify if zoom out is possible
	 * @return
	 */
	public boolean canZoomOut () {
		boolean bRet = (this.stackRootTree != null);
		if (bRet) {
			bRet = ( this.stackRootTree.size()>0 );
		}
		return bRet;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	public boolean canZoomIn ( Scope.Node node ) {
		if (node == null)
			return false;
		return ( node.getChildCount()>0 );
	}
	
	public void setViewer ( ScopeTreeViewer treeViewer ) {
		viewer = treeViewer;
	}
}
