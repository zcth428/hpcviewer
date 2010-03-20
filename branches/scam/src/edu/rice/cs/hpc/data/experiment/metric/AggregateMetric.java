package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
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
	private MetricVarMap finalizeVarMap;
	private CombineAggregateMetricVarMap combineVarMap;

	/***------------------------------------------------------------------------****
	 * Constructor: create a derived incremental metric
	 * @param sID: the ID of the metric (HAS TO BE REALLY SHORT !!!)
	 * @param sDisplayName: the title of the metric
	 * @param displayed: to show or not ?
	 * @param percent  : to show the percent ?
	 * @param index    : metric index in the list (unused)
	 * @param type	   : metric type
	 ***------------------------------------------------------------------------***/
	public AggregateMetric(String sID, String sDisplayName, boolean displayed, String format,
			boolean percent, int index, MetricType type) {
		super(sID, sDisplayName, displayed, format, percent, index);

		this.fctMap = new FuncMap();
		this.fctMap.loadDefaultFunctions();
		
		// set up the variables
		this.finalizeVarMap = new MetricVarMap();
		this.combineVarMap = new CombineAggregateMetricVarMap();

		this.metricType = type;
	}

	@Override
	public MetricValue getValue(Scope s) {
		MetricValue mv = null;
		mv = s.getMetricValue(this.index);
		return mv;
	}


	/****------------------------------------------------------------------------****
	 * set the math expression
	 * @param type
	 * @param sFormula
	 ***------------------------------------------------------------------------****/
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
	
	
	/*****------------------------------------------------------------------------****
	 * initialize the metric.
	 * THIS METHOD HAS TO BE CALLED before asking the value
	 * @param type
	 * @param exp
	 ***------------------------------------------------------------------------****/
	public void init(Experiment exp) {
		this.finalizeVarMap.setExperiment(exp);
		this.combineVarMap.setExperiment(exp);
	}
	
	
	/****------------------------------------------------------------------------****
	 * Assign the value of a scope based on the formula of a given type
	 * @param type
	 * @param scope
	 ***------------------------------------------------------------------------****/
	public void finalize(Scope scope) {
		Expression exp = this.formulaFinalize;
		
		if (exp != null) {
			this.finalizeVarMap.setScope(scope);
			this.setScopeValue(exp, this.finalizeVarMap, scope);
		}
	}
	
	/**------------------------------------------------------------------------****
	 * combining the metric from another view (typically cct) to this view
	 * if the target metric is not available (or empty) then we initialize it with
	 * 	the value of the source
	 * @param s_source
	 * @param s_target
	 **------------------------------------------------------------------------****/
	public void combine(Scope s_source, Scope s_target) {
		MetricValue value = s_target.getMetricValue(this); 
		if (value.isAvailable()) {
			//--------------------------------------------------------------------------
			// the target has the metric. we need to "combine" it with the source
			//--------------------------------------------------------------------------
			Expression expression = this.formulaCombine;
			if (expression != null) {
				this.combineVarMap.setScopes(s_source, s_target);
				this.setScopeValue(expression, this.combineVarMap, s_target);
			}
		} else {
			//--------------------------------------------------------------------------
			// the target doesn't have the metric. we need to copy from the source
			//--------------------------------------------------------------------------
			MetricValue v_source = s_source.getMetricValue(this);
			s_target.setMetricValue(index, v_source);
		}
	}
	
	
	/**------------------------------------------------------------------------****
	 * 
	 * @param expression
	 * @param var_map
	 * @param scope
	 **------------------------------------------------------------------------****/
	private void setScopeValue(Expression expression, MetricVarMap var_map, Scope scope) {
		MetricValue mv;
		try {
			double dValue = expression.eval(var_map, this.fctMap);
			mv = new MetricValue(dValue);
		} catch(java.lang.Exception e) {
			mv = MetricValue.NONE;
			e.printStackTrace();
		}
		scope.setMetricValue(this.index, mv);
	}
}
