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

protected double  sampleperiod;

protected int partnerIndex;

public final static int NO_PARTNER_INDEX = -1;

private char unit;

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
	//this.displayName = displayName + "   "; // johnmc - hack to leave enough room for ascending/descending triangle
	//this.displayed   = displayed;
	//this.percent     = percent;
    this.sampleperiod  = this.convertSamplePeriode(sampleperiod);
    this.metricType     = metricType;
    this.partnerIndex = partnerIndex;
    this.unit = '0';
}

/**
 * Construct a metric using a predefined sample period
 * @param experiment
 * @param shortName
 * @param nativeName
 * @param displayName
 * @param displayed
 * @param percent
 * @param sampleperiod
 * @param metricType
 * @param partnerIndex
 */
public Metric(Experiment experiment,
        String shortName, String nativeName, String displayName,
        boolean displayed, boolean percent, double sampleperiod, MetricType metricType, 
        int partnerIndex)
{
super(displayName, displayed, percent, 0);
// creation arguments
this.experiment  = experiment;
this.shortName   = shortName;
this.nativeName  = nativeName;
this.sampleperiod  = sampleperiod;
this.metricType     = metricType;
this.partnerIndex = partnerIndex;
this.unit = '0';
}

public void setUnit (String sUnit) {
	this.unit = sUnit.charAt(0);
}

/**
 * Return the sample period
 * @return
 */
public double getSamplePeriod()
{
	return this.sampleperiod;
}

/**
 * Laks: need an interface to update the sample period due to change in DTD
 * @param s
 */
public void setSamplePeriod(String s) {
	this.sampleperiod = this.convertSamplePeriode(s);
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

// =================================================================================
// 			UTILITY METHODS
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
private double convertSamplePeriode( String sPeriod ) {
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








