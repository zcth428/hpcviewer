package edu.rice.cs.hpc.filter.action;

import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.filter.service.FilterMap;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;

/************************************************************************
 * 
 * Action to deleted selected elements
 *
 ************************************************************************/
public class FilterDelete extends AbstractHandler 
{
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		IWorkbenchWindow winObj = HandlerUtil.getActiveWorkbenchWindow(event);
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
				ISourceProviderService.class);
		final FilterStateProvider provider = (FilterStateProvider) sourceProviderService.getSourceProvider(
				FilterStateProvider.FILTER_STATE_PROVIDER);

		Object []elements = provider.getSelections();
		if (elements != null && elements.length > 0)
		{
			final Shell shell = HandlerUtil.getActiveShell(event);
			if (MessageDialog.openQuestion(shell, "Remove a filter pattern", 
					"Are you sure to delete the " + elements.length +
					" selected pattern(s) ?")) 
			{
				final FilterMap map = FilterMap.getInstance();
				for (Object element : elements)
				{
					Entry<String, Boolean> item = (Entry<String, Boolean>) element;
					map.remove(item.getKey());
				}
				map.save();
				// notify changes
				provider.refresh();
			}
		}
		return null;
	}
}
