/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public interface IScopeActionsGUI {

	/**
	 * Method to start to build the GUI for the actions
	 * @param parent
	 * @return
	 */
	public Composite buildGUI(Composite parent, CoolBar coolbar);
	
	/**
	 * Method to start to build the GUI for the actions
	 * @param parent
	 * @return
	 */
	public Composite addTooBarAction(CoolBar parent) ;
	
	/**
	 * IMPORTANT: need to call this method once the content of tree is changed !
	 * Warning: call only this method when the tree has been populated !
	 * @param exp
	 * @param scope
	 * @param columns
	 */
	public void updateContent(Experiment exp, RootScope scope, TreeViewerColumn []columns);

	/**
	 * Set the new tree viewer
	 * @param tree
	 */
	public void setTreeViewer(TreeViewer tree);

	/**
	 * Show a warning message (with yellow background).
	 * The caller has to remove the message and restore it to the original state
	 * by calling restoreMessage() method
	 */
	public void showWarningMessagge(String sMsg);
	
	/**
	 * Restore the message bar into the original state
	 */
	public void restoreMessage();
	
	/**
	 * Show an error message on the message bar. It is the caller responsibility to 
	 * remove the message
	 * @param sMsg
	 */
	public void showErrorMessage(String sMsg);
	
	/**
	 * Show the information on the message bar
	 * @param sMsg
	 */
	public void showInfoMessgae(String sMsg);
	/**
     * Check zoom buttons (zoom out and zoom in)
     * @param node: the current selected node
     */
    public void checkZoomButtons(Scope.Node node);
    
    /**
     * Disable actions that need a selected node
     */
    public void disableNodeButtons();
	
	/**
	 * Reset the button and actions into disabled state
	 */
	public void resetActions();

	/**
	 * Enable the some actions (resize and column properties) actions for this view
	 */
	public void enableActions();
	
    /**
     * Inserting a "node header" on the top of the table to display
     * either aggregate metrics or "parent" node (due to zoom-in)
     * TODO: we need to shift to the left a little bit
     * @param nodeParent
     */
    public void insertParentNode(Scope.Node nodeParent);
    
    /**
     * Add a new metric column
     * @param colMetric
     */
    public void addMetricColumns(TreeViewerColumn colMetric);
    
	/**
	 * Resize the columns automatically
	 * ATT: Please call this method once the data has been populated
	 */
	public void resizeTableColumns();
	
	public void setColumnsStatus(boolean []status);
}
