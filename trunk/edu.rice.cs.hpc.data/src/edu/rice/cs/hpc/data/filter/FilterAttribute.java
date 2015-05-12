package edu.rice.cs.hpc.data.filter;

import java.io.Serializable;

/****************************************************
 * 
 * Attribute of a filter.
 * A filter has two attributes:<br/>
 * <ul>
 *  <li> enable: flag for enable/disable
 *  <li> filterType: type of filtering (inclusive or exclusive)
 * </ul>
 ****************************************************/
public class FilterAttribute implements Serializable
{
	/**
	 * dummy id for serialization
	 */
	private static final long serialVersionUID = 1399047856674915771L;

	/****
	 * 
	 * Enumeration for type of filter: 
	 * inclusive: filter the nodes and its descendants
	 * exclusive: filter only the node
	 */
	static public enum Type {Self_And_Children, Self_Only, Children_Only};
	
	/***
	 * Flag true: the filter is enabled
	 * Flag false: disabled
	 */
	public Boolean enable  = true;
	
	/*****
	 * @see Type
	 */
	public Type filterType = Type.Self_And_Children;
	
	/*****
	 * get the name of the filter
	 * 
	 * @return
	 */
	public String getFilterType() 
	{
		return filterType.name();
	}
	
	/*****
	 * retrieve the names of filter attributes
	 * 
	 * @return
	 */
	static public String[] getFilterNames()
	{
		FilterAttribute.Type []types = FilterAttribute.Type.values();
		String []items = new String[types.length];
		for(int i=0; i<types.length; i++)
		{
			items[i] = types[i].name();
		}
		return items;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "(" + enable + "," + filterType + ")";
	}
}
