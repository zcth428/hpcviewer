/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.FuncMap;

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
	public ExtFuncMap(BaseMetric []metrics) {
		super(false);
		this.init(metrics);
	}

	public void init(BaseMetric []metrics) {

		StdDevFunction fctStdDev = new StdDevFunction();

		this.setFunction("stdev", fctStdDev);
		this.loadDefaultFunctions();
	}
}
