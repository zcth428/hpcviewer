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
    	if(parentElement instanceof Scope.Node) {
    		// normal mode
        	Scope.Node parent = ((Scope.Node) parentElement);
        	Object arrChildren[] = parent.getChildren();
        	if (arrChildren.length>0)
        		return arrChildren;
        	/*
        	int iChildren = parent.getChildCount();
        	Scope.Node []children = new Scope.Node[iChildren];
        	for(int i=0;i<iChildren;i++) {
        		children[i] = (Scope.Node)parent.getChildAt(i);
        	}
        	return children; */
    	} else if(parentElement instanceof ArrayOfNodes) {
    		// flat-tree node
    		ArrayOfNodes listNodes = (ArrayOfNodes) parentElement;
    		return listNodes.toArray();
    	}
    	return null;
    }

    public Object getParent(Object element) {
    	if(element instanceof Scope.Node)
            return ((Scope.Node) element).getParent();
    	else
    		return null;
    }

    public boolean hasChildren(Object element) {
    	if(element instanceof Scope.Node)
            return ((Scope.Node) element).hasChildren(); // !((Scope.Node) element).isLeaf();
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
 
    public void dispose() {}
}