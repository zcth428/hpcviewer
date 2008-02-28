package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.*;

import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.Experiment;

public class ScopeTreeContentProvider implements ITreeContentProvider {
    protected TreeViewer viewer;
    final int MODE_FLAT=1;
    final int MODE_NORMAL = 0;
    private int iMode=0;
    private Experiment experiment = null;
    
    /**
     * set the mode to flatten/unflatten
     */
    public void setModeFlat() {
    	this.iMode = this.MODE_FLAT;
    }

    /**
     * set the experiment file
     * @param exp
     */
    public void setExperiment(Experiment exp) {
    	this.experiment = exp;
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
    	if(parentElement instanceof Scope.Node) {
    		// normal mode
        	Scope.Node parent = ((Scope.Node) parentElement);
        	int iChildren = parent.getChildCount();
        	Scope.Node []children = new Scope.Node[iChildren];
        	for(int i=0;i<iChildren;i++) {
        		children[i] = (Scope.Node)parent.getChildAt(i);
        	}
        	return children;
    	} else if(parentElement instanceof ArrayOfNodes) {
    		// flat-tree node
    		ArrayOfNodes listNodes = (ArrayOfNodes) parentElement;
    		/* System.err.println("TreeContentProvider:"+ listNodes.size());
    		
    		if(this.experiment != null)
    			listNodes.add(0, this.experiment.getRootScope().getTreeNode());
    			
    			*/
    		return listNodes.toArray();
    	}
    	return null;
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
    }
 
    public void dispose() {}
}