package edu.rice.cs.hpc.viewer.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowFilterView extends AbstractHandler {

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null)
		{
			try {
				IViewPart part = page.showView(FilterView.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
