package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;


public class CallingContextViewActions extends BaseScopeViewActions {

	public CallingContextViewActions(Shell shell, IWorkbenchWindow window,
			Composite parent, CoolBar coolbar) 
	{
		super(shell, window, parent, coolbar);
	}

    /**
     * Each class has its own typical GUI creation
     */
	protected  Composite createGUI(Composite parent, CoolBar coolbar) 
	{
    	this.objActionsGUI = new CallingContextActionsGUI(this.objShell, 
    			this.objWindow, parent, this);
    	return objActionsGUI.buildGUI(parent, coolbar);
	}
}
