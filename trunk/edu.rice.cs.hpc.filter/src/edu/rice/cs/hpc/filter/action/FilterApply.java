package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.filter.service.FilterStateProvider;


public class FilterApply extends AbstractHandler 
{
	final static public String ID = "edu.rice.cs.hpc.filter.action.FilterApply";
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Command command = event.getCommand();
		boolean active = !HandlerUtil.toggleCommandState(command);

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
		FilterStateProvider provider   = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
		provider.refresh(Boolean.valueOf(active));

		return null;
	}

}
