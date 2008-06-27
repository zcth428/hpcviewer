/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.metric.MetricVarMap;
import edu.rice.cs.hpc.data.experiment.metric.*;

//math expression
import com.graphbuilder.math.*;

/**
 * @author la5
 *
 */
public class ExtDerivedMetric extends Metric {
	//===================================================================================
	// DATA
	//===================================================================================
	
	// a counter to know the number of derived metrics
	static public int Counter = 0;
	// formula expression
	private Expression expression;
	// the total aggregate value
	private double dRootValue = 0.0;
	//private MetricValue objAggregateValue = new MetricValue(0.0);
	// map function
	private ExtFuncMap fctMap;
	// map variable 
	private MetricVarMap varMap;

	//===================================================================================
	// CONSTRUCTORS
	//===================================================================================
	
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
	public ExtDerivedMetric(Experiment experiment, String shortName,
			String nativeName, String displayName, boolean displayed,
			boolean percent, String sampleperiod, MetricType metricType,
			int partnerIndex) {
		super(experiment, shortName, nativeName, displayName, displayed,
				percent, sampleperiod, metricType, partnerIndex);
		this.metricType = MetricType.DERIVED;
	}

	/**
	 * Extended derived metric which is based on a formula of expression
	 * @param scopeRoot
	 * @param e
	 * @param sName
	 * @param index
	 * @param bPercent
	 */
	public ExtDerivedMetric(RootScope scopeRoot, Expression e, String sName, int index, boolean bPercent, MetricType objType) {
		super(scopeRoot.getExperiment(), "EDM"+ExtDerivedMetric.Counter, "ExtDerivedMetric"+ExtDerivedMetric.Counter,
				sName, true, bPercent, ".",MetricType.DERIVED, 0);
		ExtDerivedMetric.Counter++;
		this.setIndex(index);
		this.expression = e;
		// set up the functions
		this.fctMap = new ExtFuncMap();
		this.fctMap.init(scopeRoot.getExperiment().getMetrics(), scopeRoot);

		// set up the variables
		this.varMap = new MetricVarMap(scopeRoot.getExperiment().getMetrics());
		this.metricType = objType;//MetricType.DERIVED;
		// compute the aggregate value if necessary
		if(bPercent) {
			MetricValue objValue = this.accumulateMetricsFromKids(scopeRoot, index);
			// save the aggregate value into global variable
			this.dRootValue = (!objValue.isAvailable()?0.0:objValue.getValue());
		}
	}

	//===================================================================================
	// AGGREGATE VALUE
	//===================================================================================
	
	/**
	 * Computing the aggregate value of a metric
	 */
	/*
	private MetricValue computeAggregate(Scope scopeAggregate, int index) {
		MetricValue objValue = new MetricValue();
		int nbKids = scopeAggregate.getSubscopeCount();
		double dTotal = 0.0;
		for(int i=0;i<nbKids;i++) {
			Scope kid = scopeAggregate.getSubscope(i);
			MetricValue objKidValue = this.accumulateMetricsFromKids(kid, index);
			if(objKidValue.isAvailable()) {
				dTotal = dTotal + objKidValue.getValue();
			}
		}
		if(dTotal != 0.0) {
			objValue = new MetricValue(dTotal);
		} else
			// if for unknown reason, the total aggregate (sum reduction) is zero,
			// we initialize it with the value of the scope (computed with the formula)
			objValue = this.getValue(scopeAggregate);
		return objValue;
	}*/
	/**
	 * Computing the aggregate values of the children and save it to the original "parent" 
	 * (which is the root scope)
	 * @param parent: the root scope
	 * @param current: current scope
	 * @param index: matrix index
	 * @return the value, MetricValue.NONE if there is no value
	 */
	private MetricValue accumulateMetricsFromKids(Scope current, int index) {
		int nkids = current.getSubscopeCount();
		MetricValue objCurrentValue = this.getValue(current);//current.getDerivedMetricValue(this, index);
		double dTotal = 0.0;
		// ATT: we accumulate only the leaves, and DO NOT add the current value into the total value
		if(objCurrentValue.isAvailable() && (nkids==0)) {
			//dTotal = objCurrentValue.getValue();
			return objCurrentValue;
		}
		for (int i = 0; i < nkids; i++) {
			Scope child = current.getSubscope(i);
			// for debuggin        g purpose
			String sChildName = child.getShortName();
			// compute the accumulated value of the children of the child
			MetricValue objTotalChildrenValue = accumulateMetricsFromKids(child, index);
			// compute the total
			if(objTotalChildrenValue.isAvailable()) {
				dTotal = dTotal + objTotalChildrenValue.getValue();// + objChildValue.getValue();
			}
		}
		// we have computed all the kids. If the total is zero, then return none
		// otherwise return the total
		MetricValue objTotalValue;
		if(dTotal != 0.0) {
			objTotalValue = new MetricValue(dTotal);
		} else
			// the total value of the kids are zeros, but the current scope is not !
			objTotalValue = objCurrentValue; //MetricValue.NONE;
		return objTotalValue;
	}
	/*
	public MetricValue getAggregateValue() {
		return this.objAggregateValue;
	}
	*/

