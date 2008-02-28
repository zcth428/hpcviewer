package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ScopeViewActions {
	private ScopeViewActionsGUI objActionsGUI;

    private TreeViewer 	treeViewer;		  	// tree for the caller and callees
    private Scope 		myRootScope;		// the root scope of this view
    //-------------- FLAT DATA
    
    /**
     * Constructor: create actions and the GUI (which is a coolbar)
     * @param shell
     * @param parent
     * @param tree
     * @param font
     */
    public ScopeViewActions(IViewSite viewSite, Composite parent, Font font) {
    	this.objActionsGUI = new ScopeViewActionsGUI(viewSite, parent, font, this);
    }

    
	public void updateContent(Experiment exp, Scope scope, TreeViewerColumn []columns) {
    	this.myRootScope = scope;
    	this.objActionsGUI.updateContent(exp, scope, columns);
    }
	/**
	 * Zoom-in the children
	 */
	public void zoomIn() {
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof StructuredSelection))
			return;
		Object o = ((StructuredSelection)sel).getFirstElement();
		if (!(o instanceof Scope.Node)) {
			System.err.println("ScopeView - zoomin:"+o.getClass());
			return;
		}
		Scope.Node current = (Scope.Node) o;
		Scope.Node parent = (Scope.Node) current.getParent();
		treeViewer.setInput(current);
		this.objActionsGUI.insertParentNode(current);
		this.objActionsGUI.updateFlattenView(current.iLevel);
	}
	
	/**
	 * Zoom-out the node
	 */
	public void zoomOut() {
		Object o = treeViewer.getInput();
		Scope.Node child;
		if (!(o instanceof Scope.Node)) {
			if(o instanceof ArrayOfNodes) {
				TreeItem []tiObjects = this.treeViewer.getTree().getItems();
				child = (Scope.Node)tiObjects[1].getData(); //the 0th item is the aggregate metric
				// tricky solution when zoom-out the flattened node
				if(child != null)
					child = (Scope.Node)child.getParent();
			} else {
				System.err.println("ScopeView - zoomout:"+o.getClass());
				return;
			}
		} else
			child = (Scope.Node) o;
		Scope.Node parent = (Scope.Node)child.getParent();
		if (parent == null)
			return;
		// do not zoom out to the root
		/*
		if(parent.getScope() instanceof RootScope)
			return;
		*/
		treeViewer.setInput( parent );
		this.objActionsGUI.updateFlattenView(parent.iLevel);
	}

	/**
	 * Go deeper one level
	 */
	public void flattenNode() {
		int iNewFlatLevel = this.objActionsGUI.iFlatLevel + 1;
		Integer objLevel = Integer.valueOf(iNewFlatLevel);
		// get the next level
		ArrayOfNodes nodeArray = ((RootScope)this.myRootScope).getTableOfNodes().get(objLevel);
		if(nodeArray != null) {
			this.treeViewer.setInput(nodeArray);
			this.objActionsGUI.updateFlattenView(iNewFlatLevel, true);
		} else {
			// there is something wrong. we return to the original node
			System.err.println("ScopeView-flatten: error cannot flatten further");
		}
	}
	
	/**
	 * go back one level
	 */
	public void unflattenNode() {
		if(this.objActionsGUI.iFlatLevel <3) return;
		
		int iNewFlatLevel = this.objActionsGUI.iFlatLevel - 1;
		Integer objLevel = Integer.valueOf(iNewFlatLevel);
		ArrayOfNodes nodeArray = ((RootScope)this.myRootScope).getTableOfNodes().get(objLevel);
		if(nodeArray != null) {
			this.treeViewer.setInput(nodeArray);
			this.objActionsGUI.updateFlattenView(iNewFlatLevel, true);
		}
	}
	
    public boolean shouldZoomInBeEnabled(Scope.Node node) {
    	return (this.objActionsGUI.shouldZoomInBeEnabled(node));
    }
    
    public boolean shouldZoomOutBeEnabled(Scope.Node node) {
    	return (this.objActionsGUI.shouldZoomOutBeEnabled(node));
    }
    
    public void checkButtons(Scope.Node node) {
    	this.objActionsGUI.checkFlattenButtons();
    	this.objActionsGUI.checkZoomButtons(node);
    }

    public void setTreeViewer(TreeViewer tree) {
    	this.treeViewer = tree;
    	this.objActionsGUI.setTreeViewer(tree);
    }
}
