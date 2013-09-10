package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;



/*************************************************************
 *
 * Command menu to toggle debug mode to display CCT
 * 
 *************************************************************/
public class DebugShowCCT extends AbstractHandler {

	static final public String commandId = "edu.rice.cs.hpc.viewer.command.debug.showCCT";

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		// toggle the debug mode
		HandlerUtil.toggleCommandState( event.getCommand() );
		
		final ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
		
		return null;
	}
}
