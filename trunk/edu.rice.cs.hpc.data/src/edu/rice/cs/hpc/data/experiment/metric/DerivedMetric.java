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

	private RootScope root;
	
	//===================================================================================
	// CONSTRUCTORS
	//===================================================================================
	
	/**
	 * Extended derived metric which is based on a formula of expression
	 * @param scopeRoot
	 * @param e
	 * @param sName
	 * @param sID
	 * @param index
	 * @param annotationType
	 * @param objType
	 */
	public DerivedMetric(RootScope scopeRoot, Expression e, String sName, String sID, int index, AnnotationType annotationType, MetricType objType) {
		super(sID, sName, true, null, annotationType, index, objType);
		
		assert( scopeRoot.getExperiment() instanceof Experiment);
		
		this.expression = e;

		// set up the functions
		this.fctMap = new ExtFuncMap();
		
		root = scopeRoot;
		
		final Experiment experiment = (Experiment)scopeRoot.getExperiment();
		BaseMetric []metrics = experiment.getMetrics(); 
		this.fctMap.init(metrics);

		// set up the variables
		this.varMap = new MetricVarMap(experiment);

		// Bug fix: always compute the aggregate value 
		this.dRootValue = this.getAggregateMetrics(scopeRoot);
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
	}

//-------------------- MAIN FUNCTION FOR COMPUTING AGGREGATE VALUE ------------------
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
			dVal = getAggregateMetrics( (RootScope) scope );
			// reset the new value of root
			dRootValue = dVal;
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
		return new DerivedMetric(root, expression, displayName, shortName, index, annotationType, metricType);
	}
	
}
