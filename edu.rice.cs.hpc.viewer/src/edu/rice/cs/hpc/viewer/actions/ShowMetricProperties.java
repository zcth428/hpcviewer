package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.viewer.util.MetricPropertyDialog;

public class ShowMetricProperties extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final Shell shell = HandlerUtil.getActiveShell(event);
		MetricPropertyDialog dialog = new MetricPropertyDialog(shell);
		
		//dialog.setData(columns);
		
		dialog.open();
		
		return null;
	}

}
