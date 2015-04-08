package edu.rice.cs.hpc.filter.service;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import edu.rice.cs.hpc.filter.action.FilterApply;

/**************************************************************
 * 
 * Class to manage filter state, whether it needs to be refreshed
 * or there's a filter selection in the view
 *
 **************************************************************/
public class FilterStateProvider extends AbstractSourceProvider 
{
	final static public String FILTER_STATE_PROVIDER = "edu.rice.cs.hpc.filter.selection";
	final static public String FILTER_REFRESH_PROVIDER = "edu.rice.cs.hpc.filter.update";
	final static public String FILTER_ENABLE_PROVIDER = "edu.rice.cs.hpc.filter.enable";
	
	final static public String TOGGLE_COMMAND = "org.eclipse.ui.commands.toggleState";
	final static public String SELECTED_STATE = "SELECTED";
	
	private boolean isSelected = false;
	private Object []elements;
	private Boolean enable = null;
	
	public FilterStateProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getCurrentState() {
		Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(FILTER_STATE_PROVIDER, getSelectedValue());
		map.put(FILTER_REFRESH_PROVIDER, FilterMap.getInstance());
		
		final String filterVal = System.getenv("FILTER");
		map.put(FILTER_ENABLE_PROVIDER, filterVal);
		
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {FILTER_STATE_PROVIDER, FILTER_REFRESH_PROVIDER, FILTER_ENABLE_PROVIDER};
	}

	
	public void setSelection(ISelection selection)
	{
		isSelected = selection != null && !selection.isEmpty();
		if (isSelected) {
			final StructuredSelection elements = (StructuredSelection) selection;
			this.elements = elements.toArray();
		} else {
			this.elements = null;
		}
		fireSourceChanged(ISources.WORKBENCH, FILTER_STATE_PROVIDER, getSelectedValue());
	}
	
	public Object[] getSelections()
	{
		return elements;
	}
	
	public boolean isEnabled()
	{
		if (enable == null) 
		{
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			Assert.isNotNull(window);
			
			ICommandService service = (ICommandService) window.getService(ICommandService.class);
			Command command = service.getCommand(FilterApply.ID);
			State   state   = command.getState(TOGGLE_COMMAND);
			enable = (Boolean) state.getValue();

		}
		return enable.booleanValue();
	}
	/*****
	 * Notify the views that we may turn on/off the filter
	 * 
	 * @param enableFilter
	 */
	public void refresh(Boolean enableFilter)
	{
		this.enable = enableFilter;
		fireSourceChanged(ISources.WORKBENCH, FILTER_REFRESH_PROVIDER, enableFilter);
	}
	
	/*****
	 * refresh the table as the filter pattern may change
	 * Usually called by FilterAdd and FilterDelete 
	 */
	public void refresh()
	{
		refresh(isEnabled());
	}
	
	private String getSelectedValue()
	{
		return (isSelected ? SELECTED_STATE : "");
	}
}
