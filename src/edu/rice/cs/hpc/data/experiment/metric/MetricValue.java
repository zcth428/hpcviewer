//////////////////////////////////////////////////////////////////////////
//																		//
//	MetricValue.java													//
//																		//
//	experiment.MetricValue -- a value of a metric at some scope			//
//	Last edited: September 14, 2001 at 4:47 pm							//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.metric;


import edu.rice.cs.hpc.data.util.*;

import java.lang.Comparable;




//////////////////////////////////////////////////////////////////////////
//	CLASS METRIC-VALUE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A value of a metric at some scope.
 *
 */


public class MetricValue extends Object
implements
	Comparable
{


/** Whether the actual value is available. */
protected boolean available;

/** The actual value if available. */
protected double value;

/** Whether the percentage value is available. */
protected boolean percentAvailable;

/** The actual percentage value if available. */
protected double percent;




/** The distinguished metric value indicating no data. */
public static final MetricValue NONE = new MetricValue(-1);




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an unavailable MetricValue.
 ************************************************************************/
	
public MetricValue()
{
	this.available = false;
	this.percentAvailable = false;
}




/*************************************************************************
 *	Creates an available MetricValue with a given value and percentage value.
 ************************************************************************/
	
public MetricValue(double value, double percent)
{
	this.setValue(value);
	this.setPercentValue(percent);
}




/*************************************************************************
 *	Creates an available MetricValue with a given value.
 ************************************************************************/
	
public MetricValue(double value)
{
	this.setValue(value);
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO VALUE														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns whether the actual value is available.
 ************************************************************************/
	
public boolean isAvailable()
{
	return ( (this != MetricValue.NONE) && this.available && (this.value != 0.0) );
}




/*************************************************************************
 *	Returns the actual value if available.
 ************************************************************************/
	
public double getValue()
{
	Dialogs.Assert(this.available, "MetricValue::getValue");
	return this.value;
}




/*************************************************************************
 *	Makes the given actual value available.
 ************************************************************************/
	
public void setValue(double value)
{
	this.available = true;
	this.value = value;
}




/*************************************************************************
 *	Returns whether the percentage value is available.
 ************************************************************************/
	
public boolean isPercentAvailable()
{
	return (this != MetricValue.NONE) && this.percentAvailable;
}




/*************************************************************************
 *	Returns the percentage value if available.
 ************************************************************************/
	
public double getPercentValue()
{
	// Laks: I think it is normal not to have percent available
	//Dialogs.Assert(this.percentAvailable, "MetricValue::getPercentValue");
	return this.percent;
}




/*************************************************************************
 *	Makes the given percentage value available.
 ************************************************************************/
	
public void setPercentValue(double percent)
{
	this.percentAvailable = true;
	this.percent = percent;
}




/*************************************************************************
 *	Compares the metric value to another one.
 *
 *	Unavailable or nonexistent values are treated as less than available values.
 *	Percentage values are ignored.
 *
 ************************************************************************/
	
public int compareTo(Object other)
{
	Dialogs.Assert(other instanceof MetricValue, "MetricValue::compareTo");
	MetricValue otherMetricValue = (MetricValue) other;

	int result;
	
	if( this.isAvailable() && otherMetricValue.isAvailable() )
	{
		if( this.value > otherMetricValue.value )
			result = +1;
		else if( this.value < otherMetricValue.value )
			result = -1;
		else
			result = 0;
	}
	else if( this.isAvailable() )
		result = +1;
	else if( otherMetricValue.isAvailable() )
		result = -1;
	else
		result = 0;
		
	return result;
}




}








