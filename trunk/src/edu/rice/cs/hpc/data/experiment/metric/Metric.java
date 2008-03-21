//////////////////////////////////////////////////////////////////////////
//																		//
//	Metric.java															//
//																		//
//	experiment.Metric -- a metric and its data in an experiment			//
//	Last edited: January 15, 2002 at 12:37 am							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.metric;


import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;




//////////////////////////////////////////////////////////////////////////
//	CLASS METRIC														//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A metric and its data in an HPCView experiment.
 *
 */


public class Metric extends Object
{


/** The experiment owning this metric. */
protected Experiment experiment;

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

protected String  sampleperiod;

/** The index of this metric in its experiment's metric list. */
protected int index;

/** The display format to be used for this metric. */
protected MetricValueFormat displayFormat;

protected MetricType metricType;

protected int partnerIndex;

public final static int NO_PARTNER_INDEX = -1;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a Metric.
 ************************************************************************/
	
public Metric(Experiment experiment,
              String shortName, String nativeName, String displayName,
              boolean displayed, boolean percent,String sampleperiod, MetricType metricType, 
              int partnerIndex)
{
	// creation arguments
	this.experiment  = experiment;
	this.shortName   = shortName;
	this.nativeName  = nativeName;
	this.displayName = displayName + "   "; // johnmc - hack to leave enough room for ascending/descending triangle
	this.displayed   = displayed;
	this.percent     = percent;
    this.sampleperiod  = sampleperiod;
    this.metricType     = metricType;
    this.partnerIndex = partnerIndex;
	
	// format
	this.displayFormat = (this.percent ? MetricValueFormat.DEFAULT_PERCENT
	                                   : MetricValueFormat.DEFAULT_NOPERCENT);
}




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

public String getSamplePeriod()
{
    return this.sampleperiod;
}

/*************************************************************************
 *	Returns the value of this metric at a given scope.
 ************************************************************************/
	
public MetricValue getValue(Scope s)
{
	return s.getMetricValue(this);
}




/*************************************************************************
 *	Returns the minimum value this metric can take.
 ************************************************************************/
	
public MetricValue getMinimumValue()
{
	return new MetricValue(0.0);
}




/*************************************************************************
 *	Returns the maximum value this metric can take.
 ************************************************************************/
	
public MetricValue getMaximumValue()
{
	return new MetricValue(Double.MAX_VALUE);
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


public MetricType getMetricType()
{
	return this.metricType;
}

public int getPartnerIndex()
{
	return this.partnerIndex;
}

public void setPartnerIndex(int ei)
{
	this.partnerIndex = ei;
}

}








