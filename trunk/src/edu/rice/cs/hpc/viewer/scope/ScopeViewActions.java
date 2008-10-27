package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.dialogs.Dialog;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.CoolBar;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.*;

import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.metric.*;
//math expression
import com.graphbuilder.math.*;
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
public class ScopeViewActions extends ScopeActions {
    //-------------- DATA
    protected ScopeTreeViewer 	treeViewer;		  	// tree 
    protected RootScope 		myRootScope;		// the root scope of this view

    // stack to store the position of the zoom, tree state, ...
    private java.util.Stack<Scope.Node> stackRootTree = new java.util.Stack<Scope.Node>();
	private java.util.Stack<Object[]> stackTreeStates = new java.util.Stack<Object[]>();
	
	protected IWorkbenchWindow objWindow;
    /**
     * Constructor: create actions and the GUI (which is a coolbar)
     * @param viewSite the site of the view (used for retrieving shell, display, ...)
     * @param parent composite
     */
    public ScopeViewActions(Shell shell, IWorkbenchWindow window, Composite parent, CoolBar coolbar) {
    	super(shell, parent, coolbar);
    	createGUI(parent, coolbar);
    	this.objWindow  = window;
    }

    /**
     * Each class has its own typical GUI creation
     */
	protected  Composite createGUI(Composite parent, CoolBar coolbar) {
    	this.objActionsGUI = new ScopeViewActionsGUI(objShell, parent, this);
    	this.objActionsGUI.buildGUI(parent, coolbar);
		return parent;
	}

