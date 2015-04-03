package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.filter.view.FilterView;

public class ShowFilterView extends AbstractHandler {

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null)
		{
			boolean oldVal = HandlerUtil.toggleCommandState(event.getCommand());
			boolean newVal = !oldVal;
			
			try {
				if (newVal) {
					page.showView(FilterView.ID);
				} else {
					final IViewPart part = page.findView(FilterView.ID);
					page.hideView( part );
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
