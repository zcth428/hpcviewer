package edu.rice.cs.hpc.viewer.scope;

import edu.rice.cs.hpc.common.filter.FilterAttribute.Type;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.IMergedScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FinalizeMetricVisitorWithBackup;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;

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
    final private AbstractFilterScope filter;
	
	public CallerViewContentProvider()
	{
		filter = new FilterScope();
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
        	if (enableFilter) {
            	return filter.filter(parentElement, results);
        	}
        	
    	} else if (parentElement instanceof Scope) {
    		Scope scope = (Scope) parentElement;
			if (enableFilter) {
				Object []children = scope.getChildren();
				results = filter.filter(parentElement, children);
			} else {
	    		results = ((Scope)parentElement).getChildren();
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
    
    private class FilterScope extends AbstractFilterScope
    {
		@Override
		protected boolean hasToSkip(Scope scope) {
			if (scope instanceof ProcedureScope) {
				return true;
			}
			return false;
		}

		@Override
		protected Object[] getChildren(Scope scope, Type filterType) {
			return CallerViewContentProvider.this.getChildren(scope);
		}

		@Override
		protected void merge(Scope parent, Scope child, Type filterType) {
			// TODO Auto-generated method stub
			
		}
    }

	@Override
	protected AbstractFilterScope getFilter(BaseExperiment experiment) {
		return filter;
	}    
}
