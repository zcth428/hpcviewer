package edu.rice.cs.hpc.viewer.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**************************************************************
 * 
 * Class to manage filter state, whether it needs to be refreshed
 * or there's a filter selection in the view
 *
 **************************************************************/
public class FilterStateProvider extends AbstractSourceProvider 
{
	final static public String FILTER_STATE_PROVIDER = "edu.rice.cs.hpc.viewer.filter.selection";
	final static public String FILTER_REFRESH_PROVIDER = "edu.rice.cs.hpc.viewer.filter.refresh";
	final static public String SELECTED_STATE = "SELECTED";
	
	private boolean isSelected = false;
	private Object []elements;
	
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
		
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {FILTER_STATE_PROVIDER, FILTER_REFRESH_PROVIDER};
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
	
	public void refresh()
	{
		fireSourceChanged(ISources.WORKBENCH, FILTER_REFRESH_PROVIDER, FilterMap.getInstance());
	}
	
	private String getSelectedValue()
	{
		return (isSelected ? SELECTED_STATE : "");
	}
}
