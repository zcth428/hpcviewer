package edu.rice.cs.hpc.traceviewer.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

public class DataService extends AbstractSourceProvider {

	final static public String DATA_PROVIDER = "edu.rice.cs.hpc.traceviewer.services.DataService.data";
	
	private SpaceTimeData data;
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() {

		Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(DATA_PROVIDER, getValue());
		
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {

		return new String[] {DATA_PROVIDER};
	}
	
	/***
	 * set the updated data
	 * @param data
	 */
	public void setData( SpaceTimeData data ) {
		this.data = data;
		fireSourceChanged(ISources.WORKBENCH, DATA_PROVIDER, "ENABLED");
	}
	
	/***
	 * retrieve the current data
	 * @return
	 */
	public SpaceTimeData getData() {
		return data;
	}
 
	
	private String getValue() {
		return (data != null)? "ENABLED" : "DISABLED";
	}
}
