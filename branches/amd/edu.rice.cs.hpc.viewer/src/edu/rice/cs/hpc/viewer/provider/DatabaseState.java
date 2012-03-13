package edu.rice.cs.hpc.viewer.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class DatabaseState extends AbstractSourceProvider {

	public final static String MY_STATE = "edu.rice.cs.hpc.viewer.provider.data.active";
	public final static String ENABLED = "ENABLED";
	public final static String DISENABLED = "DISABLED";
	private boolean enabled = false;

	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Map getCurrentState() {
		Map<String, Object> map = new HashMap<String, Object>(1);
		String value = enabled ? ENABLED : DISENABLED;
		map.put(MY_STATE, value);
		return map;
	}

	public String[] getProvidedSourceNames() {
		return new String[] { MY_STATE };
	}


	public void toogleEnabled() {
		enabled = !enabled ;
		String value = enabled ? ENABLED : DISENABLED;
		fireSourceChanged(ISources.WORKBENCH, MY_STATE, value);
	}
	
	public boolean getToogle()
	{
		return enabled;
	}

}
