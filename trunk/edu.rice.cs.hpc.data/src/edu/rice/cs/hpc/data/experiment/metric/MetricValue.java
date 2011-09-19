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




//////////////////////////////////////////////////////////////////////////
//	CLASS METRIC-VALUE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A value of a metric at some scope.
 *
 */


public final class MetricValue 
{

	/** The actual value if available. */
	protected float value;
	
	/** The actual percentage value if available. */
	protected float percent;
	
	protected byte flags;
	
	protected static final byte VALUE_IS_AVAILABLE = 1;
	protected static final byte PERCENT_IS_AVAILABLE = 2;
	
	/** Whether the actual value is available. */
	// protected boolean available;

	/** Whether the percentage value is available. */
	// protected boolean percentAvailable;


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
	setAvailable(this, false);
	setPercentAvailable(this, false);
}




/*************************************************************************
 *	Creates an available MetricValue with a given value and percentage value.
 ************************************************************************/
	
public MetricValue(double value, double percent)
{
	setValue(this, value);
	setPercentValue(this, ((float)percent));
}




/*************************************************************************
 *	Creates an available MetricValue with a given value.
 ************************************************************************/
	
public MetricValue(double value)
{
	setValue(this, value);
	setAvailable(this, true);
	setPercentAvailable(this, false);
}



//////////////////////////////////////////////////////////////////////////
//	ACCESS TO VALUE														//
//////////////////////////////////////////////////////////////////////////

/*************************************************************************
 *	Returns whether the actual value is available.
 ************************************************************************/
	
private static boolean getAvailable(MetricValue m)
{
	boolean available = (m.flags & VALUE_IS_AVAILABLE) == VALUE_IS_AVAILABLE;
	return available;
}

private static void setAvailable(MetricValue m, boolean status)
{
	if (status) {
		m.flags |= VALUE_IS_AVAILABLE;
	} else {
		m.flags &= ~VALUE_IS_AVAILABLE;
	}	
}


private static boolean getPercentAvailable(MetricValue m)
{
	boolean available = (m.flags & PERCENT_IS_AVAILABLE) == PERCENT_IS_AVAILABLE;
	return available;
}

private static void setPercentAvailable(MetricValue m, boolean status)
{
	if (status) {
		m.flags |= PERCENT_IS_AVAILABLE;
	} else {
		m.flags &= ~PERCENT_IS_AVAILABLE;
	}	
}

/*************************************************************************
 *	Returns whether the metric value is available.
 ************************************************************************/
	
public static boolean isAvailable(MetricValue m)
{
	return ( (m != MetricValue.NONE) && getAvailable(m) && (m.value != 0.0) );
}




/*************************************************************************
 *	Returns the actual value if available.
 ************************************************************************/
	
public static double getValue(MetricValue m)
{
	boolean available = (m.flags & VALUE_IS_AVAILABLE) == VALUE_IS_AVAILABLE;
	Dialogs.Assert(available, "MetricValue::getValue");
	return m.value;
}




/*************************************************************************
 *	Makes the given actual value available.
 ************************************************************************/
	
public static void setValue(MetricValue m, double value)
{
	setAvailable(m, true);
	m.value = (float) value;
}




/*************************************************************************
 *	Returns whether the percentage value is available.
 ************************************************************************/
	
public static boolean isPercentAvailable(MetricValue m)
{
	return (m != MetricValue.NONE) && getPercentAvailable(m);
}




/*************************************************************************
 *	Returns the percentage value if available.
 ************************************************************************/
	
public static float getPercentValue(MetricValue m)
{
	// Laks: I think it is normal not to have percent available
	//Dialogs.Assert(this.percentAvailable, "MetricValue::getPercentValue");
	return m.percent;
}




/*************************************************************************
 *	Makes the given percentage value available.
 ************************************************************************/

public static void setPercentValue(MetricValue m, double percent)
{
	setPercentAvailable(m, true);
	m.percent = (float) percent;
}

	
public static void setPercentValue(MetricValue m, float percent)
{
	setPercentAvailable(m, true);
	m.percent = percent;
}


public static boolean isZero(MetricValue m) 
{
	if (m != MetricValue.NONE) {
		return ( Double.compare(0.0, m.value) == 0 );
	}
	return true;
}

/*************************************************************************
 *	Compares the metric value to another one.
 *
 *	Unavailable or nonexistent values are treated as less than available values.
 *	Percentage values are ignored.
 *
 ************************************************************************/
	
public static int compareTo(MetricValue left, MetricValue right)
{
	int result;
	
	if( MetricValue.isAvailable(left) && MetricValue.isAvailable(right) )
	{
		if( left.value > right.value )
			result = +1;
		else if( left.value < right.value )
			result = -1;
		else
			result = 0;
	}
	else if( MetricValue.isAvailable(left) )
		result = +1;
	else if( MetricValue.isAvailable(right) )
		result = -1;
	else
		result = 0;
		
	return result;
}




}








