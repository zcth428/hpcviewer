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

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.viewer.metric.MetricLabelProvider;

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
	public TreeViewerColumn addTreeColumn(BaseMetric objMetric, boolean bSorted) {
		// laks: addendum for column  
    	TreeViewerColumn colMetric = addTreeColumn(objMetric, bSorted, false);
		return colMetric;
    }
    
	/**
	 * Return the canocalized text from the list of elements 
	 * @param sListOfTexts
	 * @param sSeparator
	 * @return
	 */
	public String getTextBasedOnColumnStatus(String []sListOfTexts, String sSeparator, 
			int startColIndex, int startTextIndex) {
		StringBuffer sBuf = new StringBuffer();
		TreeColumn columns[] = this.getTree().getColumns();
		for ( int i=startColIndex; i<columns.length; i++ ) {
			if ( columns[i].getWidth()>0 ) {
				if (sBuf.length()>0)
					sBuf.append(sSeparator);
				sBuf.append( sListOfTexts[i+startTextIndex] );
			}
		}
		return sBuf.toString();
	}
	
	/**
	 * retrieve the title of the columns
	 * @param iStartColIndex
	 * @param sSeparator
	 * @return
	 */
	public String getColumnTitle(int iStartColIndex, String sSeparator) {
		// get the column title first
		TreeColumn columns[] = this.getTree().getColumns();
		String sTitles[] = new String[columns.length];
		for ( int i=0; i<columns.length; i++ ) {
			sTitles[i] = "\"" + columns[i].getText().trim() + "\"";
		}
		// then get the string based on the column status
		return this.getTextBasedOnColumnStatus(sTitles, sSeparator, iStartColIndex, 0);
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
    private TreeViewerColumn addTreeColumn(BaseMetric objMetric, //int iPosition, 
    		boolean bSorted, boolean bDisplayed) {
    	
    	TreeViewerColumn colMetric = new TreeViewerColumn(this,SWT.RIGHT);	// add column
		colMetric.setLabelProvider(new MetricLabelProvider(objMetric /*, Utilities.fontMetric*/) );

		TreeColumn col = colMetric.getColumn();
    	col.setText(objMetric.getDisplayName());	// set the title
    	col.setWidth(120); //TODO dynamic size
		// associate the data of this column to the metric since we
		// allowed columns to move (col position is not enough !)
    	col.setData(objMetric);
		col.setMoveable(true);
		//this.colMetrics[i].getColumn().pack();			// resize as much as possible
		int iPosition = this.doGetColumnCount();
		ColumnViewerSorter colSorter = new ColumnViewerSorter(this, 
				col, objMetric,iPosition); // sorting mechanism
		if(bSorted)
			colSorter.setSorter(colSorter, ColumnViewerSorter.ASC);

		return colMetric;
    }
	/**
	 * Returns the viewer cell at the given widget-relative coordinates, or
	 * <code>null</code> if there is no cell at that location
	 * 
	 * @param point
	 * 		the widget-relative coordinates
	 * @return the cell or <code>null</code> if no cell is found at the given
	 * 	point
	 * 
	 * @since 3.4
	 */
	/*
    public ViewerCell getCell(Point point) {
		ViewerRow row = getViewerRow(point);
		if (row != null) {
			return row.getCell(point);
		}

		return null;
	}*/
}
