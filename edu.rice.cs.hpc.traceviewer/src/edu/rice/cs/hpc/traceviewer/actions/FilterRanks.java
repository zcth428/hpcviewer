package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.traceviewer.filter.FilterTimeline;
import edu.rice.cs.hpc.traceviewer.filter.FilterDialog;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.timeline.ITimeline;


/******
 * 
 * An action to filter ranks 
 *
 */
public class FilterRanks extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = HandlerUtil.getActiveShell(event);
		IWorkbenchWindow winObj = HandlerUtil.getActiveWorkbenchWindow(event);
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
				ISourceProviderService.class);
		
		DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);

		final SpaceTimeData data = dataService.getData();
		FilterTimeline filter;
		
		// get the existing filter if exist
		ITimeline timeline = data.getTimeline();
		if (timeline instanceof FilterTimeline) {
			// filter exists
			filter = (FilterTimeline) timeline;
		} else {
			// the filter doesn't exist, we need to create a new one
			filter = new FilterTimeline();
		}
		
		FilterDialog dlg = new FilterDialog(shell, filter);
		
		if (dlg.open() == Dialog.OK) {
			filter.setShownMode(false);
			filter.filter(data.getProcessNames());
			data.setTimeline(filter);
			dataService.broadcastUpdate(data);
		}
		
		return null; 
	}

}
