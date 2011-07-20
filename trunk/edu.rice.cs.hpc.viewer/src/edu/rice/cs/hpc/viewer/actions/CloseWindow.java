/**
 * 
 */
package edu.rice.cs.hpc.viewer.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * @author laksonoadhianto
 *
 */
public class CloseWindow extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.commands.IHandler#execute(java.util.Map)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow winWorkbench = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// close editor windows
		winWorkbench.getActivePage().closeAllEditors(false);
		// close the workbench (which will close the application as well)
		winWorkbench.close();
		return null;
	}

}
