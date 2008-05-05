/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author la5
 * Special metric to derive from other metric(s).
 * We need this special class because we want to compute "on the fly" the derived
 *  metric if necessary, instead of computing the whole value which may take times.
 */
public class DerivedMetric extends Metric {

	// derived metric operation
	static public final int ADD = 0;
	static public final int SUB = 1;
	static public final int MUL = 2;
	static public final int DIV = 3;
	static public final int NONE = -1;
	
	public Metric metric1 = null ;
	public Metric metric2 = null;
	public float coef1;
	public float coef2;
	public int operation=NONE;

	private double dRootValue;
	/**
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
	public DerivedMetric(Experiment experiment, String shortName,
			String nativeName, String displayName, boolean displayed,
			boolean percent, String sampleperiod, MetricType metricType,
			int partnerIndex) {
		super(experiment, shortName, nativeName, displayName, displayed,
				percent, sampleperiod, metricType, partnerIndex);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor for deriving from a single metric
	 * @param metric
	 * @param coef
	 */
	public DerivedMetric(RootScope scopeRoot, Metric metric, float coef) {
		// DUMMY constructor to keep compiler to be quiet
		// TODO: add new constructor in the parent
		super(metric.experiment, "*"+metric.shortName, "*"+metric.nativeName,
				"*"+metric.displayName,true,true,".", MetricType.DERIVED,metric.partnerIndex);
		this.metric1 = metric;
		this.coef1 = coef;
		this.setAggregateValue(scopeRoot);
	}
	
	/**
	 * Constructor for deriving from two metrics
	 * @param metric1
	 * @param coef1
	 * @param metric2
	 * @param coef2
	 * @param operation
	 */
	public DerivedMetric(RootScope scopeRoot, Metric metric1, float coef1, 
			Metric metric2, float coef2, int operation) {
		super(metric1.experiment, "*"+metric1.shortName, "*"+metric1.nativeName,
				"*"+metric1.displayName,true,true,".",MetricType.DERIVED,metric1.partnerIndex);
		this.metric1 = metric1;
		this.metric2 = metric2;
		this.coef1 = coef1;
		this.coef2 = coef2;
		this.operation = operation;
		this.setAggregateValue(scopeRoot);
	}
	
	/**
	 * Set the new name of the metric
	 * @param sName
	 */
	public void setName(String sName) {
		this.displayName = sName;
		// we need a convention of the short name of the matrix
		this.shortName = sName.substring(0, 1);
		this.nativeName = sName;
	}
	
	public void setPercent(boolean bPercentage) {
		this.percent = bPercentage;
	}
	
	private void setAggregateValue(RootScope scopeRoot) {
		 this.dRootValue = DerivedMetric.getValue(scopeRoot.getTreeNode().getScope(), this);
	}
	//========================
	// STATIC METHODS
	//========================

	/**
	 * Get the metric value
	 */
	static public double getValue(Scope scope, DerivedMetric metric) {
			// find the first operand
		double fResult = 0.0;
		Metric metricBase1 = metric.metric1;
		float fScale = metric.coef1;
		MetricValue value = scope.getMetricValue(metricBase1);
		// check if the base metric has a value and the scale factor is not zero
		if(value != MetricValue.NONE && fScale != 0) {
			double fVal = value.getValue() * fScale;
			Metric metricBase2 = metric.metric2;

			// check if we need the second operand
			if(metricBase2 != null) {
				float fScale2 = metric.coef2;
				value = scope.getMetricValue(metricBase2);
				double fVal2;
				if(value == MetricValue.NONE)
					fVal2 = 0;
				else
					fVal2 = scope.getMetricValue(metricBase2).getValue() * fScale2;
				
				// combine the two operations
				int opCode = metric.operation;
				switch(opCode) {
				case DerivedMetric.ADD:
					fResult = fVal + fVal2;
					break;
				case DerivedMetric.SUB:
					fResult = fVal - fVal2;
					break;
				case DerivedMetric.MUL:
					fResult = fVal * fVal2;
					break;
				case DerivedMetric.DIV:
					if(fVal2 != 0)
						fResult = fVal / fVal2;
					else
						fResult = 0; // this is not correct, but it's better than raising an exception
					break;
				}
			} else
				fResult = fVal;	// we have only one operand		
		}
	
		return fResult;
	}
	/**
	 * Get the text value of the derived metric based on the scope
	 * @param: scope is the current scope that the metric values
	 * @param metric: the derived metric
	 */
	static public String getTextValue(Scope scope, DerivedMetric metric) {
		String text = null; // we don't need this init
		// check if the arguments are valid
		if ((metric != null) && (scope != null)) {
			double fResult = DerivedMetric.getValue(scope, metric);
			if (fResult != 0.0) {
				MetricValue mv;
				if(metric.percent && metric.dRootValue != 0.0) {
					mv = new MetricValue(fResult, (double)fResult/metric.dRootValue);
				} else {
					mv = new MetricValue(fResult);
				}
				text = metric.getDisplayFormat().format(mv);
			}
		}
		return text;
	}
}
