package edu.rice.cs.hpc.data.experiment.merge;

import java.util.ArrayList;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/*****
 * 
 * information of two merged metrics 
 * 
 * the merge of two database cause the following changes:
 * - if the metrics are the same, it will be merged into one metric
 * - otherwise, the two metrics will be appended at the end 
 * 		of list of metric in the new database 
 *
 */
public class MergeMetric {
	int pointerMetric1[];
	int pointerMetric2[];
	int pointerCommon[];
	
	ArrayList<BaseMetric> metrics;
	
	/*****
	 * compute the delta of two metric values
	 *
	 * @param target
	 * @param source
	 * @param offset : the index of the source v.a.v the target
	 */
	static public void mergeMetrics(Scope target, Scope source, int offset[]) {
		
		for(int i=0; i<offset.length; i++) {
			MetricValue sourceValue = source.getMetricValue(i);
			MetricValue newValue = MetricValue.NONE;
			
			if (sourceValue != null && MetricValue.isAvailable(sourceValue)) {
				newValue = new MetricValue();
				MetricValue targetValue = target.getMetricValue(offset[i]);
				float myValue = 0;
				
				if (targetValue != null && MetricValue.isAvailable(targetValue)) {
					myValue = target.getMetricValue(offset[i]).getValue();
				}
				MetricValue.setValue(newValue, myValue - sourceValue.getValue());
				target.setMetricValue(offset[i], newValue);
			}
			// make it uniform: annotation not available for all metrics
			MetricValue.setAnnotationAvailable(newValue, false);
		}
	}
	
	static public void setMetrics(Scope target, Scope source, int offset[], int factor) {
		
		for(int i=0; i<offset.length; i++) {
			MetricValue mv = source.getMetricValue(i);
			MetricValue mine = MetricValue.NONE;
			
			if (mv != null && MetricValue.isAvailable(mv)) {
				mine = new MetricValue();
				MetricValue myMV = target.getMetricValue(i);
				float myValue = 0;
				
				if (myMV != null && MetricValue.isAvailable(myMV)) {
					myValue = target.getMetricValue(i).getValue();
					MetricValue.setValue(mine, myValue - mv.getValue());
				} else {
					MetricValue.setValue(mine, factor * mv.getValue());
					float annValue = MetricValue.getAnnotationValue(mv);
					MetricValue.setAnnotationValue(mine, annValue);
				}
				// we don't want to show percentage in the merged metric
				MetricValue.setAnnotationAvailable(mine, false);
				target.setMetricValue(i, mine);
			}
		}
	}
	
}
