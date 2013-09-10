package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.*;

import edu.rice.cs.hpc.data.experiment.scope.*;

public class ScopeTreeContentProvider implements ITreeContentProvider {
    protected TreeViewer viewer;
    
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
        	if (arrChildren != null)
        		if (arrChildren.length>0)
        			return arrChildren;
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