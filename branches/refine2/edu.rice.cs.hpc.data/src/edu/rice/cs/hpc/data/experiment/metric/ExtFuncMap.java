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

	/***
	 * construct list of function specifically for hpcdata
	 * @param metrics: list of metrics
	 * @param rootscope: a root scope (any root scope will do)
	 */
	public ExtFuncMap(BaseMetric []metrics, RootScope rootscope) {
		super(false);
		this.init(metrics, rootscope);
	}

	public void init(BaseMetric []metrics, RootScope rootscope) {
		//AggregateFunction fct = new AggregateFunction(metrics, rootscope);
		StdDevFunction fctStdDev = new StdDevFunction();

		//this.setFunction("@", fct);
		this.setFunction("stdev", fctStdDev);
		this.loadDefaultFunctions();
	}
}
