package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.ui.part.ViewPart;

public abstract class AbstractScopeView extends ViewPart {
	protected ScopeViewActions objViewActions;	// actions for this scope view

	@Override
	public void createPartControl(Composite parent) {
    	Composite objCompositeParent;
    	objCompositeParent = this.createToolBarArea(parent);
    	CoolBar objCoolbar = this.initToolbar(objCompositeParent);
		this.objViewActions =  createActions(objCompositeParent, objCoolbar); //actions of the tree

	}


    
    /**
     * Create the toolbar layout
     * @param parent
     * @return
     */
    protected Composite createToolBarArea(Composite parent) {
    	// make the parent with grid layout
    	Composite toolbarArea = new Composite(parent, SWT.NONE);
    	GridLayout grid = new GridLayout(1,false);
    	parent.setLayout(grid);
    	return toolbarArea;
    }

    /**
     * Create and Initialize coolbar, set the layout and return the coolbar 
     * @param toolbarArea
     * @return
     */
    protected CoolBar initToolbar(Composite toolbarArea) {
    	CoolBar coolBar = new CoolBar(toolbarArea, SWT.FLAT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	coolBar.setLayoutData(data);

    	return coolBar;
    }
    
    /**
     * The derived class has to implement this method to create its own actions
     * For instance, caller view and cct view has the same actions but flat view
     * 	may have additional actions (flattening ...)
     * @param parent
     * @param coolbar
     * @return
     */
    abstract protected ScopeViewActions createActions(Composite parent, CoolBar coolbar);

}
