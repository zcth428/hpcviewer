package edu.rice.cs.hpc.viewer.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.provider.DatabaseState;

public class OpenDatabase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final ExperimentManager expFile = new ExperimentManager(window); 
				//ExperimentData.getInstance(window).getExperimentManager();
		expFile.openFileExperiment(ExperimentManager.FLAG_DEFAULT);

		ISourceProviderService sourceProviderService = (ISourceProviderService) HandlerUtil
				.getActiveWorkbenchWindow(event).getService(
						ISourceProviderService.class);
		// Now get my service
		DatabaseState commandStateService = (DatabaseState) sourceProviderService
				.getSourceProvider(DatabaseState.MY_STATE);
		commandStateService.toogleEnabled();
		System.out.println("Change to " + commandStateService.getToogle());

		
		return null;
	}


}
