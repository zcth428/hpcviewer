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


public class Metric extends BaseMetric
{


/** The experiment owning this metric. */
protected Experiment experiment;

protected String  sampleperiod;

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
	super(displayName, displayed, percent, 0);
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
}


/**
 * Return the sample period
 * @return
 */
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

public int getPartnerIndex()
{
	return this.partnerIndex;
}

public void setPartnerIndex(int ei)
{
	this.partnerIndex = ei;
}

public Scope getRootScope() {
	return this.experiment.getRootScope();
}
}








