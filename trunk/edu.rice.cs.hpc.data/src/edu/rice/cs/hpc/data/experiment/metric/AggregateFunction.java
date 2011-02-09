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
public class AggregateFunction implements Function {

	private RootScope rootscope;
	private static boolean is_warned = false;
	
	/**
	 * Retrieve the aggregate value of a metric
	 * @param metrics: a list of metrics
	 */
	public AggregateFunction(BaseMetric []metrics, RootScope scope) {
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
		int index = (int) param[0];

		if (!is_warned) {
			System.err.println("WARNING: this function is deprecated. Please use the prefix '&' to get the aggregate value of a metric.\n " +
			"For instance, the expression: '&1' is to get the aggregate value of metric 1 (where 1 is the metric ID)");
			
			is_warned = true;
		}
		
		if (this.rootscope != null) {
			// laksono 2010.02.26: bug fix: need to access the metric by ID instead of index metric
			// in this case, the ID is the index given by the formula assuming all ID is integer 
			String sID = String.valueOf(index); 
			BaseMetric metric = this.rootscope.getExperiment().getMetric(sID);
			if (metric != null)
				return metric.getValue(this.rootscope).getValue();
		} 

		// the rootscope is null, or the metric doesn't exist 
		// it is not important what value is returned. (or we send an exception ?)
		throw new RuntimeException("Invalid argument of aggregate function: " + index);
	}

	public String toString() {
		return "aggregate(x) -- deprecated. Use &x instead.";
	}

}
