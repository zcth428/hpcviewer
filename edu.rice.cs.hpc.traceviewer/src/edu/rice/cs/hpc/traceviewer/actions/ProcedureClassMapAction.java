package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;
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
		ProcedureClassMap classMap = new ProcedureClassMap(shell.getDisplay());
		ProcedureClassDialog dlg = new ProcedureClassDialog(shell, classMap);
		if ( dlg.open() == Dialog.OK ) {
			classMap.save();
			broadcastChanges(event);
		}
		
		return null;
	}

	private void broadcastChanges(ExecutionEvent event) {
		IWorkbenchWindow winObj = HandlerUtil.getActiveWorkbenchWindow(event);
		if (winObj == null ){
			winObj = Util.getActiveWindow();
		}
		
		if (winObj == null) {
			// impossible to get a window handle
			return;
		}
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
				ISourceProviderService.class);
		
		DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);
		// reset the color table
		ColorTable colorTable = dataService.getData().getPainter().getColorTable(); 
		colorTable.dispose();
		colorTable.setColorTable();
		
		// broadcast to all views
		dataService.broadcastUpdate(null);
	}
}
