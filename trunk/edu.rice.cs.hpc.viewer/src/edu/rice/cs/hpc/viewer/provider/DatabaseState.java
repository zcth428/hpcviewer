package edu.rice.cs.hpc.viewer.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

public class DatabaseState extends AbstractSourceProvider 
{
	public final static String DATABASE_ACTIVE_STATE = "edu.rice.cs.hpc.viewer.provider.data.active";
	public final static String DATABASE_MERGE_STATE  = "edu.rice.cs.hpc.viewer.provider.data.merge";
	static final public String DATABASE_NEED_REFRESH = "edu.rice.cs.hpc.viewer.provider.data.refresh";

	public final static String ENABLED = "ENABLED";
	public final static String DISABLED = "DISABLED";
	
	private int num_opened_database = 0;
	
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() 
	{
		Map<String, Object> map = new HashMap<String, Object>(2);
		String value = num_opened_database>0 ? ENABLED : DISABLED;
		map.put(DATABASE_ACTIVE_STATE, value);
		
		value = num_opened_database>1 ? ENABLED : DISABLED;
		map.put(DATABASE_MERGE_STATE, value);
		
		map.put(DATABASE_NEED_REFRESH, Boolean.valueOf(false));
		
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() 
	{
		return new String[] { DATABASE_ACTIVE_STATE, DATABASE_MERGE_STATE, DATABASE_NEED_REFRESH };
	}


	public void toogleEnabled(IWorkbenchWindow window) 
	{
		ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
		
		num_opened_database = vw.getOpenDatabases();
		String value = num_opened_database>0 ? ENABLED : DISABLED;
		fireSourceChanged(ISources.WORKBENCH, DATABASE_ACTIVE_STATE, value);
		
		value = num_opened_database>1 ? ENABLED : DISABLED;
		fireSourceChanged(ISources.WORKBENCH, DATABASE_MERGE_STATE, value);
	}

	public void refreshDatabase(boolean filter)
	{
		fireSourceChanged(ISources.WORKBENCH, DATABASE_NEED_REFRESH, filter);
	}
}
