package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;

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

	public AggregateMetric(String sID, String sDisplayName, boolean displayed,
			boolean percent, int index) {
		super(sID, sDisplayName, displayed, percent, index);

		this.fctMap = new FuncMap();
		this.fctMap.loadDefaultFunctions();
		
		// set up the variables
		this.varMap = new MetricVarMap();

	}

	@Override
	public MetricValue getValue(Scope s) {
		//assert ( this.formulaCurrent != null );
		
		MetricValue mv = null;
		/*
		this.varMap.setScope(s);
		try {
			double dValue = this.formulaCurrent.eval(this.varMap, this.fctMap);
			mv = new MetricValue(dValue);
		} catch(java.lang.Exception e) {
			mv = MetricValue.NONE;
		}
		*/
		mv = s.getMetricValue(this.index);
		return mv;
	}

	
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
	
	
	public void init(char type, BaseMetric []metrics) {
		this.varMap.setMetrics(metrics);
	}
	
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
