package edu.rice.cs.hpc.viewer.scope;

import java.util.ArrayList;

import org.eclipse.jface.viewers.*;

import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.filter.FilterMap;

public class ScopeTreeContentProvider implements ITreeContentProvider {
    protected TreeViewer viewer;
    protected boolean enableFilter = false;

    public void setEnableFilter(boolean isFilterEnabled)
    {
    	this.enableFilter = isFilterEnabled;
    }
    
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
        	if (arrChildren != null && arrChildren.length>0)
    			if (!enableFilter) {
        			return arrChildren;
    			} else {
    				// check the element if it has to be excluded, we need to skip the element
    				// and return its descendants 
    				ArrayList<Object> list = new ArrayList<>();
    				FilterMap filter = FilterMap.getInstance();
    				
    				for (Object child : arrChildren)
    				{
    					Scope node = (Scope) child;
    					if (filter.select(node.getName()))
    					{
    						// the child is included, we're fine
    						list.add(node);
    					} else {
    						// the child is excluded, find its descendants
							// recursively check whether its children can be included
							Object []descendants = getChildren(node);
							for(Object descendant : descendants)
							{
								list.add(descendant);
							}
    					}
    				}
    				return list.toArray();
    			}
    	}
    	return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
    	if(element instanceof Scope)
            return ((Scope) element).getParent();
    	else
    		return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
    	if(element instanceof Scope)
            return ((Scope) element).hasChildren(); // !((Scope.Node) element).isLeaf();
    	else
    		return false;
    }

    /**
    * Notifies this content provider that the given viewer's input
    * has been switched to a different element.
    *
    * @param viewer the viewer
    * @param oldInput the old input element, or <code>null</code> if the viewer
    *   did not previously have an input
    * @param newInput the new input element, or <code>null</code> if the viewer
    *   does not have an input
    */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	if (viewer instanceof TreeViewer)
            this.viewer = (TreeViewer)viewer;
    }
 
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {}
}