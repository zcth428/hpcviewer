package edu.rice.cs.hpc.viewer.filter;

import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.common.util.AliasMap;
import edu.rice.cs.hpc.data.experiment.scope.filters.IFilterData;

public class FilterMap extends AliasMap<String, Boolean> 
implements IFilterData
{

	static private final String FILE_NAME = "filter.map";
	static private final FilterMap filterMap = new FilterMap();
	
	private FilterStateProvider filterStateProvider = null;
	
	public static FilterMap getInstance()
	{
		return filterMap;
	}
	
	@Override
	public String getFilename() {
		
		IPath path = Platform.getLocation().makeAbsolute();
		return path.append(FILE_NAME).makeAbsolute().toString();
	}

	@Override
	public void initDefault() {
	}
	
	/******
	 * retrieve a list of filters
	 * 
	 * @return
	 */
	public Object[] getEntrySet() {
		checkData();
		return data.entrySet().toArray();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.AliasMap#put(java.lang.Object, java.lang.Object)
	 */
	public void put(String filter, Boolean state)
	{
		super.put(filter, state);
		save();
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#select(java.lang.String)
	 */
	public boolean select(String element)
	{
		if (!isFilterEnabled())
		{
			return true;
		}
		Object []entries = getEntrySet();
		
		// --------------------------------------------------------------------------------
		// this is a bad bad bad practice.
		// the complexity is O(NM) where N is the number of nodes and M is the number of patterns
		// we know this is not quick, but assuming M is very small, the order should be linear
		// --------------------------------------------------------------------------------
		for (Object entry : entries)
		{
			Entry<String, Boolean> pattern = (Entry<String, Boolean>) entry;
			Boolean toFilter = pattern.getValue();
			if (toFilter)
			{
				final String key = pattern.getKey().replace("*", ".*").replace("?", ".?");
				if (element.matches(key)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/*****
	 * rename the filter
	 * 
	 * @param oldKey : old filter
	 * @param newKey : new filter
	 * 
	 * @return true if the update successful, false otherwise
	 */
	public boolean update(String oldKey, String newKey)
	{
		Boolean val = get(oldKey);
		if (val != null)
		{
			remove(oldKey);
			put(newKey, val);
			save();
			return true;
		}
		return false;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#isFilterEnabled()
	 */
	public boolean isFilterEnabled() 
	{
		if (filterStateProvider == null)
		{
			IWorkbenchWindow window = Util.getActiveWindow();
			Assert.isNotNull(window);
			ISourceProviderService service = (ISourceProviderService) window.getService(ISourceProviderService.class);
			filterStateProvider   = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
		}
		
		return filterStateProvider.isEnabled();
	}
}
