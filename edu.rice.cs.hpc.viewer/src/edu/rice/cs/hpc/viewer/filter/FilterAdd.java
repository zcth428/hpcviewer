package edu.rice.cs.hpc.viewer.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

public class FilterAdd extends AbstractHandler 
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		IWorkbenchWindow winObj = HandlerUtil.getActiveWorkbenchWindow(event);
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
				ISourceProviderService.class);

		final Shell shell = HandlerUtil.getActiveShell(event);
		final InputDialog dialog = new InputDialog(shell, "Add a pattern", 
				"Use a glob pattern to define a filter. For instance, a MPI* will filter all MPI routines", 
				"", new PatternValidator());
		
		if (dialog.open() == Window.OK)
		{
			final FilterMap filterMap = FilterMap.getInstance();
			filterMap.put(dialog.getValue(), Boolean.TRUE);
			
			// notify changes
			final FilterStateProvider provider = (FilterStateProvider) sourceProviderService.getSourceProvider(
					FilterStateProvider.FILTER_STATE_PROVIDER);

			provider.refresh();
		}

		return null;
	}

}
