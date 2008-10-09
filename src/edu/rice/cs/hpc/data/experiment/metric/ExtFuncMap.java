/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.FuncMap;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

/**
 * @author laksonoadhianto
 *
 */
public class ExtFuncMap extends FuncMap {

	/**
	 * 
	 */
	public ExtFuncMap() {
		// TODO Auto-generated constructor stub
		super(false);
	}

	/**
	 * @param caseSensitive
	 */
	public ExtFuncMap(boolean caseSensitive) {
		super(caseSensitive);
	}

	/**
	 * 
	 */
	public ExtFuncMap(BaseMetric []metrics, RootScope rootscope) {
		// TODO Auto-generated constructor stub
		super(false);
		this.init(metrics, rootscope);
	}

	public void init(BaseMetric []metrics, RootScope rootscope) {
		AggregateFunction fct = new AggregateFunction(metrics, rootscope);
		StdDevFunction fctStdDev = new StdDevFunction();
		this.setFunction("aggregate", fct);
		this.setFunction("stdev", fctStdDev);
		this.loadDefaultFunctions();
	}
}
