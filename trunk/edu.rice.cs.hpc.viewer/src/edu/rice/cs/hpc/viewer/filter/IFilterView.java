package edu.rice.cs.hpc.viewer.filter;

import org.eclipse.ui.IViewPart;

public interface IFilterView extends IViewPart 
{	
	/***
	 * a new data input has been set and need to be refreshed
	 */
	public void refresh();
	
	/*****
	 * retrieve all checked elements
	 * 
	 * @return checked elements, null if doesn't exist
	 */
	public Object[] getSelectedElements();
}
