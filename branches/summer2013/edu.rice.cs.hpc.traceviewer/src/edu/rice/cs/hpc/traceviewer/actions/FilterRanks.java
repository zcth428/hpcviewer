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
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.filter.FilterDialog;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerRemote;

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
        SpaceTimeDataController absData = dataService.getData();
        
        
        if (absData instanceof SpaceTimeDataControllerRemote){
        	System.out.println("Data filtering not yet supported for remote databases.");
        	return null;
        }
        
		final SpaceTimeDataControllerLocal data  = (SpaceTimeDataControllerLocal) absData;
		IBaseData baseData = data.getBaseData();
		
		Filter filter;
		if (baseData instanceof FilteredBaseData) {
			filter = ((FilteredBaseData)baseData).getFilter();
		} else {
			filter = new Filter();
		}
	
		try {
			
			FilteredBaseData filteredBaseData = new FilteredBaseData(data.getTraceFileAbsolutePath(), 
					data.getTraceAttribute().dbHeaderSize, TraceAttribute.DEFAULT_RECORD_SIZE);
			
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
