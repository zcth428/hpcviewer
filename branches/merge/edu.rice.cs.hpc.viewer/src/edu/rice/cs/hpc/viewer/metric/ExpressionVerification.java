/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;

import com.graphbuilder.math.Expression;
import edu.rice.cs.hpc.data.experiment.metric.ExtFuncMap;
import edu.rice.cs.hpc.data.experiment.metric.MetricVarMap;

/**
 * @author laksonoadhianto
 *
 */
public class ExpressionVerification {
	
	private ExtFuncMap fctMap;
	
	public ExpressionVerification ( ExtFuncMap fm ) {
		fctMap = fm;
	}
	
	public boolean check ( Expression objExpression ) {
		MetricVarMap vm = new MetricVarMap(false /* case sensitive */);

		vm.setScope(null);
		try {
			double dValue = objExpression.eval(vm, fctMap);
			return (dValue == 0);
		} catch(java.lang.Exception e) {
			// should throw an exception
			e.printStackTrace();
		}

		return false;
	}
}
