package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.Vector;

/**Representation of a call path for one sample.*/
public class CallStackSample 
{
	/**The actual names of the functions stored in this CallStackSample.*/
	Vector<String> sampleNames;
	
	/**A null function - used for debugging.*/
	public static final String NULL_FUNCTION = "-Outside Timeline-";
	
	/**Creates a new CallStackSample.*/
	public CallStackSample() 
	{
		sampleNames = new Vector<String>();
	}
	
	/**Gets the function name at the maximum depth of this sample.*/
	public String getDeepestFunctionName() 
	{
		return sampleNames.elementAt(sampleNames.size()-1);
	}
	
	/**Adds a function to this sample.*/
	public void addFunction(String name) 
	{
		sampleNames.addElement(name);
	}
	
	/**Gets the name of the function at depth 'depth'.*/
	public String getFunctionName(int depth)
	{
		return sampleNames.elementAt(depth);
	}
	
	/**Returns the Vector containing all of the names.*/
	public Vector<String> getNames()
	{
		return this.sampleNames;
	}
	
	/**Returns the depth of this CallStackSample.*/
	public int getSize()
	{
		return sampleNames.size();
	}
	
	/***********************************************************************
	 * Checks to see if the contents of this CallStackSample are equal
	 * to the contents of the CallStackSample other.
	 **********************************************************************/
	public boolean equals(Object other)
	{
		CallStackSample o = (CallStackSample)other;
		return (sampleNames.equals(o.sampleNames));
	}
}
