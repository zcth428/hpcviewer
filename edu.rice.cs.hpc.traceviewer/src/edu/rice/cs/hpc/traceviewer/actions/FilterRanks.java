package edu.rice.cs.hpc.traceviewer.actions;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.experiment.extdata.FilteredBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.Filter;
import edu.rice.cs.hpc.traceviewer.filter.FilterDialog;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*****
 * 
 * Action class to filter ranks
 *
 */
public class FilterRanks extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final Shell shell = HandlerUtil.getActiveShell(event);
		IWorkbenchWindow winObj = HandlerUtil.getActiveWorkbenchWindow(event);
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
				ISourceProviderService.class);
		
		DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);

		final SpaceTimeDataController data = dataService.getData();
		IBaseData baseData = data.getBaseData();
		
		Filter filter;
		if (baseData instanceof FilteredBaseData) {
			filter = ((FilteredBaseData)baseData).getFilter();
		} else {
			filter = new Filter();
		}
	
		try {
			
			FilteredBaseData filteredBaseData = new FilteredBaseData(data.getTraceFile().getAbsolutePath(), 
					data.getTraceAttribute().dbHeaderSize, 24);
			
			filteredBaseData.setFilter( filter );
			
			FilterDialog dlgFilter = new FilterDialog(shell, filteredBaseData);
			if (dlgFilter.open() == Dialog.OK) {
				
				data.setBaseData(filteredBaseData);
				dataService.broadcastUpdate(new Boolean(true));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return null;
	}

}
