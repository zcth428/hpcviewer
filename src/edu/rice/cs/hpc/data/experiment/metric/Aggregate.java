/**
 * 
 */
package edu.rice.cs.hpc.data.experiment.metric;

import com.graphbuilder.math.func.Function;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

/**
 * @author laksonoadhianto
 *
 */
public class Aggregate implements Function {

	private Metric []arrMetrics;
	private RootScope rootscope;
	
	/**
	 * Retrieve the aggregate value of a metric
	 * @param metrics: a list of metrics
	 */
	public Aggregate(Metric []metrics, RootScope scope) {
		this.arrMetrics = metrics;
		this.rootscope = scope;
	}

	/* (non-Javadoc)
	 * @see com.graphbuilder.math.func.Function#acceptNumParam(int)
	 */
	public boolean acceptNumParam(int numParam) {
		// TODO Auto-generated method stub
		return (numParam == 1);
	}

	/* (non-Javadoc)
	 * @see com.graphbuilder.math.func.Function#of(double[], int)
	 */
	public double of(double[] param, int numParam) {
		// TODO Auto-generated method stub
		int index = (int) param[0];
		if(index > this.arrMetrics.length || index<0)
			throw new java.lang.ArrayIndexOutOfBoundsException("Aggregate(x): the value of x is out of range.");
		Metric metric = this.arrMetrics[index];
		if(metric instanceof ExtDerivedMetric) {
			ExtDerivedMetric edm = (ExtDerivedMetric) metric;
			double dVal = edm.getAggregateValue(); 
			return dVal;
		} else {
			double dVal = metric.getValue(this.rootscope).getValue();
			return dVal;
		}
	}

	public String toString() {
		return "aggregate(&x)";
	}

}
