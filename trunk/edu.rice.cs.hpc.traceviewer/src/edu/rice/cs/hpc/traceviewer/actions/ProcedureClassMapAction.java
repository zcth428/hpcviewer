package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import edu.rice.cs.hpc.traceviewer.ui.ProcedureClassDialog;
import edu.rice.cs.hpc.traceviewer.util.ProcedureClassMap;


/****
 * 
 * action handler to show procedure-class map dialog
 *
 */
public class ProcedureClassMapAction extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final Shell shell = HandlerUtil.getActiveShell(event);
		ProcedureClassMap classMap = new ProcedureClassMap(shell);
		ProcedureClassDialog dlg = new ProcedureClassDialog(shell, classMap);
		if ( dlg.open() == Dialog.OK ) {
			classMap.save();
		}
		
		return null;
	}

}
