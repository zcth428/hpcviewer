/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.metric.MetricVarMap;

//math expression
import com.graphbuilder.math.*;

/**
 * @author la5
 *
 */
public class DerivedMetric extends BaseMetric {
	//===================================================================================
	// DATA
	//===================================================================================
	protected RootScope scopeOfTheRoot;
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
	 * Extended derived metric which is based on a formula of expression
	 * @param scopeRoot
	 * @param e
	 * @param sName
	 * @param index
	 * @param bPercent
	 */
	public DerivedMetric(RootScope scopeRoot, Expression e, String sName, int index, boolean bPercent, MetricType objType) {
		super(sName, true, bPercent, index);
		DerivedMetric.Counter++;
		this.expression = e;
		this.scopeOfTheRoot = scopeRoot;
		// set up the functions
		this.fctMap = new ExtFuncMap();
		BaseMetric []metrics = scopeRoot.getExperiment().getMetrics(); 
		this.fctMap.init(metrics, scopeRoot);

		// set up the variables
		this.varMap = new MetricVarMap(scopeRoot.getExperiment().getMetrics());
		this.metricType = objType;//MetricType.DERIVED;
		// Bug fix: always compute the aggregate value 
		//MetricValue objValue = this.getAggregateMetrics(scopeRoot);
		this.dRootValue = this.getAggregateMetrics(scopeRoot);
		if(this.dRootValue == 0.0)
			this.percent = false;
		//this.dRootValue = objValue.value;
	}

	//===================================================================================
	// AGGREGATE VALUE
	//===================================================================================
	
	/**
	 * Computing the aggregate values of the children and save it to the original "parent" 
	 * (which is the root scope)
	 * @param parent: the root scope
	 * @param current: current scope
	 * @param index: matrix index
	 * @return the value, MetricValue.NONE if there is no value
	 */
	private double computeAggregateBU_LeavesOnly(Scope current) {
		int nkids = current.getSubscopeCount();
		MetricValue objCurrentValue = this.getValue(current);//current.getDerivedMetricValue(this, index);
		double dTotal = 0.0;
		// ATT: we accumulate only the leaves, and DO NOT add the current value into the total value
		if(objCurrentValue.isAvailable() && (nkids==0)) {
			//dTotal = objCurrentValue.getValue();
			return objCurrentValue.value;
		}
		for (int i = 0; i < nkids; i++) {
			Scope child = current.getSubscope(i);
			// for debugging purpose
			String sChildName = child.getShortName();
			// compute the accumulated value of the children of the child
			double dTotalChildrenValue = computeAggregateBU_LeavesOnly(child);
			// compute the total
			if(dTotalChildrenValue != -1.0) {
				dTotal = dTotal + dTotalChildrenValue;
			}
		}
		// we have computed all the kids. If the total is zero, then return none
		// otherwise return the total
		double dTotalValue;
		if(dTotal != 0.0) {
			dTotalValue = dTotal;
		} else
			// the total value of the kids are zeros, but the current scope is not !
			dTotalValue = objCurrentValue.value; //MetricValue.NONE;
		return dTotalValue;
	}
	
	private double computeAggregateBU(Scope current) {
		int nkids = current.getSubscopeCount();
		double dTotal = 0.0;
		MetricValue mv = this.getValue(current);
		if(mv.isAvailable())
			dTotal = mv.value;
		else if(mv.value != -1.0)
			System.out.println("Aggregate check:"+mv.value);
		
		// sum the total with the children
		for (int i = 0; i < nkids; i++) {
			Scope child = current.getSubscope(i);
			// for debugging purpose
			String sChildName = child.getShortName();
			// compute the accumulated value of the children of the child
			double dTotalChildrenValue = computeAggregateBU(child);
			// compute the total
			if(dTotalChildrenValue != -1.0) {
				dTotal = dTotal + dTotalChildrenValue;
			}
		}
		return dTotal;
	}

/**
 * Compute the aggregate value for caller-tree
 * @param scopeRoot
 * @return
 */
	private double computeAggregateValueCT(RootScope scopeRoot) {
		if(this.metricType == MetricType.EXCLUSIVE)
			return this.computeAggregateBU(scopeRoot);
		
		// just get the one who has no children --> the main program
		int nbKids = scopeRoot.getSubscopeCount();
		for(int i=0; i<nbKids; i++) {
			Scope child = scopeRoot.getSubscope(i);
			if(child.getSubscopeCount() == 0) {
				return this.getValue(child).value;
			}
		}
		System.err.println("Computing aggregate value -- Warning: the tree has no main program ! Impossible to compute the aggregate value");
		return 0.0;
	}
	
	/**
	 * Compute the aggregate value for flat-tree
	 * @param scopeRoot
	 * @return
	 */
	private double computeAggregateValueFT(RootScope scopeRoot) {
		// exclusive: compute with bottom-up approach
		if (this.metricType == MetricType.EXCLUSIVE) {
			return this.computeAggregateBU(scopeRoot);
		} else {
			// temporary solution for inclusive: compute the aggregate value using math formula
			double dSum = this.getDoubleValue(scopeRoot).doubleValue();
			return dSum;
		}
	}
	
	private double computeAggregateValueCCT(RootScope scopeRoot) {
		if(this.metricType == MetricType.EXCLUSIVE) {
			return this.computeAggregateBU(scopeRoot);
		}  else {
			return this.computeAggregateBU_LeavesOnly(scopeRoot);
		}
	}
	/**
	 * Compute the general aggregate metric for cct, caller tree and flat tree
	 * @param scopeRoot
	 * @return the aggregate value
	 */
	private double getAggregateMetrics(RootScope scopeRoot) {
		if(scopeRoot.getType() == RootScopeType.CallerTree) {
			return computeAggregateValueCT(scopeRoot);
		} else if(scopeRoot.getType() == RootScopeType.Flat) {
			return computeAggregateValueFT(scopeRoot);
		} else {
			// calling context tree
			double dValue = this.computeAggregateValueCCT(scopeRoot);
			return dValue;
		}
	}
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
		double dVal;
		// if the scope is a root scope, then we return the aggregate value
		if(scope instanceof RootScope) {
			dVal = (this.dRootValue);
		} else {
			// otherwise, we need to recompute the value again via the equation
			Double objVal = this.getDoubleValue(scope);
			if(objVal == null)
				return MetricValue.NONE;	// the value is not available !
			dVal = objVal.doubleValue();
		}
		if(this.getPercent()){
			return new MetricValue(dVal,dVal/this.dRootValue);
		} else {
			return new MetricValue(dVal);
		}
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
	/*
	public double getAggregateValue() {
		if(this.dRootValue == 0.0)
			return this.getDoubleValue(this.scopeOfTheRoot).doubleValue();
		else
			return this.dRootValue;
	} */
	//===================================================================================
	// COMPARISON
	//===================================================================================
	/**
	 * Compare two derived values from two different scopes
	 * @param scope1
	 * @param scope2
	 * @return zero if the values are identical, <0 if the first is less, >0 otherwise
	 */
	/*
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
	}*/
}
