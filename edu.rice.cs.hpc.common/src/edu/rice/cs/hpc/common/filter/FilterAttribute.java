package edu.rice.cs.hpc.common.filter;

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
	static public enum Type {Inclusive, Exclusive};
	
	/***
	 * Flag true: the filter is enabled
	 * Flag false: disabled
	 */
	public Boolean enable  = true;
	
	/*****
	 * @see Type
	 */
	public Type filterType = Type.Inclusive;
	
	/*****
	 * get the name of the filter
	 * 
	 * @return
	 */
	public String getFilterType() 
	{
		return filterType.name();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "(" + enable + "," + filterType + ")";
	}
}
