package edu.rice.cs.hpc.viewer.scope;

import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.CallersViewScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;

public class CallerViewContentProvider extends ScopeTreeContentProvider {

	private ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	private MetricValue root_values[];
	
    /**
     * get the number of elements (called by jface)
     */
    public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
    }

    /**
     * find the list of children
     */
    public Object[] getChildren(Object parentElement) {
    	if(parentElement instanceof Scope.Node) {
    		// normal mode
        	Scope.Node parent = ((Scope.Node) parentElement);
        	Object arrChildren[] = parent.getChildren();
        	// if the database has empty data, the children is null
        	if (arrChildren != null) {
        		if (arrChildren.length>0)
        			return arrChildren;
        	} else {
        		
        		//-------------------------------------------------------------------------
        		// dynamically create callers view
        		//-------------------------------------------------------------------------
        		Scope scope = parent.getScope();

        		CallSiteScopeCallerView cc = (CallSiteScopeCallerView) scope;
        		CallSiteScope cct = (CallSiteScope) cc.getScopeCCT();
        		
        		LinkedList<CallSiteScopeCallerView> path =
        			CallersViewScopeVisitor.createCallChain( cct, cc, cc,
        					inclusiveOnly, exclusiveOnly);
        		
        		//-------------------------------------------------------------------------
        		// set the percent
        		//-------------------------------------------------------------------------
        		for(int i=0; i<path.size(); i++) {
        			CallSiteScopeCallerView callsite = path.get(i);
            		PercentScopeVisitor.setPercentValue(callsite, root_values, root_values.length);
        		}
        		
        		CallSiteScopeCallerView first = path.removeFirst();
        		CallersViewScopeVisitor.addNewPathIntoTree(cc, first, path);
        		
        		//-------------------------------------------------------------------------
        		// set the new node into the content of the tree view:
        		//  we assume that the parent node only has ONE child
        		//-------------------------------------------------------------------------
        		Scope.Node scope_children[] = new Scope.Node[1];
        		scope_children[0] = first.getTreeNode();
        		return scope_children;
        	}
    	}
    	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
    	if(element instanceof Scope.Node) {
    		Scope.Node node = (Scope.Node) element;
    		boolean has_children = node.hasChildren();
    		if (!has_children) {
    			Scope scope = node.getScope();
    			if (scope instanceof CallSiteScopeCallerView) {
        			CallSiteScopeCallerView cc = (CallSiteScopeCallerView) scope;
        			has_children = cc.numChildren>0;
    			}
    		}
            return has_children; // !((Scope.Node) element).isLeaf();    		
    	}
    	else
    		return false;
    }

    
    public void setDatabase(Experiment experiment) {
    	BaseMetric metrics[] = experiment.getMetrics();
    	exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(metrics);
    	inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(metrics);
    	
    	RootScope root = experiment.getCallerTreeRoot();
    	if (root != null) {
    		this.root_values = new MetricValue[experiment.getMetricCount()];
    		
    		for(int i=0; i<experiment.getMetricCount(); i++) {
    			this.root_values[i] = root.getMetricValue(i);
    		}
    	} else {
    		
    		throw new RuntimeException("Unable to retrieve the root of caller view");
    	}
    }
}
