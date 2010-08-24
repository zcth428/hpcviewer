package edu.rice.cs.hpc.viewer.scope;

import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.CallersViewScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PercentScopeVisitor;

public class CallerViewContentProvider extends ScopeTreeContentProvider {

	private ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	private RootScope root;
	private Experiment experiment;
	
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
    	if(parentElement instanceof Scope) {
    		// normal mode
        	Scope parent = ((Scope) parentElement);
        	Object arrChildren[] = parent.getChildren();
        	// if the database has empty data, the children is null
        	if (arrChildren != null) {
        		if (arrChildren.length>0)
        			return arrChildren;
        	} else {
        		
        		//-------------------------------------------------------------------------
        		// dynamically create callers view
        		//-------------------------------------------------------------------------

        		CallSiteScopeCallerView cc = (CallSiteScopeCallerView) parent;
        		CallSiteScope cct = (CallSiteScope) cc.getScopeCCT();
        		
        		LinkedList<CallSiteScopeCallerView> path =
        			CallersViewScopeVisitor.createCallChain( cct, cc,
        					inclusiveOnly, exclusiveOnly);
        		
        		//CallersViewScopeVisitor.mergeCallerPath(cc, inclusiveOnly, exclusiveOnly, path);
        		
        		//-------------------------------------------------------------------------
        		// set the percent
        		//-------------------------------------------------------------------------
        		for(int i=0; i<path.size(); i++) {
        			CallSiteScopeCallerView callsite = path.get(i);
            		PercentScopeVisitor.setPercentValue(callsite, root, this.experiment.getMetricCount());
        		}
        		
        		CallSiteScopeCallerView first = path.removeFirst();
        		CallersViewScopeVisitor.addNewPathIntoTree(cc, first, path);
        		
        		//-------------------------------------------------------------------------
        		// set the new node into the content of the tree view:
        		//-------------------------------------------------------------------------
        		/*int num_kids = first.getSubscopeCount();
        		Scope.Node scope_children[] = new Scope.Node[num_kids];
        		scope_children[0] = first.getTreeNode();
        		return scope_children; */
        		return cc.getChildren();
        	}
    	}
    	return null;
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
        			has_children = cc.numChildren>0;
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
    	
    	root = experiment.getCallerTreeRoot();
    	this.experiment = experiment;
    }
}
