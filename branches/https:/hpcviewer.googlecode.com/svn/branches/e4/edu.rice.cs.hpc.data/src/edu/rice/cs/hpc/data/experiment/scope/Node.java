package edu.rice.cs.hpc.data.experiment.scope;



/*****
 * Class handling the management of node in the tree
 * @author laksonoadhianto
 *
 */
public class Node extends TreeNode {

	int nSize;
		
	/** Constructs a new scope node. */
	
	/**
	 * Copy the scope into this node
	 * @param value: the value of this node
	 */

	public Node(Object value) {
		super(value);
		nSize = 0;
	}

	
	public void add (Node treeNode) {
		if ( (treeNode != null) && (treeNode.getParent() != this) ) {
			TreeNode [] data = this.ensureCapacity(this.nSize + 1);
			data[this.nSize++] = treeNode;
			treeNode.setParent(this);	// we need to make sure it will not be added twice
			this.setChildren(data);
		}
		
	}

	/**
	 * Simulate DefaultMutableTreeNode's getChildAt
	 * @param index
	 * @return
	 */
	public TreeNode getChildAt(int index) {
		return super.getChildren()[index];
	}

	/**
	 * Simulate DefaultMutableTreeNode's getChildCount
	 * @return
	 */
	public int getChildCount() {
		// TODO Auto-generated method stub
		return this.nSize;
		/*
		if (this.getChildren() == null)
			return 0;
		return this.getChildren().length; */
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.TreeNode#getChildren()
	 */
	public TreeNode[] getChildren() {
		if (this.nSize == 0) {
			return null;
		} else {
			TreeNode []oldChildren = super.getChildren();
			if (oldChildren.length == this.nSize)
				return oldChildren;
			else {
				TreeNode []children = new TreeNode[this.nSize];
				System.arraycopy(oldChildren, 0, children, 0, this.nSize);
				return children;
			}
		}
	}

	/****
	 * dispose the allocated attributes (parent and children)
	 */
	public void dispose() {
		setChildren(null);
		setParent(null);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	//// 	PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////

	/**
	 * Make sure the size of the children is big enough. If not, we increment it. 
	 * @param minCapacity
	 * @return
	 */
    private TreeNode [] ensureCapacity(int minCapacity) {
    	TreeNode []oldData = super.getChildren();
    	int oldCapacity = 0; // = getArraySize();
    	if (oldData != null)
    		oldCapacity = oldData.length;
    	if (minCapacity > oldCapacity) {
    	    int newCapacity = (oldCapacity * 3)/2 + 1;
        	if (newCapacity < minCapacity)
        		newCapacity = minCapacity;
    	    TreeNode []newData = new TreeNode[newCapacity];
    	    if (oldCapacity > 0)
    	    	System.arraycopy(oldData, 0, newData, 0, this.nSize);
    	    return newData;
    	} else 
    		return oldData;
    }

}
