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


public class Metric extends BaseMetric
{

protected int partnerIndex;

public final static int NO_PARTNER_INDEX = -1;


//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a Metric.
 ************************************************************************/
	
public Metric(String shortName, String nativeName, String displayName,
              boolean displayed, String format, boolean percent,String sampleperiod, MetricType metricType, 
              int partnerIndex)
{
	super(shortName, displayName, displayed, format, percent, 0, metricType);
	// creation arguments
	this.nativeName  = nativeName;
    this.sampleperiod  = this.convertSamplePeriode(sampleperiod);
    this.metricType     = metricType;
    this.partnerIndex = partnerIndex;
}


/**
 * Construct a metric using a predefined sample period
 * @param shortName
 * @param nativeName
 * @param displayName
 * @param displayed
 * @param format
 * @param percent
 * @param sampleperiod
 * @param metricType
 * @param partnerIndex
 */
public Metric( String shortName, String nativeName, String displayName,
        boolean displayed, String format, boolean percent, double sampleperiod, MetricType metricType, 
        int partnerIndex)
{
super(shortName, displayName, displayed, format, percent, 0, metricType);
// creation arguments
this.nativeName  = nativeName;
this.sampleperiod  = sampleperiod;
this.metricType     = metricType;
this.partnerIndex = partnerIndex;
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

public int getPartnerIndex()
{
	return this.partnerIndex;
}

public void setPartnerIndex(int ei)
{
	this.partnerIndex = ei;
}

}








