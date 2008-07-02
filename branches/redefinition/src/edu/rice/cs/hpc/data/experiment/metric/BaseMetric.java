/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public abstract class BaseMetric {

	//-------------------------------------------------------------------------------
	// DATA
	//-------------------------------------------------------------------------------
	/** The short name of this metric, used within an experiment's XML file. */
	protected String shortName;

	/** The native (target OS toolset) name of this metric. */
	protected String nativeName;

	/** The user-visible name of this metric. */
	protected String displayName;

	/** Whether this metric should be displayed. */
	protected boolean displayed;

	/** Whether this metric's display should include a percentage. */
	protected boolean percent;

	/** The index of this metric in its experiment's metric list. */
	protected int index;

	/** The display format to be used for this metric. */
	protected MetricValueFormat displayFormat;

	protected MetricType metricType;

	//-------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-------------------------------------------------------------------------------
	public BaseMetric(String sDisplayName, boolean displayed, boolean percent, int index) {
		this.displayName = sDisplayName;
		this.displayed = displayed;
		this.percent = percent;
		this.index = index;
		
		// format
		this.displayFormat = (this.percent ? MetricValueFormat.DEFAULT_PERCENT
		                                   : MetricValueFormat.DEFAULT_NOPERCENT);

	}
	//-------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------
	/*************************************************************************
	 *	Sets the metric's index.
	 ************************************************************************/
		
	public void setIndex(int index)
	{
		this.index = index;
	}

	/*************************************************************************
	 *	Returns the metric's index.
	 ************************************************************************/
		
	public int getIndex()
	{
		return this.index;
	}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO METRIC													//
//////////////////////////////////////////////////////////////////////////
	/*************************************************************************
	 *	Returns the metric's short (internal) name.
	 ************************************************************************/
	public String getShortName()
	{
		return this.shortName;
	}

	public void setShortName(String newName)
	{
		this.shortName = newName;
	}

	/*************************************************************************
	 *	Returns the metric's native (target OS toolset) name.
	 ************************************************************************/
	public String getNativeName()
	{
		return this.nativeName;
	}

	/*************************************************************************
	 *	Returns the metric's user-visible name.
	 ************************************************************************/
	public String getDisplayName()
	{
		return this.displayName;
	}	

	/*************************************************************************
	 *	Returns whether the metric should be displayed.
	 ************************************************************************/	
	public boolean getDisplayed()
	{
		return this.displayed;
	}

	/**
	 * Set display flag (true=to be displayed)
	 * @param d, the flag
	 */
	public void setDisplayed(boolean d)
	{
		this.displayed = d;
	}

	/*************************************************************************
 	*	Returns whether the metric's display should include a percentage value.
 	************************************************************************/
	public boolean getPercent()
	{
		return this.percent;
	}

	/**
	 * method to return the value of a given scope. To be implemented by derived class.
	 * @param s
	 * @return
	 */
	abstract public MetricValue getValue(Scope s);
	
	/**
	 * Return the text to display based on the value of the scope
	 * @param scope
	 * @return
	 */
	public String getMetricTextValue(Scope scope) {
		MetricValue mv = this.getValue(scope);
		String sText;
		if(mv.getPercentValue() == 0.0) sText = "";
		else{
				sText = getDisplayFormat().format(mv);
		}
		return sText;
	}

	/*************************************************************************
	 *	Sets the metric's display format.
	 ************************************************************************/
	public void setDisplayFormat(MetricValueFormat displayFormat)
	{
		this.displayFormat = displayFormat;
	}

	/*************************************************************************
	 *	Returns the metric's display format.
	 ************************************************************************/
	public MetricValueFormat getDisplayFormat()
	{
		return this.displayFormat;
	}

	/*************************************************************************
	 *	MISC
	 ************************************************************************/
	public MetricType getMetricType()
	{
		return this.metricType;
	}

}
