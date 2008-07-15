/**
 * 
 */
package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * @author laksonoadhianto
 *
 */
public class NewWindow implements IWorkbenchWindowActionDelegate {

	/**
	 * 
	 */
	public NewWindow() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// TODO Auto-generated method stub
		try {
			IWorkbenchWindow workbench = PlatformUI.getWorkbench().openWorkbenchWindow(null);
			if(workbench == null) {
				System.err.println("NW: unable to create an RCP workbench");
			} else {
				/*
				System.out.println("NW: successful: "+workbench.toString()+ 
						"wrk: "+workbench.getWorkbench().toString()+" pp: "+workbench.getActivePage().toString() +
						"nb-pp:"+workbench.getPages().length);
				*/
			}
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
