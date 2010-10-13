/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.scope.FlatScopeViewActions;

/**
 * Actions GUI class specifically designed for flat view. 
 * This view includes toolbar for flatten and unflatten actions
 * 
 * @author laksonoadhianto
 *
 */
public class FlatScopeViewActionsGUI extends ScopeViewActionsGUI {

	private ToolItem tiFlatten;		//flatten button
	private ToolItem tiUnFlatten ;	// unflatten button
	protected FlatScopeViewActions objFlatAction;
	/**
	 * @param viewSite
	 * @param parent
	 * @param objActions
	 */
	public FlatScopeViewActionsGUI(Shell objShell, Composite parent,
			ScopeViewActions objActions) {
		super(objShell, parent, objActions);
		this.objFlatAction = (FlatScopeViewActions) objActions;
	}

	/**
	 * Method to start to build the GUI for the actions
	 * @param parent
	 * @return
	 */
	public Composite buildGUI(Composite parent, CoolBar coolbar) {
		// add toobar actions for flatten and unflatten
		Composite c =  addTooBarAction(coolbar);
		super.finalizeToolBar(parent, coolbar);
		return c;
	}

	/**
	 * Disables all actions
	 */
	public void resetActions() {
		super.resetActions();
		this.tiFlatten.setEnabled(false);
		this.tiUnFlatten.setEnabled(false);
	}
	
	/**
	 * Enable the some actions (resize and column properties) actions for this view
	 */
	public void enableActions() {
		super.enableActions();
		this.checkFlattenButtons();
	}

	/**
	 * Method to start to build the GUI for the actions
	 * @param parent
	 * @return
	 */
	public Composite addTooBarAction(CoolBar parent)  {
    	// prepare the toolbar
    	ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
    	Icons iconsCollection = Icons.getInstance();

    	// ------------- prepare the items
    	// flatten
    	tiFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiFlatten.setToolTipText("Flatten nodes one level");
    	tiFlatten.setImage(iconsCollection.imgFlatten);
    	tiFlatten.addSelectionListener(new SelectionAdapter() {
      	  	public void widgetSelected(SelectionEvent e) {
      	  		objFlatAction.flatten();
      	  	}
      	});
    	
    	// unflatten
    	tiUnFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiUnFlatten.setToolTipText("Unflatten nodes one level");
    	tiUnFlatten.setImage(iconsCollection.imgUnFlatten);
    	tiUnFlatten.addSelectionListener(new SelectionAdapter(){
      	  	public void widgetSelected(SelectionEvent e) {
      	  		objFlatAction.unflatten();
      	  	}    		
    	});
    	this.createCoolItem(parent, toolbar);

    	// we need to add the parent's default actions
    	// Without this statement, the default actions disappear
		Composite objComposite = super.addTooBarAction(parent);
    	
		return objComposite;
	}
    /**
     * Check if flatten/unflatten buttons need to be disable or not.
     */
    public void checkFlattenButtons() {
    	tiFlatten.setEnabled(shouldFlattenBeEnabled());
    	tiUnFlatten.setEnabled(shouldUnflattenBeEnabled());
    }

	/**
	 * Update the GUI when a flatten actions are performed
	 * @param iLevel
	 * @param showAggregate show in the root node the aggregate metrics
	 */
	public void updateFlattenView(int iLevel, boolean showAggregate) {
		if(showAggregate)
			this.displayRootExperiment();	// display the aggregate metrics
		this.checkFlattenButtons();
		//this.updateFlattenView(iLevel);
	}

	/**
	 * Check if it is possible to flatten the tree
	 * @return
	 */
    private boolean shouldFlattenBeEnabled() {
    	// DO NOT flatten if we reach to the point where there is no children 
    	return this.myRootScope.getTreeNode().getDepth()>this.myRootScope.getFlattenLevel() + 1;
    }
    
    /**
     * Verify if unflatten can be done
     * @return
     */
    private boolean shouldUnflattenBeEnabled() {
    	// DO NOT unflatten if we reach to root scope level
    	return (this.myRootScope.getFlattenLevel()>0);
    }


}