/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.graphics.Image;

import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.ExtDerivedMetric;

import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.scope.ColumnViewerSorter;
import edu.rice.cs.hpc.viewer.scope.MetricLabelProvider;
import edu.rice.cs.hpc.viewer.scope.ExtDerivedMetricLabelProvider;

/**
 * Class providing auxiliary utilities methods.
 * Remark: it is useless to instantiate this class since all its methods are static !
 * @author laksono
 *
 */
public class Utilities {
	//special font for the metric columns. It supposed to be fixed font
	static public Font fontMetric;

	/**
	 * Set the font for the metric columns (it may be different to other columns)
	 * @param display
	 */
	static public void setFontMetric(Display display) {
		int iHeight = display.getSystemFont().getFontData()[0].getHeight();
		Utilities.fontMetric = new Font(display, "Courier", iHeight, SWT.NONE); // johnmc - was SWT.NONE
	}
	
	/**
	 * Insert an item on the top on the tree/table with additional image if not null
	 * @param treeViewer : the tree viewer
	 * @param imgScope : the icon for the tree node
	 * @param arrText : the label of the items (started from  col 0..n-1)
	 */
	static public void insertTopRow(TreeViewer treeViewer, Image imgScope, String []arrText) {
		if(arrText == null)
			return;
    	TreeItem item = new TreeItem(treeViewer.getTree(), SWT.BOLD, 0);
    	if(imgScope != null)
    		item.setImage(0,imgScope);
    	Font fntOrig = item.getFont();	// retrieve the original font
    	// make monospace font for all metric columns
    	item.setFont(Utilities.fontMetric);
    	item.setFont(0, fntOrig); // The tree has the original font
    	// put the text on the table
    	item.setText(arrText);
    	// set the array of text as the item data 
    	// we will use this information when the table is sorted (to restore the original top row)
    	item.setData(arrText);
	}

	/**
	 * Return an image depending on the scope of the node.
	 * The criteria is based on ScopeTreeCellRenderer.getScopeNavButton()
	 * @param scope
	 * @return
	 */
	static public Image getScopeNavButton(Scope scope) {
		if (scope instanceof CallSiteScope) {
			CallSiteScope scopeCall = (CallSiteScope) scope;
        	LineScope lineScope = (LineScope) (scopeCall).getLineScope();
			if (((CallSiteScope) scope).getType() == CallSiteScopeType.CALL_TO_PROCEDURE) {
				if(Utilities.isFileReadable(lineScope))
					return Icons.getInstance().imgCallTo;
				else
					return Icons.getInstance().imgCallToDisabled;
			} else {
				if(Utilities.isFileReadable(lineScope))
					return Icons.getInstance().imgCallFrom;
				else
					return Icons.getInstance().imgCallFromDisabled;
			}
		} else if (scope instanceof RootScope) {
			RootScope rs = (RootScope) scope;
			if (rs.getType() == RootScopeType.CallTree)	{ 
				return null;
			}
		} else if (scope instanceof ProcedureScope) {
			if (scope.getParentScope() instanceof RootScope) {
				return null;
			} 
		} else if (scope instanceof LineScope) {
			if (scope.getParentScope() instanceof CallSiteScope) {
				return null;
			}
		}
		else if (scope instanceof LoopScope) {
			if (scope.getParentScope() instanceof CallSiteScope) {
				return null;
			}
		}
		return null;
	}

    /**
     * Verify if the file exist or not.
     * Remark: we will update the flag that indicates the availability of the source code
     * in the scope level. The reason is that it is less time consuming (apparently) to
     * access to the scope level instead of converting and checking into FileSystemSourceFile
     * level.
     * @param scope
     * @return true if the source is available. false otherwise
     */
    static public boolean isFileReadable(Scope scope) {
    	// check if the source code availability is already computed
    	if(scope.iSourceCodeAvailability == Scope.SOURCE_CODE_UNKNOWN) {
    		SourceFile newFile = ((SourceFile)scope.getSourceFile());
    		if((newFile != null && (newFile != SourceFile.NONE)
    			|| (newFile.isAvailable()))  ) {
    			if (newFile instanceof FileSystemSourceFile) {
    				FileSystemSourceFile objFile = (FileSystemSourceFile) newFile;
    				if(objFile != null) {
    					// find the availability of the source code
    					if (objFile.isAvailable()) {
    						scope.iSourceCodeAvailability = Scope.SOURCE_CODE_AVAILABLE;
    						return true;
    					} 
    				}
    			}
    		}
    	} else
    		// the source code availability is already computed, we just reuse it
    		return (scope.iSourceCodeAvailability == Scope.SOURCE_CODE_AVAILABLE);
    	// in this level, we don't think the source code is available
		scope.iSourceCodeAvailability = Scope.SOURCE_CODE_NOT_AVAILABLE;
		return false;
    }

    /**
     * Add a new tree column into the tree viewer
     * @param treeViewer: tree viewer
     * @param objMetric: new metric
     * @param iPosition: position of the column inside the viewer (0..n-1)
     * @param bSorted: flag if the column should be sorted or not
     * @return the tree viewer column
     */
    static public TreeViewerColumn addTreeColumn(TreeViewer treeViewer, Metric objMetric, int iPosition, boolean bSorted) {
		// laks: addendum for column  
    	TreeViewerColumn colMetric = Utilities.addTreeColumn(treeViewer, objMetric, iPosition, bSorted, false);
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
    static public TreeViewerColumn addTreeColumn(TreeViewer treeViewer, ExtDerivedMetric objMetric, int iPosition, 
    		boolean bSorted) {
    	TreeViewerColumn col = Utilities.addTreeColumn(treeViewer, objMetric, iPosition, bSorted, true);
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
    static private TreeViewerColumn addTreeColumn(TreeViewer treeViewer, Metric objMetric, int iPosition, 
    		boolean bSorted, boolean bDisplayed) {
    	TreeViewerColumn colMetric = new TreeViewerColumn(treeViewer,SWT.RIGHT);	// add column
    	TreeColumn col = colMetric.getColumn();
    	col.setText(objMetric.getDisplayName());	// set the title
    	col.setWidth(120); //TODO dynamic size
		// associate the data of this column to the metric since we
		// allowed columns to move (col position is not enough !)
    	col.setData(objMetric);
		col.setMoveable(true);
		//this.colMetrics[i].getColumn().pack();			// resize as much as possible
		ColumnViewerSorter colSorter = new ColumnViewerSorter(treeViewer, 
				col, objMetric,iPosition); // sorting mechanism
		if(bSorted)
			colSorter.setSorter(colSorter, ColumnViewerSorter.ASC);
		return colMetric;
    }
}
