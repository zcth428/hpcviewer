/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.FuncMap;
import com.graphbuilder.math.VarMap;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author la5
 *
 */
public class MetricVarMap extends VarMap {

	private BaseMetric metrics[];// = new BaseMetric[2];
	private Scope scope;
	/**
	 * 
	 */
	public MetricVarMap() {
		super(false);
	}

	public MetricVarMap(Scope s) {
		super(false);
		this.scope = s;
	}
	
	public MetricVarMap(BaseMetric m[]) {
		super(false);
		this.metrics = m;
	}
	
	public MetricVarMap(Scope s, BaseMetric m[]) {
		super(false);
		this.scope = s;
		this.metrics = m;
	}
	/**
	 * @param caseSensitive
	 */
	public MetricVarMap(boolean caseSensitive) {
		super(caseSensitive);
		// TODO Auto-generated constructor stub
	}

	//===========================
	

	/**
	 * set the value for a metric variable (identified as $x) where x is the metric index
	 * @param iMetricID: the index of the metric
	 * @param metric: pointer to the metric
	 */
	public void setMetrics(BaseMetric []arrMetrics) {
		this.metrics = arrMetrics;
	}

	/**
	 * set the current scope which contains metric values
	 * @param s: the scope of node
	 */
	public void setScope(Scope s) {
		this.scope = s;
	}
	
	/**
	 * Overloaded method: a callback to retrieve the value of a variable (or a metric)
	 * If the variable is a normal variable, it will call the parent method.		
	 */
	public double getValue(String varName) {
		//this.hasValidValue = true;
		if(varName.startsWith("$")) {
			// Metric variable
			String sIndex = varName.substring(1);
			try {
				int index = Integer.parseInt(sIndex);
				if(index<this.metrics.length) {
					BaseMetric metric = this.metrics[index];
					MetricValue value = metric.getValue(scope);
					if(value.isAvailable())
						return value.getValue();
					// Laks 2008.07.03: if the value is not available, we just assume it equals to zero
					else
						return 0;
					//throw new RuntimeException(varName);
					
				} else
					throw new RuntimeException("metric index is not valid: " + varName);
			} catch (java.lang.NumberFormatException e) {
				e.printStackTrace();
				return 0;
			}
		} else if (varName.startsWith("&")) {
			// pointer to metric variable
			String sIndex = varName.substring(1);
			if(sIndex.startsWith("$")) {
				// we want to enable users to declare a pointer as "&$1" or "&1"
				sIndex = sIndex.substring(1);
			}
			try{
				int index = Integer.parseInt(sIndex);
				return index;
			} catch (java.lang.NumberFormatException e) {
				e.printStackTrace();
				throw new RuntimeException(varName);
			}
		} else
			return super.getValue(varName);
	}
	
	// Laks: instead of having a valid value flag, we just throw an exception. 
	//		 this is simpler and less bugs (but the caller needs to intercept it)
	//private boolean hasValidValue;
	
	/**
	 * check if the expression, the scope and the metric have a valid value.
	 * To some cases, a metric has no value, and any arithmetric operation for
	 * void value is invalid.
	 * @return true if the value of the expression is valid.
	 */
	/*
	public boolean isValueValid() {
		return this.hasValidValue;
	}*/
	
	/**
	 * Unit test for MetricVarMap
	 * @param args
	 */
	public static void main(String[] args) {
		String s = "&1*r^2";
		Expression x = ExpressionTree.parse(s);

		MetricVarMap vm = new MetricVarMap(false /* case sensitive */);
		vm.setValue("r", 5);

		FuncMap fm = new FuncMap(); // no functions in expression
		fm.loadDefaultFunctions();
		System.out.println(x); 
		System.out.println(x.eval(vm, fm)); 

		vm.setValue("r", 10);
		System.out.println(x.eval(vm, fm)); 
	}

}
