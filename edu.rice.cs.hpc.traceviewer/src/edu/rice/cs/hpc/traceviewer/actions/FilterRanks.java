package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import edu.rice.cs.hpc.traceviewer.util.FilterRankDialog;

import edu.rice.cs.hpc.common.ui.Util;

public class FilterRanks extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = Util.getActiveShell();
		final FilterRankDialog filterDlg = new FilterRankDialog(shell);
		
		final int ret = filterDlg.open();
		
		return null;
	}

}
