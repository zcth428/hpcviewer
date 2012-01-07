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
	protected IMetricValueFormat displayFormat;

	protected MetricType metricType;

	protected double  sampleperiod;

	private char unit;

	//-------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-------------------------------------------------------------------------------


	/*************************************************************************
	 * 
	 * @param sID: Unique ID of the metric
	 * @param sDisplayName: the name of the title
	 * @param displayed: will metric be displayed ?
	 * @param format: format of the display
	 * @param percent: show the percent ?
	 * @param index: index in the table
	 *************************************************************************/
	public BaseMetric(String sID, String sDisplayName, boolean displayed, String format, 
			boolean percent, int index, MetricType type) {
		this.displayName = sDisplayName + "   "; // johnmc - hack to leave enough room for ascending/descending triangle;
		this.displayed = displayed;
		this.percent = percent;
		this.index = index;

		// format
		if (format == null) {
			this.displayFormat = (this.percent ? MetricValueFormat.DEFAULT_PERCENT
					: MetricValueFormat.DEFAULT_NOPERCENT);			
		} else {
			this.displayFormat = new MetricValuePredefinedFormat(format);
		}

		this.unit = '0';
		this.shortName = sID;

		this.metricType = type;
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

	//=================================================================================
	//		ACCESS TO METRIC
	//=================================================================================

	/*************************************************************************
	 *	Returns the metric's short (internal) name.
	 ************************************************************************/
	public String getShortName()
	{
		return this.shortName;
	}

	/*************************************************************************
	 * 
	 * @param newName
	 *************************************************************************/
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

	/*************************************************************************
	 * Set display flag (true=to be displayed)
	 * @param d, the flag
	 *************************************************************************/
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
	 * Return the text to display based on the value of the scope
	 * @param scope
	 * @return
	 */
	public String getMetricTextValue(Scope scope) {
		MetricValue mv = this.getValue(scope);
		// bug fix: if the percent has to be displayed BUT the value is zero, then display nothing
		return this.getMetricTextValue(mv);
	}

	/*************************************************************************
	 * Return the text to display based on the metric value
	 * @param mv: the value of a metric
	 *************************************************************************/
	public String getMetricTextValue(MetricValue mv) {
		String sText;
		if(mv.value == 0.0 || mv == MetricValue.NONE || !MetricValue.isAvailable(mv) ) sText = "";
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
	public IMetricValueFormat getDisplayFormat()
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

	/*************************************************************************
	 * 
	 * @param objType
	 *************************************************************************/
	public void setMetricType( MetricType objType ) 
	{
		this.metricType = objType;
	}
	/*************************************************************************
	 * Laks: need an interface to update the sample period due to change in DTD
	 * @param s
	 *************************************************************************/
	public void setSamplePeriod(String s) {
		this.sampleperiod = this.convertSamplePeriode(s);
	}

	/*************************************************************************
	 * 
	 * @param sUnit
	 *************************************************************************/
	public void setUnit (String sUnit) {
		this.unit = sUnit.charAt(0);
		if (this.isUnitEvent())
			this.sampleperiod = 1.0;
	}

	/*************************************************************************
	 * Return the sample period
	 * @return
	 *************************************************************************/
	public double getSamplePeriod()
	{
		return this.sampleperiod;
	}

	//=================================================================================
	//		ABSTRACT METHODS
	//=================================================================================
	/*************************************************************************
	 * method to return the value of a given scope. To be implemented by derived class.
	 * @param s
	 * @return
	 *************************************************************************/
	abstract public MetricValue getValue(Scope s);



	//=================================================================================
	//		UTILITY METHODS
	//=================================================================================

	/**
	 * Verify if the unit is an event or not
	 * @return
	 */
	private boolean isUnitEvent() {
		return this.unit == 'e';
	}

	/**
	 * convert the input sample period into a double, depending of the unit
	 * @param sPeriod
	 * @return
	 */
	protected double convertSamplePeriode( String sPeriod ) {
		if (this.isUnitEvent())
			return 1.0;

		double period = 1.0;
		if (sPeriod != null && sPeriod.length()>0) {
			try {
				period = Double.parseDouble(sPeriod);
			} catch (java.lang.NumberFormatException e) {
				System.err.println("The sample period is incorrect :" + this.sampleperiod);
			}
		}
		return period;
	}

}
