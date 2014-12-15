/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public class BaseScopeViewActions extends ScopeViewActions {

	public BaseScopeViewActions(Shell shell, IWorkbenchWindow window,
			Composite parent, CoolBar coolbar) {
		super(shell, window, parent, coolbar);
		// TODO Auto-generated constructor stub
	}

	public void checkStates(Scope nodeSelected) {
    	boolean bCanZoomIn = objZoom.canZoomIn(nodeSelected);
		objActionsGUI.enableZoomIn( bCanZoomIn );
		objActionsGUI.enableHotCallPath( bCanZoomIn );
		
		checkStates();
	}

	public void checkStates() {
		objActionsGUI.enableZoomOut( objZoom.canZoomOut() );
	}

    /**
     * Each class has its own typical GUI creation
     */
	protected  Composite createGUI(Composite parent, CoolBar coolbar) {
    	this.objActionsGUI = new ScopeViewActionsGUI(this.objShell, this.objWindow, parent, this);
    	return objActionsGUI.buildGUI(parent, coolbar);
	}


	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.ScopeViewActions#actionZoom(edu.rice.cs.hpc.viewer.scope.ScopeViewActions.ZoomType)
	 */
	protected void registerAction(IActionType type) {	}

}
