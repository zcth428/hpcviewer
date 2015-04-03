package edu.rice.cs.hpc.viewer.scope;

import edu.rice.cs.hpc.common.filter.FilterAttribute;
import edu.rice.cs.hpc.common.filter.FilterAttribute.Type;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;

/************************************************************************
 * 
 * content provider for CCT view
 *
 ************************************************************************/
public class ScopeTreeContentProvider extends AbstractContentProvider 
{
	private CCTFilter filter = null;
	
	public ScopeTreeContentProvider() 
	{
	}
	
	@Override
	protected AbstractFilterScope getFilter(BaseExperiment experiment) {
		if (filter == null)
		{
			filter = new CCTFilter(experiment);
		} else if (experiment != filter.experiment) {
			filter.experiment = experiment;
		}
		
		return filter;
	}
	
	
	/*******
     * 
     * Filtering CCT
     * 
     * X : exclusive metric value
     * I : Inclusive metric value
     * 
     * Exclusive filter
     * Xp <- For all filtered i, sum Xi
     * Ip <- Ip (no change)
     * 
     * Inclusive filter
     * Xp <- For all filtered i, sum Ii
     * Ip <- Ip (no change)
     *
	 * 
	 * @param experiment
	 * @param filterType
	 * @param parent
	 * @param child
	 */
	private void mergeMetrics(Experiment experiment, FilterAttribute.Type filterType, 
			Scope parent, Scope child)
	{
		// the child matches a filter pattern
		// let's compute the parent's metric value
		MetricValue []childValues = child.getMetricValues();
		if (childValues != null)
		{
			RootScope root  = (RootScope) experiment.getRootScopeChildren()[0];
			// merge metrics if necessary
			final int numMetrics = experiment.getMetricCount();
			
			for(int i=0; i<numMetrics; i++) 
			{
				BaseMetric metric = experiment.getMetric(i);
				final MetricValue mvChild  = childValues[i];

				if ( (metric.getMetricType() == MetricType.EXCLUSIVE) &&
					 (filterType == FilterAttribute.Type.Exclusive)	)
				{
					// exclusive filter and exclusive metric:
					// the exclusive value of the child is added to the exclusive value of the parent
					
					mergeMetricToParent(root, parent, i, mvChild);
					
				} else if ( (metric.getMetricType() == MetricType.INCLUSIVE) &&
						 (filterType == FilterAttribute.Type.Inclusive)	)
				{
					// inclusive filter and inclusive metric:
					// the inclusive value of the child is added to the exclusive value of the parent
					int index_exclusive_metric = metric.getPartner();
					if (index_exclusive_metric != metric.getIndex())
					{
						// this is tricky: the original index of the metric is the same as the short name
						// however, when we ask getMetric(), it requires the metric index in the array (which is 0..n)
						// we can cheat this by converting the index into "short name" and get the metric.
						BaseMetric metric_exc = experiment.getMetric(String.valueOf(index_exclusive_metric));
						// the exclusive metric exist
						mergeMetricToParent(root, parent, metric_exc.getIndex(), mvChild);
					}
				}
			}
		}
	}
	
	/***************
	 * Merge child metric value to the parent, set the percentage (annotation) and put it to the parent's 
	 * 	filter metric
	 * 
	 * @param root   : root scope
	 * @param parent : scope of the parent
	 * @param metric : base metric for the parent
	 * @param metric_exclusive_index : index of the current metric value to be stored to the parent
	 * @param mvChild : the value of the child scope
	 */
	private void mergeMetricToParent(RootScope root, Scope parent, 
			int metric_exclusive_index, MetricValue mvChild)
	{
		MetricValue mvParentExc = parent.getMetricValue(metric_exclusive_index);
		MetricValue mvParentFilteredExc = parent.getFilteredMetric(metric_exclusive_index);
		float value = 0;
		if (mvParentFilteredExc.getValue() <= 0) {
			// the filtered value is not initialized, let assign with value of the exclusive metric
			value = mvParentExc.getValue();
		}
		// update the filtered value
		value             = mvParentFilteredExc.getValue() + mvChild.getValue();
		float rootValue   = root.getMetricValue(metric_exclusive_index).getValue();
		float annotation  = value / rootValue;
		MetricValue mv    = new MetricValue(value, annotation);
		parent.setFilteredMetric(metric_exclusive_index, mv);
	}


	/*********************************************************
	 * 
	 * Specific filter for CCT 
	 *
	 *********************************************************/
	private  class CCTFilter extends AbstractFilterScope
	{
		BaseExperiment experiment;
		
		public CCTFilter(BaseExperiment experiment)
		{
			this.experiment = experiment;
		}
		@Override
		protected boolean hasToSkip(Scope scope) {
			return false;
		}
		

		@Override
		protected Object[] getChildren(Scope scope, Type filterType) {
			return ScopeTreeContentProvider.this.getChildren(scope);
		}

		@Override
		protected void merge(Scope parent, Scope child, Type filterType) {
			mergeMetrics((Experiment) experiment, filterType, parent, child);
		}

	}
}