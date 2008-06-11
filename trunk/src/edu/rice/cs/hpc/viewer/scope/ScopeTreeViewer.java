/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author laksono
 * we set lazy virtual bit in this viewer
 */
public class ScopeTreeViewer extends TreeViewer {

	/**
	 * @param parent
	 */
	public ScopeTreeViewer(Composite parent) {
		super(parent, SWT.VIRTUAL);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param tree
	 */
	public ScopeTreeViewer(Tree tree) {
		super(tree, SWT.VIRTUAL);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ScopeTreeViewer(Composite parent, int style) {
		super(parent, SWT.VIRTUAL | style);
		this.setUseHashlookup(true);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Finding the path based on the treeitem information
	 * @param item
	 * @return
	 */
	public TreePath getTreePath(TreeItem item) {
		return super.getTreePathFromItem(item);
	}
}
