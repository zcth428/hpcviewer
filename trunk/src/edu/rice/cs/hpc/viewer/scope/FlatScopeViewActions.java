/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.widgets.CoolBar;

import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public class FlatScopeViewActions extends ScopeViewActions {
	private FlatScopeViewActionsGUI objFlatActionsGUI;
	
	/**
	 * @param viewSite
	 * @param parent
	 */
	public FlatScopeViewActions(Shell shell, IWorkbenchWindow window, Composite parent, CoolBar coolbar) {
		super(shell, window, parent, coolbar);
		//this.createGUI(parent, coolbar);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create your own specific GUI
	 * @param parent
	 * @return
	 */
	protected Composite createGUI(Composite parent, CoolBar coolbar) {
		objFlatActionsGUI = new FlatScopeViewActionsGUI(this.objShell, this.objWindow, parent, this);
		this.objActionsGUI = this.objFlatActionsGUI;
		this.objFlatActionsGUI.buildGUI(parent, coolbar);
		return parent;
	}
	   /**
     * Check if the buttons in the toolbar should be enable/disable
     * @param node
     */
    public void checkButtons(Scope.Node node) {
    	this.objFlatActionsGUI.checkFlattenButtons();
    	super.checkButtons(node);
    }
    
	/**
	 * Flatten the tree one level more
	 */
	public void flatten() {
		ArrayOfNodes arrNodes = ((RootScope)this.myRootScope).getFlatten();
		if(arrNodes != null) {
			this.treeViewer.getTree().setRedraw(false);
			// we update the data of the table
			this.treeViewer.setInput(arrNodes);
			// refreshing the table to take into account a new data
			this.treeViewer.refresh();
			// post processing: inserting the "aggregate metric" into the top row of the table
			this.objFlatActionsGUI.updateFlattenView(this.myRootScope.getFlattenLevel(), true);
			this.treeViewer.getTree().setRedraw(true);
		} else {
			// either there is something wrong or we cannot flatten anymore
			//this.objActionsGUI.updateFlattenView(this.myRootScope.getFlattenLevel());
			
		}
	}

	/**
	 * Unflatten flattened tree (tree has to be flattened before)
	 */
	public void unflatten() {
		ArrayOfNodes arrNodes = ((RootScope)this.myRootScope).getUnflatten();
		if(arrNodes != null) {
			this.treeViewer.setInput(arrNodes);
			this.objFlatActionsGUI.updateFlattenView(this.myRootScope.getFlattenLevel(), true);
		}
	}

}
