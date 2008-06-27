/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.experiment.metric.ExtDerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.viewer.metric.ExtDerivedMetricLabelProvider;
import edu.rice.cs.hpc.viewer.metric.MetricLabelProvider;
import edu.rice.cs.hpc.viewer.util.Utilities;

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

    /**
     * Add a new tree column into the tree viewer
     * @param treeViewer: tree viewer
     * @param objMetric: new metric
     * @param iPosition: position of the column inside the viewer (0..n-1)
     * @param bSorted: flag if the column should be sorted or not
     * @return the tree viewer column
     */
	public TreeViewerColumn addTreeColumn(Metric objMetric, int iPosition, boolean bSorted) {
		// laks: addendum for column  
    	TreeViewerColumn colMetric = addTreeColumn(objMetric, iPosition, bSorted, false);
		colMetric.setLabelProvider(new MetricLabelProvider(objMetric, Utilities.fontMetric));
		return colMetric;
    }
    
    /**
     * Create a new column for extended derived metric (which uses an expression formula)
     * 
     * @param treeViewer
     * @param objMetric
     * @param iPosition
     * @param bSorted
     * @return
     */
    public TreeViewerColumn addTreeColumn(ExtDerivedMetric objMetric, int iPosition, 
    		boolean bSorted) {
    	TreeViewerColumn col = addTreeColumn(objMetric, iPosition, bSorted, true);
    	col.setLabelProvider(new ExtDerivedMetricLabelProvider(objMetric, Utilities.fontMetric));
    	return col;
    }

    /**
     * Add new tree column for derived metric
     * @param treeViewer
     * @param objMetric
     * @param iPosition
     * @param bSorted
     * @param b: flag to indicate if this column should be displayed or not (default should be true)
     * @return
     */
    private TreeViewerColumn addTreeColumn(Metric objMetric, int iPosition, 
    		boolean bSorted, boolean bDisplayed) {
    	TreeViewerColumn colMetric = new TreeViewerColumn(this,SWT.RIGHT);	// add column
    	TreeColumn col = colMetric.getColumn();
    	col.setText(objMetric.getDisplayName());	// set the title
    	col.setWidth(120); //TODO dynamic size
		// associate the data of this column to the metric since we
		// allowed columns to move (col position is not enough !)
    	col.setData(objMetric);
		col.setMoveable(true);
		//this.colMetrics[i].getColumn().pack();			// resize as much as possible
		ColumnViewerSorter colSorter = new ColumnViewerSorter(this, 
				col, objMetric,iPosition); // sorting mechanism
		if(bSorted)
			colSorter.setSorter(colSorter, ColumnViewerSorter.ASC);
		return colMetric;
    }

}
