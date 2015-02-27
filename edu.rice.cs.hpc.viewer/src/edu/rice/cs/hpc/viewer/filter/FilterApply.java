package edu.rice.cs.hpc.viewer.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;

public class FilterApply extends AbstractHandler 
{
	final static public String ID = "edu.rice.cs.hpc.viewer.filter.FilterApply";
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Command command = event.getCommand();
		boolean active = !HandlerUtil.toggleCommandState(command);

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();
		
		IViewReference []refs = page.getViewReferences();
		for (IViewReference ref : refs)
		{
			IViewPart part = ref.getView(false);
			if (part instanceof AbstractBaseScopeView)
			{
				((AbstractBaseScopeView)part).enableFilter( active );
			}
		}

		return null;
	}

}
