package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

public class OptionMidpoint extends AbstractHandler {

	final static public String commandId = "edu.rice.cs.hpc.traceviewer.actions.OptionMidpoint";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		
		Command command = event.getCommand();
		if (command != null) 
		{
			ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
					ISourceProviderService.class);
			DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);
			final SpaceTimeDataController data = dataService.getData();

			boolean enable = !Util.isOptionEnabled(command);
			data.setEnableMidpoint(enable);
		}

		HandlerUtil.toggleCommandState(command);
		// use the old value and perform the operation 
		return null;
	}

}
