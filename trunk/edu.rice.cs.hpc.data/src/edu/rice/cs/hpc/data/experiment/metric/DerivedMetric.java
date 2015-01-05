/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.*;

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

	// formula expression
	private Expression expression;
	// the total aggregate value
	private double dRootValue = 0.0;
	// map function
	private ExtFuncMap fctMap;
	// map variable 
	private MetricVarMap varMap;

	private Experiment experiment;
	
	//===================================================================================
	// CONSTRUCTORS
	//===================================================================================
	

	/*****
	 * Create derived metric based on experiment data. We'll associate this metric with the root scope of CCT
	 * <p/>
	 * A metric should be independent to root scope. The root scope is only used to compute the percentage
	 * 
	 * @param experiment
	 * @param e
	 * @param sName
	 * @param sID
	 * @param index
	 * @param annotationType
	 * @param objType
	 */
	public DerivedMetric(Experiment experiment, Expression e, String sName, String sID, int index, AnnotationType annotationType, MetricType objType) {
		
		// no root scope information is provided, we'll associate this metric to CCT root scope 
		super(sID, sName, true, null, annotationType, index, objType);
		
		this.expression = e;
		this.experiment = experiment;
		
		// set up the functions
		this.fctMap = new ExtFuncMap();
		
		RootScope root = (RootScope) experiment.getRootScope().getSubscope(0);
		
		BaseMetric []metrics = experiment.getMetrics(); 
		this.fctMap.init(metrics);

		// set up the variables
		this.varMap = new MetricVarMap(experiment);

		// Bug fix: always compute the aggregate value 
		this.dRootValue = getAggregateMetrics(root);
		if(this.dRootValue == 0.0)
			this.annotationType = AnnotationType.NONE ;
	}
	
	/****
	 * Set the new expression
	 * 
	 * @param expr : the new expression
	 */
	public void setExpression( Expression expr ) {
		this.expression = expr;
		
		// new formula has been set, refresh the root value used for computing percent
		RootScope root = (RootScope) experiment.getRootScope().getSubscope(0);
		dRootValue = getAggregateMetrics(root);
	}

	// -----------------------------------------------------------------------
	//	For the time being, the aggregate value will be computed point-wise, just like
	//	the way scopes are computed.
	// -----------------------------------------------------------------------
	/**
	 * Compute the general aggregate metric for cct, caller tree and flat tree
	 * @param scopeRoot
	 * @return the aggregate value
	 */
	private double getAggregateMetrics(RootScope scopeRoot) {
		try {
			Double objSum = this.getDoubleValue(scopeRoot);
			if (objSum != null)
				return objSum.doubleValue();
		} catch (Exception e) {
			// invalid metric ?
		}
		return Double.MIN_VALUE;
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
			dVal = dRootValue;
		} else {
			// otherwise, we need to recompute the value again via the equation
			Double objVal = this.getDoubleValue(scope);
			if(objVal == null)
				return MetricValue.NONE;	// the value is not available !
			dVal = objVal.doubleValue();
		}
		if(this.getAnnotationType() == AnnotationType.PERCENT){
			return new MetricValue(dVal, ((float) dVal/this.dRootValue));
		} else {
			return new MetricValue(dVal);
		}
	}

	/****
	 * return the current expression formula
	 * 
	 * @return
	 */
	public Expression getFormula() {
		return expression;
	}

	//@Override
	public BaseMetric duplicate() {
		return new DerivedMetric(experiment, expression, displayName, shortName, index, annotationType, metricType);
	}
	
}
