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
	/** Valid types of Annotations to be used with metric values */
	public enum AnnotationType { NONE, PERCENT, PROCESS };

	/** The short name of this metric, used within an experiment's XML file. */
	protected String shortName;

	/** The native (target OS toolset) name of this metric. */
	protected String nativeName;

	/** The user-visible name of this metric. */
	protected String displayName;

	/** Whether this metric should be displayed. */
	protected boolean displayed;

	/** The type of annotation that should be displayed with this metric (percent or process number). */
	protected AnnotationType annotationType = AnnotationType.NONE;

	/** The index of this metric in its experiment's metric list. */
	protected int index;
	// partner of the metric. If the metric is exclusive, then its partner is the inclusive one
	protected int partner_index;

	/** The display format to be used for this metric. */
	protected IMetricValueFormat displayFormat;

	protected MetricType metricType;

	protected double  sampleperiod;

	private char unit;

	final private String EMPTY_SUFFIX = "   ";
	//-------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-------------------------------------------------------------------------------


	/*************************************************************************
	 * 
	 * @param sID: Unique ID of the metric
	 * @param sDisplayName: the name of the title
	 * @param displayed: will metric be displayed ?
	 * @param format: format of the display
	 * @param annotationType: show the percent or process number ?
	 * @param index: index in the table
	 *************************************************************************/
	public BaseMetric(String sID, String sDisplayName, boolean displayed, String format, 
			AnnotationType annotationType, int index, int partner_index, MetricType type) 
	{
		// in case of instantiation from duplicate() method, we need to make sure there is
		//	no double empty suffix
		if (sDisplayName.endsWith(EMPTY_SUFFIX))
			this.displayName = sDisplayName;
		else
			this.displayName = sDisplayName + EMPTY_SUFFIX; // johnmc - hack to leave enough room for ascending/descending triangle;
		
		this.displayed = displayed;
		this.annotationType = annotationType;
		this.index = index;
		this.partner_index = partner_index;
		
		// format
		if (format == null) {
			if (annotationType == AnnotationType.PERCENT) {
				this.displayFormat = new MetricValueFormat(true, MetricValueFormat.FLOAT, 8, 2, true, MetricValueFormat.FIXED, 5, 1, "#0.0%", 1);
			} else if (annotationType == AnnotationType.PROCESS) {
				this.displayFormat = new MetricValueFormat(true, MetricValueFormat.FLOAT, 8, 2, true, MetricValueFormat.FIXED, 5, 0, "<0>", 1);
			} else {
				this.displayFormat = new MetricValueFormat(true, MetricValueFormat.FLOAT, 8, 2, false, 0, 0, 0, null, 1);
			}
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

	
	/*****
	 * get the partner metric index
	 * @return
	 */
	public int getPartner() {
		return partner_index;
	}
	

	public void setPartner(int ei)
	{
		this.partner_index = ei;
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
	 * set the new display name
	 * @param name
	 *************************************************************************/
	public void setDisplayName(String name)
	{
		displayName = name;
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
	 *	Returns the type of annotation a metric's display should include (percent, process number, others ??).
	 ************************************************************************/
	public AnnotationType getAnnotationType()
	{
		return this.annotationType;
	}

	public void setAnnotationType( AnnotationType annType ) 
	{
		annotationType = annType;
	}
	
	/**
	 * Return the text to display based on the value of the scope
	 * @param scope
	 * @return
	 */
	public String getMetricTextValue(Scope scope) {
		MetricValue mv = this.getValue(scope);
		return this.getMetricTextValue(mv);
	}

	/*************************************************************************
	 * Return the text to display based on the metric value
	 * @param mv: the value of a metric
	 *************************************************************************/
	public String getMetricTextValue(MetricValue mv_) {
		String sText;
		MetricValue mv = mv_;
		
		// enforce bounds for presentation
		if (mv.value > 9.99e99) mv.value = Float.POSITIVE_INFINITY;
		else if (mv.value < -9.99e99) mv.value = Float.NEGATIVE_INFINITY;
		else if (Math.abs(mv.value) < 1.00e-99)  mv.value = (float) 0.0;
		
		// if not a special case, convert the number to a string
		if (mv.value == 0.0 || mv == MetricValue.NONE || !MetricValue.isAvailable(mv) ) sText = "";
		else if (mv.value == Float.POSITIVE_INFINITY) sText = "Infinity";
		else if (mv.value == Float.NEGATIVE_INFINITY) sText = "-Infinity";
		else if (Float.isNaN(mv.value)) sText = "NaN";
		else {
			sText = getDisplayFormat().format(mv);
		}
		
		return sText;
	}

	/*************************************************************************
	 * Sets the metric's display format.
	 * @param format
	 *************************************************************************/
	public void setDisplayFormat( IMetricValueFormat format )
	{
		this.displayFormat = format;
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

	/***
	 * Method to duplicate itself (cloning)
	 * @return
	 */
	abstract public BaseMetric duplicate();


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
				System.err.println("The sample period is incorrect :" + sPeriod);
			}
		}
		return period;
	}

}
