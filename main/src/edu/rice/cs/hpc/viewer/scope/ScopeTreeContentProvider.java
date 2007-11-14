package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.*;

import edu.rice.cs.hpc.data.experiment.scope.*;

public class ScopeTreeContentProvider implements ITreeContentProvider {
    protected TreeViewer viewer;

    public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
    }

    public Object[] getChildren(Object parentElement) {
    	Scope.Node parent = ((Scope.Node) parentElement);
    	int iChildren = parent.getChildCount();
    	Scope.Node []children = new Scope.Node[iChildren];
    	System.out.println(this.getClass() + "/"+iChildren+":"+parent.getScope().getShortName());
    	for(int i=0;i<iChildren;i++) {
    		children[i] = (Scope.Node)parent.getChildAt(i);
    	}
    	return children;
    	//return ((Scope.Node) parentElement).getChildren();
    }

    public Object getParent(Object element) {
            return ((Scope.Node) element).getParent();
    }

    public boolean hasChildren(Object element) {
            return !((Scope.Node) element).isLeaf();
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
            /*
            if(oldInput != null) {
                    removeListenerFrom((Scope.Node)oldInput);
            }
            if(newInput != null) {
                    addListenerTo((Scope.Node)newInput);
            }
            */
    }
 
    public void dispose() {}
}