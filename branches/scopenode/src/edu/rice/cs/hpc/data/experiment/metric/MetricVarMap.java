/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.FuncMap;
import com.graphbuilder.math.VarMap;
import com.graphbuilder.math.func.Function;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author la5
 *
 */
public class MetricVarMap extends VarMap {

	private Experiment experiment;
	private Scope scope;

	/**
	 * 
	 */
	public MetricVarMap() {
		super(false);
	}

	
	public MetricVarMap(Experiment exp) {
		super(false);
		this.experiment = exp;
	}
	
	public MetricVarMap(Scope s, Experiment exp) {
		super(false);
		this.scope = s;
		this.experiment = exp;
	}
	
	/**
	 * @param caseSensitive
	 */
	public MetricVarMap(boolean caseSensitive) {
		super(caseSensitive);
	}

	//===========================
	

	public void setExperiment(Experiment exp) {
		this.experiment = exp;
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
		assert(experiment != null);
		
		if(varName.startsWith("$")) {
			// Metric variable
			String sIndex = varName.substring(1);
			BaseMetric metric = this.experiment.getMetric(sIndex);
			if (metric == null) 
				throw new RuntimeException("metric doesn't exist: " + sIndex);
			if (scope != null) {
				MetricValue value = metric.getValue(scope);
				if(value.isAvailable())
					return value.getValue();
			}
			return 0.0;
			
		} else if (varName.startsWith("&")) {
			// pointer to metric variable
			String sIndex = varName.substring(1);
			if(sIndex.startsWith("$")) {
				// we want to enable users to declare a pointer as "&$1" or "&1"
				sIndex = sIndex.substring(1);
			}
			try{
				int index = this.experiment.getMetric(sIndex).getIndex();
				return index;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
				throw new RuntimeException(varName);
			}
		} else
			return super.getValue(varName);
	}
	
	
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
		Function []fs = fm.getFunctions();
		for( int i=0; i<fs.length ; i++) {
			System.out.println("\t<tr><td>" + " <code> " + fs[i].toString() + " </code> </td> <td></td> </tr>");
		}
	}

}
