package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.core.runtime.IStatus;

import edu.rice.cs.hpc.Activator;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.viewer.util.PreferenceConstants;

/**
 * Class to manage the actions of the tree view such as zooms, flattening,
 * resize the columns, etc. This class will add additional toolbar on the top
 * of the tree. Therefore, it is necessary to instantiate this class before
 * the creation of the tree, then call the method updateContent() to associate
 * the action with the tree (once the tree is created). 
 * This looks somewhat stupid, but this is the fastest thing in my mind :-(
 * 
 * @author laksono
 *
 */
public class ScopeViewActions {
	// public preference
	static public double fTHRESHOLD = 0.6; 
    //-------------- DATA
	private ScopeViewActionsGUI objActionsGUI;	// associated GUI (toolbar)
    private ScopeTreeViewer 	treeViewer;		  	// tree 
    private Scope 		myRootScope;		// the root scope of this view
    private IViewSite objSite;				// associated view

    // stack to store the position of the zoom, tree state, ...
    private java.util.Stack<Scope.Node> stackRootTree = new java.util.Stack<Scope.Node>();
	private java.util.Stack<Object[]> stackTreeStates = new java.util.Stack<Object[]>();
	
	
    /**
     * Constructor: create actions and the GUI (which is a coolbar)
     * @param viewSite the site of the view (used for retrieving shell, display, ...)
     * @param parent composite
     */
    public ScopeViewActions(IViewSite viewSite, Composite parent) {
    	this.objActionsGUI = new ScopeViewActionsGUI(viewSite, parent, this);
    	IPreferenceStore objPref = Activator.getDefault().getPreferenceStore();
    	double fDefaultThreshold = objPref.getDouble(PreferenceConstants.P_THRESHOLD);
    	if(fDefaultThreshold > 0.0)
    		ScopeViewActions.fTHRESHOLD= fDefaultThreshold; 
    	this.objSite = viewSite;
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
	 * @param pathItem
	 * @param item
	 * @param scope
	 * @param metric
	 * @param iLevel
	 * @return
	 */
	private HotCallPath getHotCallPath(TreePath pathItem, TreeItem item, Scope scope, Metric metric, int iLevel) {
		if(scope == null || metric == null || item == null)
			return null;
		// expand the immediate child if necessary
		if(!item.getExpanded()) {
			this.treeViewer.expandToLevel(pathItem, 1);
		}
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
					HotCallPath objCallPath = new HotCallPath();
					// we found the hot call path
					objCallPath.path = this.treeViewer.getTreePath(child);
					objCallPath.item = child;
					objCallPath.node = nodeChild;
					return objCallPath;
				} else {
					// let see the next kid
					return this.getHotCallPath(this.treeViewer.getTreePath(child), 
							child, scopeChild, metric, iLevel+ 1);
				}
			}
		}
		// if we reach at this statement, then there is no hot call path !
		return null;
	}

	/**
	 * Get the current input node
	 * @return
	 */
	private Scope.Node getInputNode() {
		Object o = treeViewer.getInput();
		Scope.Node child;
		if (!(o instanceof Scope.Node)) {
			if(o instanceof ArrayOfNodes) {
				TreeItem []tiObjects = this.treeViewer.getTree().getItems();
				o = tiObjects[0];
				if(o instanceof Scope.Node)
					child = (Scope.Node)tiObjects[0].getData(); //the 0th item can be the aggregate metric
				else if(tiObjects.length>1)
					// in case of the top row is not a node, the second one MUST BE a node
					child = (Scope.Node)tiObjects[1].getData();
				else
					// Otherwise there is something wrong with the data and the tree
					throw (new java.lang.RuntimeException("ScopeViewActions: tree contains unknown objects"));
				// tricky solution when zoom-out the flattened node
				if(child != null)
					child = (Scope.Node)child.getParent();
			} else {
				// there is something wrong here ...
				throw(new java.lang.RuntimeException("ScopeViewAction: unknown input or the input is null" + o));
			}
		} else 
			child = (Scope.Node) o;
		return child;
	}
	
	
	//====================================================================================
	// ----------------------------- ACTIONS ---------------------------------------------
	//====================================================================================
	/**
	 * show the hot path below the selected node in the tree
	 */
	public void showHotCallPath() {
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
		if((colSelected == null) || colSelected.getWidth() == 0) {
			// the column is hidden or there is no column sorted
			org.eclipse.jface.dialogs.MessageDialog.openError(this.objSite.getShell(), 
					"Unknown sorted column", "Please select a column to sort before using this feature.");
		}
		// get the metric data
		Object data = colSelected.getData();
		if(data instanceof Metric && item != null) {
			Metric metric = (Metric) data;
			// find the hot call path
			int iLevel = 0;
			System.out.print("Looking for hot path with threshold of " + ScopeViewActions.fTHRESHOLD + " ... ");
			HotCallPath objHot = this.getHotCallPath(arrPath[0], item, current.getScope(), metric, iLevel);
			if(objHot != null) {
				// we find the hot path !!
				this.treeViewer.setSelection(new TreeSelection(objHot.path));
				System.out.println(" found: "+ objHot.node.getScope().getName());
				//objHot.item.setBackground(0, new Color(null,255,106,106));
			} else {
				// we cannot find it
				System.out.println(" cannot be found.\nPlease adjust the threshold int the preference dialog box.");
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
		
		// ---------------------- save the current view
		Scope.Node objInputNode = this.getInputNode();
		this.stackRootTree.push(objInputNode); // save the node for future zoom-out
		Object treeStates[] = this.treeViewer.getExpandedElements();
		this.stackTreeStates.push(treeStates);
		// ---------------------- 

		// set the new view based on the selected node
		Scope.Node current = (Scope.Node) o;
		treeViewer.setInput(current);
		// we need to insert the selected node on the top of the table
		// FIXME: this approach is not elegant, but we don't have any choice
		// 			at the moment
		this.objActionsGUI.insertParentNode(current);
		this.objActionsGUI.checkZoomButtons(current);
	}
	
	/**
	 * Zoom-out the node
	 */
	public void zoomOut() {
		Scope.Node child;
		if(this.stackRootTree.size()>0) {
			// the tree has been zoomed
			child = this.stackRootTree.pop();
		} else {
			// case where the tree hasn't been zoomed
			// FIXME: there must be a bug if the code comes to here !
			child = (Scope.Node)treeViewer.getInput();
			throw( new java.lang.RuntimeException("ScopeViewActions - illegal zoomout"+child));
		}
		Scope.Node parent = child; //(Scope.Node)child.getParent();
		if (parent == null)
			return;
		Object userObject = parent.getUserObject();
		if( (parent.getParent() == null) || 
				((userObject != null) && (userObject instanceof RootScope))  ){
			// in this case, the parent is the aggregate metrics
			// we will not show the node, but instead we will insert manually
			treeViewer.setInput( parent );			
	    	Scope.Node  node = (Scope.Node) this.myRootScope.getTreeNode();
	    	this.objActionsGUI.insertParentNode(node);
		} else {
			treeViewer.setInput( parent );
		}
		//this.objActionsGUI.updateFlattenView(parent.iLevel);
		this.objActionsGUI.checkZoomButtons(null); // no node has been selected ?
		// return the previous expanded tree items
		if(this.stackTreeStates.size()>0) {
			Object o[] = this.stackTreeStates.pop();
			this.treeViewer.setExpandedElements(o);
		}
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
		if(this.objActionsGUI.iFlatLevel <2) return;
		
		int iNewFlatLevel = this.objActionsGUI.iFlatLevel - 1;
		Integer objLevel = Integer.valueOf(iNewFlatLevel);
		java.util.Hashtable<Integer, ArrayOfNodes> tableOfNodes = ((RootScope)this.myRootScope).getTableOfNodes();
		if(tableOfNodes != null) {
			ArrayOfNodes nodeArray = tableOfNodes.get(objLevel);
			if(nodeArray != null) {
				this.treeViewer.setInput(nodeArray);
				this.objActionsGUI.updateFlattenView(iNewFlatLevel, true);
			}
		}
	}
	
	/**
	 * Resize the columns
	 */
	public void resizeColumns() {
		this.objActionsGUI.resizeTableColumns();
	}
	/**
	 * Check if zoom-in button should be enabled
	 * @param node
	 * @return
	 */
    public boolean shouldZoomInBeEnabled(Scope.Node node) {
    	return (ScopeViewActionsGUI.shouldZoomInBeEnabled(node));
    }
    
    /**
     * In case there is no selected node, we determine the zoom-out button
     * can be enabled only and only if we have at least one item in the stack
     * @return
     */
    public boolean shouldZoomOutBeEnabled() {
    	return this.stackRootTree.size()>0;
    }
    
    /**
     * Check if the buttons in the toolbar should be enable/disable
     * @param node
     */
    public void checkButtons(Scope.Node node) {
    	this.objActionsGUI.checkFlattenButtons();
    	this.objActionsGUI.checkZoomButtons(node);
    }

    /**
     * Update the content of tree viewer
     * @param tree
     */
    public void setTreeViewer(ScopeTreeViewer tree) {
    	this.treeViewer = tree;
    	this.objActionsGUI.setTreeViewer(tree);
    }
    
    //===========================================================================
    //------------------- ADDITIONAL CLASSES ------------------------------------
    //===========================================================================
    /**
     * Class to store the information on the tree item path
     * @author laksono
     *
     */
    class HotCallPath {
    	// the path of the item
    	public TreePath path;
    	// the item
    	public TreeItem item;
    	// the node associated
    	public Scope.Node node;
    }
}
