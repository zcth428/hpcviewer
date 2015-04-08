package edu.rice.cs.hpc.viewer.scope.bottomup;

import org.eclipse.core.runtime.Assert;

import edu.rice.cs.hpc.common.filter.FilterAttribute;
import edu.rice.cs.hpc.common.filter.FilterAttribute.Type;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.IMergedScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FinalizeMetricVisitorWithBackup;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;

/************************************************************************
 * 
 * Content provider class specifically for caller view
 * This class will update the children of a scope dynamically, unlike
 * other views
 *
 ************************************************************************/
public class CallerViewContentProvider extends AbstractContentProvider 
{
	private ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	private PercentScopeVisitor percentVisitor;
	private FinalizeMetricVisitorWithBackup finalizeVisitor;
    private AbstractFilterScope filter;
	
	public CallerViewContentProvider()
	{
	}
	
    /**
     * get the number of elements (called by jface)
     */
	@Override
    public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
    }

    /**
     * find the list of children
     */
    public Object[] getChildren(Object parentElement) {
    	Object []results = null;
    	
    	if(parentElement instanceof IMergedScope) {
    		// normal mode
    		IMergedScope parent = ((IMergedScope) parentElement);
    		results = parent.getAllChildren(finalizeVisitor, percentVisitor, inclusiveOnly, exclusiveOnly);
        	
    	} else if (parentElement instanceof Scope) {
    		Scope scope = (Scope) parentElement;
    		results = scope.getChildren();
    	}
    	if (enableFilter)
    	{
			Object data = viewer.getTree().getData();
			if (data instanceof RootScope)
			{
				BaseExperiment exp = ((RootScope)data).getExperiment();
	    		return getFilter(exp).filter(parentElement, results);
			}
    	}
    	return results;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
    	if(element instanceof Scope) {
    		Scope node = (Scope) element;
    		boolean has_children = node.hasChildren();
    		if (!has_children) {
    			if (node instanceof CallSiteScopeCallerView) {
        			CallSiteScopeCallerView cc = (CallSiteScopeCallerView) node;
        			has_children = cc.hasScopeChildren(); //cc.numChildren>0;
    			} else if ( !(node instanceof ProcedureScope)){
    				throw new RuntimeException("Unexpected scope node: " + node);
    			}
    		}
            return has_children; // !((Scope.Node) element).isLeaf();    		
    	}
    	else
    		return false;
    }

    
    /***
     * Update the database
     * @param experiment
     */
    public void setDatabase(Experiment experiment) {
    	exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(experiment);
    	inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(experiment);
    	
    	RootScope root = experiment.getCallerTreeRoot();
    	percentVisitor = new PercentScopeVisitor(experiment.getMetricCount(), root);
    	finalizeVisitor = new FinalizeMetricVisitorWithBackup(experiment.getMetrics());
    }

	@Override
	protected AbstractFilterScope getFilter(BaseExperiment experiment) {
		if (filter == null)
		{
			filter = new FilterScope(experiment);
		}
		return filter;
	}    
    
	/*******************************
	 * 
	 * Filter for bottom-up view
	 *
	 *******************************/
    private class FilterScope extends AbstractFilterScope
    {
    	private BaseExperiment experiment;
    	private RootScope root;
    	
    	public FilterScope(BaseExperiment experiment)
    	{
    		reset(experiment);
    	}
    	
    	public void reset(BaseExperiment experiment)
    	{
    		this.experiment = experiment;
    		root = experiment.getCallerTreeRoot();
    	}
    	
		@Override
		protected boolean hasToSkip(Scope scope, FilterAttribute.Type filterType) {

			boolean has_to_skip = (scope instanceof ProcedureScope) ;
			return has_to_skip;
		}

		@Override
		protected Object[] getChildren(Scope scope, Type filterType) {
			return CallerViewContentProvider.this.getChildren(scope);
		}

		@Override
		protected void merge(Scope parent, Scope child, Type filterType) {
			if (child instanceof ProcedureScope)
			{
				int numChildren = child.getChildCount();
				// update the metric of the procedure scope of the children
				for (int i=0; i<numChildren; i++)
				{
					Scope scope = (Scope) child.getChildAt(i);
					ProcedureScope proc = getProcedureByName(scope.getName());
					Assert.isNotNull(proc);

					Experiment exp = (Experiment)experiment;
					int numMetrics = exp.getMetricCount();
					
					for(int j=0; j<numMetrics; j++)
					{
						BaseMetric metric = exp.getMetric(j);
						MetricValue mvChild = scope.getMetricValue(j);
						if (metric.getMetricType() == MetricType.EXCLUSIVE && 
								(filterType == FilterAttribute.Type.Exclusive) )
						{
							mergeMetricToParent(root, proc, j, mvChild);
							
						} else if (metric.getMetricType() == MetricType.INCLUSIVE && 
								(filterType == FilterAttribute.Type.Inclusive)) 
						{
							int exclusive_metric_index = metric.getPartner();
							BaseMetric metric_exc = exp.getMetric(String.valueOf(exclusive_metric_index));
							filter.mergeMetricToParent(root, proc, metric_exc.getIndex(), mvChild);
						}
					}
				}
			}
		}
		
		/*****
		 * get the procedure scope given a procedure name
		 * 
		 * @param name
		 * @return
		 */
		private ProcedureScope getProcedureByName(String name)
		{
			final TreeNode []procedures = root.getChildren();
			
			// linear search ... can be really bad for huge list of procedures
			for(TreeNode node: procedures)
			{
				ProcedureScope scope = (ProcedureScope) node;
				if (name.equals(scope.getName()))
				{
					return scope;
				}
			}
			return null;
		}
    }
}