	//===================================================================================
	// GET VALUE
	//===================================================================================
	/**
	 * Computing the value of the derived metric
	 * @param scope: the current scope
	 * @return the object Double if there is a value, null otherwise
	 */
	public Double getDoubleValue(Scope scope) {
		Double objResult = null;
		this.varMap.setScope(scope);
		try {
			double dValue = this.expression.eval(this.varMap, this.fctMap);
			objResult = new Double(dValue);
		} catch(java.lang.Exception e) {
			// should throw an exception
		}
		return objResult;
	}
	
	/**
	 * Overloading method to compute the value of the derived metric of a scope
	 * Return a MetricValue
	 */
	public MetricValue getValue(Scope scope) {
		Double dVal = this.getDoubleValue(scope);
		if(dVal == null)
			return MetricValue.NONE;	// the value is not available !
		return new MetricValue(dVal);
	}
	
	/**
	 * Retrieve the text value of the scope
	 * Notes: we put here instead of Scope class to avoid too much instantiation done in Scope class
	 * @param scope
	 * @return
	 */
	public String getTextValue(Scope scope) {
		MetricValue mv = this.getValue(scope); 
		if(!mv.isAvailable()) 
			return null;
		double dVal; 
		if((scope instanceof RootScope) && (this.dRootValue != 0.0)) {
			// FIXME: a dirty solution to find the aggregate value:
			// if scope is RootScope, it means we want to show the aggregate metrics
			dVal = this.dRootValue;
		} else {
			dVal = mv.getValue();
		}
		if(dVal == 0.0)
			return null;
		if(this.percent && this.dRootValue != 0.0) {
			mv = new MetricValue(dVal, (double)dVal/this.dRootValue);
		//} else {
		//	mv = new MetricValue(dVal);
		}
		return this.getDisplayFormat().format(mv);
	}
	
	/**
	 * Retrieve the aggregate value of this metric
	 * @return
	 */
	public double getAggregateValue() {
		if(this.dRootValue == 0.0)
			return this.getDoubleValue(this.experiment.getRootScope());
		else
			return this.dRootValue;
	}
	//===================================================================================
	// COMPARISON
	//===================================================================================
	/**
	 * Compare two derived values from two different scopes
	 * @param scope1
	 * @param scope2
	 * @return zero if the values are identical, <0 if the first is less, >0 otherwise
	 */
	public int compare(Scope scope1, Scope scope2) {
		int iResult = 0;
		MetricValue mv1 = this.getValue(scope1); 	//scope1.getDerivedMetricValue(this);
		MetricValue mv2 = this.getValue(scope2);	//scope2.getDerivedMetricValue(this);
		if(mv1.isAvailable() || mv2.isAvailable()) {
			if(!mv1.isAvailable()) {
				// only mv2 is available
				return 1;
			}
			if(!mv2.isAvailable()) {
				// only mv1 is available
				return -1;
			}
			// compare both values
			double d1 = mv1.getValue();
			double d2 = mv2.getValue();
			// attention: we treat 0.0 as value none !
			if(d1 == d2)
				return 0;
			// if one the value is zero, the other has higher priority, regardless the value
			if(d1 == 0.0 && d2 != 0.0)
				return 1;
			if(d2 == 0.0 && d1 != 0.0)
				return -1;
			// simple comparison.
			return(int) (d2-d1);
		}
		return iResult;
	}
}

