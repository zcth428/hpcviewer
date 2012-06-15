package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.util.FilterRankDialog;

public class FilterRanks extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = HandlerUtil.getActiveShell(event);
		final FilterRankDialog filterDlg = new FilterRankDialog(shell);
		
		final int ret = filterDlg.open();
		
		return null;
	}

}
