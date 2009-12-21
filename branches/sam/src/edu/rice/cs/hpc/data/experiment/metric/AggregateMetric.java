package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.metric.MetricVarMap;
import com.graphbuilder.math.FuncMap;

public class AggregateMetric extends BaseMetric {
 
	static public char FORMULA_COMBINE = 'c';
	static public char FORMULA_FINALIZE = 'f';
	
	// formula expression
	private Expression formulaCombine, formulaFinalize;
	//private Expression formulaCurrent = null;
	
	// map function
	private FuncMap fctMap;
	// map variable 
	private MetricVarMap varMap;

	/***
	 * Constructor: create a derived incremental metric
	 * @param sID: the ID of the metric (HAS TO BE REALLY SHORT !!!)
	 * @param sDisplayName: the title of the metric
	 * @param displayed: to show or not ?
	 * @param percent  : to show the percent ?
	 * @param index    : metric index in the list (unused)
	 */
	public AggregateMetric(String sID, String sDisplayName, boolean displayed,
			boolean percent, int index) {
		super(sID, sDisplayName, displayed, percent, index);

		this.fctMap = new FuncMap();
		this.fctMap.loadDefaultFunctions();
		
		// set up the variables
		this.varMap = new MetricVarMap();
		// laksono hack 2009: we force this class of type DERIVED_INCR ... yuck !
		//this.metricType = MetricType.DERIVED_INCR;
	}

	@Override
	public MetricValue getValue(Scope s) {
		MetricValue mv = null;
		mv = s.getMetricValue(this.index);
		return mv;
	}


	/**
	 * set the math expression
	 * @param type
	 * @param sFormula
	 */
	public void setFormula(char type, String sFormula) {
		assert (type == FORMULA_COMBINE || type == FORMULA_FINALIZE);
		
		try {
			if (type == FORMULA_COMBINE) {
				formulaCombine = ExpressionTree.parse(sFormula);				
			} else {
				formulaFinalize = ExpressionTree.parse(sFormula);
			}
		} catch (ExpressionParseException e) {
			e.printStackTrace();
		}
	}
	
	
	/***
	 * initialize the metric.
	 * THIS METHOD HAS TO BE CALLED before asking the value
	 * @param type
	 * @param exp
	 */
	public void init(char type, Experiment exp) {
		this.varMap.setxperiment(exp);
	}
	
	
	/**
	 * Assign the value of a scope based on the formula of a given type
	 * @param type
	 * @param scope
	 */
	public void setScopeValue(char type, Scope scope) {
		Expression exp;
		
		if (type == FORMULA_COMBINE) exp = this.formulaCombine;
		else exp = this.formulaFinalize;
		
		if (exp != null) {
			this.varMap.setScope(scope);
			MetricValue mv;
			try {
				double dValue = exp.eval(this.varMap, this.fctMap);
				mv = new MetricValue(dValue);
			} catch(java.lang.Exception e) {
				mv = MetricValue.NONE;
			}
			scope.setMetricValue(this.index, mv);
		}
	}
}