    /**
     * The tree has been updated or has new content. This object needs to refresh
     * the data and variable initialization too.
     * @param exp
     * @param scope
     * @param columns
     */
	public void updateContent(Experiment exp, RootScope scope, TreeViewerColumn []columns) {
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
	private HotCallPath getHotCallPath(TreePath pathItem, TreeItem item, Scope scope, BaseMetric metric, int iLevel) {
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
				// get the child node
				Scope.Node nodeChild = (Scope.Node) o;
				Scope scopeChild = nodeChild.getScope();
				// get the values
				double x1, x2;
				double dParent, dChild;

				MetricValue mvParent = metric.getValue(scope);
				MetricValue mvChild = metric.getValue(scopeChild);
				dParent = mvParent.getValue();
				dChild = mvChild.getValue();
				
				// normalization: x1 must be bigger than x2
				if(dParent > dChild) {
					x1 = dParent; x2 = dChild;
				} else {
					x1 = dChild; x2 = dParent;
				}

				// simple comparison: if the child has "huge" difference compared to its parent
				// then we consider it as host spot node.
				if(x2 < (ScopeViewActions.fTHRESHOLD * x1)) {
					HotCallPath objCallPath = new HotCallPath();
					// we found the hot call path
					objCallPath.path = pathItem; // this.treeViewer.getTreePath(child);
					objCallPath.item = item; // child;
					objCallPath.node = (Scope.Node) item.getData(); // nodeChild;
					return objCallPath;
				} else {
					// let move deeper down the tree
					HotCallPath objHotPath = this.getHotCallPath(this.treeViewer.getTreePath(child), 
							child, scopeChild, metric, iLevel+ 1);
					// BUG FIX no 126: 
					if(objHotPath != null) {
						return objHotPath; // a hot path is found
					}
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
	
	public void showProcessingMessage() {
		this.objShell.getDisplay().asyncExec(new Runnable(){
			public void run() {
				objActionsGUI.showWarningMessagge("... Processing .... Please wait ...");
			}
		});
	}
	
	/**
	 * Class to restoring the background of the message bar by waiting for 5 seconds
	 * TODO: we need to parameterize the timing for the wait
	 * @author la5
	 *
	 */
	class RestoreMessageThread extends Thread {	
		RestoreMessageThread() {
			super();
		}
         public void run() {
             try{
            	 sleep(5000);
             } catch(InterruptedException e) {
            	 e.printStackTrace();
             }
             // need to run from UI-thread for restoring the background
             // without UI-thread we will get SWTException !!
             objShell.getDisplay().asyncExec( new Runnable() {
            	 public void run() {
                	 objActionsGUI.restoreMessage();
            	 }
             });
         }
     }
	
	public void showInfoMessage(String sMsg) {
		this.objActionsGUI.showInfoMessgae(sMsg);
		// remove the msg in 5 secs
		RestoreMessageThread thrRestoreMessage = new RestoreMessageThread();
		thrRestoreMessage.start();
	}
	
	/**
	 * Show an error message on the message bar (closed to the toolbar) and
	 * wait for 5 seconds before removing the message
	 * @param strMsg
	 */
	public void showErrorMessage(String strMsg) {
		this.objActionsGUI.showErrorMessage(strMsg);
		// remove the msg in 5 secs
		RestoreMessageThread thrRestoreMessage = new RestoreMessageThread();
		thrRestoreMessage.start();

	}
	
	/**
	 * asynchronously removing the message on the message bar and restoring the
	 * background color
	 */
	public void restoreProcessingMessage() {
		this.objShell.getDisplay().asyncExec(new Runnable(){
			public void run() {
				objActionsGUI.restoreMessage();
			}
		});
	}
	/**
	 * show the hot path below the selected node in the tree
	 */
	public void showHotCallPath() {
		// preparing the message
		//this.showProcessingMessage();
		// find the selected node
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection)) {
			System.err.println("SVA: not a TreeSelecton instance");
			return;
		}
		TreeSelection objSel = (TreeSelection) sel;
		// get the node
		Object o = objSel.getFirstElement();
		if (!(o instanceof Scope.Node)) {
			showErrorMessage("Please select a scope node.");
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
			this.showErrorMessage("Please select a column to sort before using this feature.");
			return;
		}
		// get the metric data
		Object data = colSelected.getData();
		if(data instanceof BaseMetric && item != null) {
			BaseMetric metric = (BaseMetric) data;
			// find the hot call path
			int iLevel = 0;
			HotCallPath objHot = this.getHotCallPath(arrPath[0], item, current.getScope(), metric, iLevel);
			if(objHot != null) {
				// we found the hot path
				this.treeViewer.setSelection(new TreeSelection(objHot.path), true);
			} else {
				// we cannot find it
				this.showErrorMessage("No hot call path detected.");
			}
		} else {
			// It is almost impossible for the jvm to reach this part of branch.
			// but if it is the case, it should be a BUG !!
			if(data !=null )
				System.err.println("SVA BUG: data="+data.getClass()+" item= " + (item==null? 0 : item.getItemCount()));
			else
				this.showErrorMessage("Please select a metric column !");
		}
	}
	
	/**
	 * Retrieve the selected node
	 * @return null if there is no selected node
	 */
	private Scope.Node getSelectedNode() {
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection))
			return null;
		Object o = ((TreeSelection)sel).getFirstElement();
		if (!(o instanceof Scope.Node)) {
			return null;
		}
		return (Scope.Node) o;
	}
	/**
	 * Zoom-in the children
	 */
	public void zoomIn() {
		// set the new view based on the selected node
		Scope.Node current = this.getSelectedNode();
		if(current == null)
			return;
		
		// ---------------------- save the current view
		Scope.Node objInputNode = this.getInputNode();
		this.stackRootTree.push(objInputNode); // save the node for future zoom-out
		Object treeStates[] = this.treeViewer.getExpandedElements();
		this.stackTreeStates.push(treeStates);
		// ---------------------- 

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
		Scope.Node parent; 
		if(this.stackRootTree.size()>0) {
			// the tree has been zoomed
			parent = this.stackRootTree.pop();
		} else {
			// case where the tree hasn't been zoomed
			// FIXME: there must be a bug if the code comes to here !
			parent = (Scope.Node)treeViewer.getInput();
			throw( new java.lang.RuntimeException("ScopeViewActions - illegal zoomout"+parent));
		}
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
			// Bug fix: we need to insert the parent on the top of the table
	    	this.objActionsGUI.insertParentNode(parent);
		}

		// return the previous expanded tree items
		if(this.stackTreeStates.size()>0) {
			Object o[] = this.stackTreeStates.pop();
			this.treeViewer.setExpandedElements(o);
		}
		// funny behavior on Windows: they still keep the track of the previously selected item !!
		// therefore we need to check again the state of the buttons
		Scope.Node nodeSelected = this.getSelectedNode();
		this.objActionsGUI.checkZoomButtons(nodeSelected); // no node has been selected ?
	}
	
	/**
	 * add a new column for metric
	 * @param colMetric
	 */
	protected void addTreeColumn(TreeViewerColumn colMetric) {
		this.objActionsGUI.addMetricColumns(colMetric);
	}
	
	/**
	 * create a new metric based on a free expression
	 */
	public void addExtNewMetric() {
		// prepare the dialog box
		ExtDerivedMetricDlg dlg = new ExtDerivedMetricDlg(this.objShell, 
				this.myRootScope.getExperiment().getMetrics());
		// prepare the scope node for the preview of the expression
		Scope.Node node = this.getSelectedNode();
		if(node == null)
			node = (Scope.Node) this.getInputNode();

		// display the dialog box
		if(dlg.open() == Dialog.OK) {
			// the expression is valid (already verified in the dialog box)
			Expression expFormula = dlg.getExpression();
			String sName = dlg.getName();					// metric name
			boolean bPercent = dlg.getPercentDisplay();		// display the percentage ?
			/*
			MetricType objMetricType;			
			if(dlg.isExclusive())
				objMetricType = MetricType.EXCLUSIVE;
			else
				objMetricType = MetricType.INCLUSIVE;
			*/
			Experiment exp = this.myRootScope.getExperiment();
			// add a derived metric and register it to the experiment database
			DerivedMetric objMetric = exp.addDerivedMetric(this.myRootScope, expFormula, sName, bPercent, MetricType.EXCLUSIVE);
			ExperimentData objExpData = ExperimentData.getInstance(this.objWindow);
			ExperimentManager objExpManager = objExpData.getExperimentManager();
			BaseScopeView arrScopeViews[] = objExpManager.getExperimentView().getViews();
			for(int i=0; i<arrScopeViews.length; i++) {
				ScopeTreeViewer objTreeViewer = arrScopeViews[i].getTreeViewer();
				objTreeViewer.getTree().setRedraw(false);
				TreeViewerColumn colDerived = objTreeViewer.addTreeColumn(objMetric,  false);
				// update the viewer, to refresh its content and invoke the provider
				// bug SWT https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
				// we need to hold the UI to draw until all the data is available
				objTreeViewer.refresh();	// we refresh to update the data model of the table
				// notify the GUI that we have added a new column
				ScopeViewActions objAction = arrScopeViews[i].getViewActions();
				objAction.addTreeColumn(colDerived);
				//this.objActionsGUI.addMetricColumns(colDerived); 
				objTreeViewer.getTree().setRedraw(true);
				// adjust the column width 
				colDerived.getColumn().pack();
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
    	this.objActionsGUI.checkZoomButtons(node);
    }

    /**
     * Check if zooms and hot-path button need to be disabled or not
     * This is required to solve bug no 132: 
     * https://outreach.scidac.gov/tracker/index.php?func=detail&aid=132&group_id=22&atid=169
     */
    public void checkNodeButtons() {
    	Scope.Node node = this.getSelectedNode();
    	if(node == null)
    		this.objActionsGUI.disableNodeButtons();
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
