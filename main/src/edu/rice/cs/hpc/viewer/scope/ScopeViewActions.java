package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.Metric;

public class ScopeViewActions {
	// public preference
	static public double fTHRESHOLD = 0.6; 
    //-------------- DATA
	private ScopeViewActionsGUI objActionsGUI;
    private ScopeTreeViewer 	treeViewer;		  	// tree for the caller and callees
    private Scope 		myRootScope;		// the root scope of this view
    
    /**
     * Constructor: create actions and the GUI (which is a coolbar)
     * @param shell
     * @param parent
     * @param tree
     * @param font
     */
    public ScopeViewActions(IViewSite viewSite, Composite parent) {
    	this.objActionsGUI = new ScopeViewActionsGUI(viewSite, parent, this);
    }

    /**
     * The tree has been updated or has new content. This object needs to refresh
     * the data and variable initialization too.
     * @param exp
     * @param scope
     * @param columns
     */
	public void updateContent(Experiment exp, Scope scope, TreeViewerColumn []columns) {
    	this.myRootScope = scope;
    	this.objActionsGUI.updateContent(exp, scope, columns);
    }
	
	/**
	 * find the hot call path
	 * @param rootPath
	 * @param item
	 * @param scope
	 * @param metric
	 * @param iLevel
	 * @return
	 */
	private TreePath getHotCallpath(TreePath rootPath, TreeItem item, Scope scope, Metric metric, int iLevel) {
		if(scope == null || metric == null || item == null)
			return null;
		// expand the immediate child
		this.treeViewer.expandToLevel(this.treeViewer.getTreePath(item), 1);
		int iCounts = item.getItemCount();
		// depth first search
		for(int i=0;i<iCounts;i++) {
			TreeItem child = item.getItem(i);
			Object o = child.getData();
			if(o instanceof Scope.Node) {
				Scope.Node nodeChild = (Scope.Node) o;
				Scope scopeChild = nodeChild.getScope();
				double dParent = scope.getMetricPercentValue(metric);
				double dChild = scopeChild.getMetricPercentValue(metric);

				if(dChild<(ScopeViewActions.fTHRESHOLD*dParent)) {
					// we found the hot call path
					return this.treeViewer.getTreePath(child); 
				} else {
					// let see the next kid
					return this.getHotCallpath(rootPath, child, scopeChild, metric, iLevel+ 1);
				}
			}
		}
		// if we reach at this statement, then there is no hot call path !
		return null;
	}
	/**
	 * show the hot call path from the selected node in the tree
	 */
	public void showHotCallpath() {
		// find the selected node
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection))
			return;
		TreeSelection objSel = (TreeSelection) sel;
		// get the node
		Object o = objSel.getFirstElement();
		if (!(o instanceof Scope.Node)) {
			return;
		}
		Scope.Node current = (Scope.Node) o;
		// get the item
		TreeItem item = this.treeViewer.getTree().getSelection()[0];
		// get the path
		TreePath []arrPath = objSel.getPaths();
		// get the selected metric
		TreeColumn colSelected = this.treeViewer.getTree().getSortColumn();
		// get the metric data
		Object data = colSelected.getData();
		if(data instanceof Metric && item != null) {
			Metric metric = (Metric) data;
			// find the hot call path
			int iLevel = 0;
			TreePath pathHot = this.getHotCallpath(arrPath[0], item, current.getScope(), metric, iLevel);
			if(pathHot != null) {
				this.treeViewer.setSelection(new TreeSelection(pathHot));
			}
		}
	}
	/**
	 * Zoom-in the children
	 */
	public void zoomIn() {
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection))
			return;
		Object o = ((TreeSelection)sel).getFirstElement();
		if (!(o instanceof Scope.Node)) {
			return;
		}
		Scope.Node current = (Scope.Node) o;
		treeViewer.setInput(current);
		this.objActionsGUI.insertParentNode(current);
		//this.objActionsGUI.updateFlattenView(current.iLevel);
		this.objActionsGUI.checkZoomButtons(current);
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
		if(parent.getParent() == null) {
			// in this case, the parent is the aggregate metrics
			// we will not show the node, but instead we will insert manually
			treeViewer.setInput( child );			
	    	Scope.Node  node = (Scope.Node) this.myRootScope.getTreeNode();
	    	this.objActionsGUI.insertParentNode(node);
		} else {
			treeViewer.setInput( parent );
		}
		//this.objActionsGUI.updateFlattenView(parent.iLevel);
		this.objActionsGUI.checkZoomButtons(parent);
		// do not zoom out to the root
		/*
		if(parent.getScope() instanceof RootScope)
			return;
		*/
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
    	return (ScopeViewActionsGUI.shouldZoomInBeEnabled(node));
    }
    
    public boolean shouldZoomOutBeEnabled(Scope.Node node) {
    	return (ScopeViewActionsGUI.shouldZoomOutBeEnabled(node));
    }
    
    public void checkButtons(Scope.Node node) {
    	this.objActionsGUI.checkFlattenButtons();
    	this.objActionsGUI.checkZoomButtons(node);
    }

    public void setTreeViewer(ScopeTreeViewer tree) {
    	this.treeViewer = tree;
    	this.objActionsGUI.setTreeViewer(tree);
    }
}
