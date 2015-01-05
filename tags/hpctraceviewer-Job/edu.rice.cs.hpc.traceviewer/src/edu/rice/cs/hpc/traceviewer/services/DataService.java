package edu.rice.cs.hpc.traceviewer.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

public class DataService extends AbstractSourceProvider {

	final static public String DATA_PROVIDER = "edu.rice.cs.hpc.traceviewer.services.DataService.data";
	final static public String DATA_UPDATE = "edu.rice.cs.hpc.traceviewer.services.DataService.update";
	
	private SpaceTimeDataController data;
	

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
		map.put(DATA_UPDATE, data);
		
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {

		return new String[] {DATA_PROVIDER, DATA_UPDATE};
	}
	
	/***
	 * set the updated data
	 * @param data
	 */
	public void setData( SpaceTimeDataController data ) {
		this.data = data;
		fireSourceChanged(ISources.WORKBENCH, DATA_PROVIDER, "ENABLED");
	}
	
	/***
	 * broadcast updated data
	 */
	public void broadcastUpdate( Object updatedData ) {
		if (updatedData == null)
			fireSourceChanged(ISources.WORKBENCH, DATA_UPDATE, data);
		else
			fireSourceChanged(ISources.WORKBENCH, DATA_UPDATE, updatedData);
	}
	
	/***
	 * retrieve the current data
	 * @return
	 */
	public SpaceTimeDataController getData() {
		return data;
	}
 
	
	private String getValue() {
		return (data != null)? "ENABLED" : "DISABLED";
	}
}
