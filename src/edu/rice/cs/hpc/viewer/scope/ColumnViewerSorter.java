package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.graphics.Image;

//import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.ExtDerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.Utilities;
//======================================================
// ................ SORTING ............................
//======================================================
public class ColumnViewerSorter extends ViewerComparator {
	// direction
	public static final int ASC = 1;
	public static final int NONE = 0;	// unused: for init only
	public static final int DESC = -1;
	private int direction = 0;
	// data
	private TreeColumn column;		// column
	private TreeViewer viewer;	// viewer
	private int iColNumber;			// column position
	private Metric metric;			// data for metric table
	
	/**
	 * Update the metric for this column
	 * @param newMetric
	 */
	public void setMetric(Metric newMetric) {
		this.metric = newMetric;
	}
	/**
	 * Class to sort a column
	 * @param viewer: the table tree
	 * @param column: the column
	 * @param newMetric: the metric
	 * @param colNum: the position
	 */
	public ColumnViewerSorter(TreeViewer viewer, TreeColumn column, Metric newMetric, int colNum) {
		this.column = column;
		this.iColNumber = colNum;
		this.viewer = viewer;
		this.metric = newMetric;

		// catch event when the user sort the column on the column header
		this.column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// before sorting, we need to check if the first row is an element header 
				// something like "aggregate metrics" or zoom-in item
				TreeItem item = ColumnViewerSorter.this.viewer.getTree().getItem(0);
				String []sText= null; // have to do this to avoid error in compilation;
				Image imgItem = item.getImage(0);
				if(item.getData() instanceof Scope.Node) {
					// the table has been zoomed-out
				} else {
					// the table is in original form or flattened or zoom-in
					Object o = item.getData();
					if(o != null) {
						Object []arrObj = (Object []) o;
						if(arrObj[0] instanceof String) {
							sText = (String[]) item.getData(); 
						}
					}
				}
				if( ColumnViewerSorter.this.viewer.getComparator() != null ) {
					if( ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this ) {
						int tdirection = ColumnViewerSorter.this.direction;
						
						if( tdirection == ASC ) {
							setSorter(ColumnViewerSorter.this, DESC);
						} else if( tdirection == DESC ) {
							setSorter(ColumnViewerSorter.this, ASC);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				} else {
					setSorter(ColumnViewerSorter.this, ASC);
				}
				// post-sorting 
				if(sText != null) {
					Utilities.insertTopRow(ColumnViewerSorter.this.viewer, imgItem, sText);
				}
			}

		}
		);

		//if (colNum == 1) setSorter(this, ASC); // johnmc
	}
	
	/**
	 * Sort the column according to the direction
	 * @param sorter
	 * @param direction
	 */
	public void setSorter(ColumnViewerSorter sorter, int direction) {
		// bug Eclipse no 199811 https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// sorting can be very slow in mac OS
		// we need to manually disable redraw before comparison and the refresh after the comparison 
		this.viewer.getTree().setRedraw(false);
		if( direction == NONE ) {
			column.getParent().setSortColumn(null);
			column.getParent().setSortDirection(SWT.NONE);
			viewer.setComparator(null);
		} else {
			column.getParent().setSortColumn(column);
			sorter.direction = direction;
			
			if( direction == ASC ) {
				column.getParent().setSortDirection(SWT.DOWN);
			} else {
				column.getParent().setSortDirection(SWT.UP);
			}
			
			if( viewer.getComparator() == sorter ) {
				//viewer.refresh(); // Laks: I don't think we need to refresh the UI now
			} else {
				viewer.setComparator(sorter);
				//viewer.refresh(); // was: johnmc. Laks: we don't need to refresh
			}
			
		}
		// bug Eclipse no 199811 https://bugs.eclipse.org/bugs/show_bug.cgi?id=199811
		// sorting can be very slow in mac OS
		// we need to manually disable redraw before comparison and the refresh after the comparison 
		this.viewer.refresh();
		this.viewer.getTree().setRedraw(true);
	}

	/**
	 * general comparison for sorting
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
	}

	/**
	 * Compare the name of the node 1 and node 2.
	 * @param node1
	 * @param node2
	 * @return
	 */
	private int doCompare(Scope.Node node1, Scope.Node node2) {
		String text1 = node1.getScope().getName();
		String text2 = node2.getScope().getName();
		return text1.compareTo(text2);
	}
	// laks: lazy comparison
	/**
	 * This method is to compare one object to another
	 * Please implement this method in the child class if necessary
	 */
	protected int doCompare(Viewer viewer, Object e1, Object e2) {
		if(e1 instanceof Scope.Node && e2 instanceof Scope.Node) {
			Scope.Node node1 = (Scope.Node) e1;
			Scope.Node node2 = (Scope.Node) e2;

			// dirty solution: if the column position is 0 then we sort
			// according to its element name
			// otherwise, sort according to the metric
			if(this.iColNumber==0) {
				return this.doCompare(node1, node2);
			} else {
				// different treatment between normal metrics and derived metrics
				if(metric instanceof ExtDerivedMetric) {
					
					ExtDerivedMetric edm = (ExtDerivedMetric) metric;
					int iResult = edm.compare(node1.getScope(), node2.getScope());
					if(iResult != 0)
						return iResult;
					/*
					Double d1 = edm.computeValue(node1.getScope()); //edm.getDoubleValue(node1.getScope());
					Double d2 = edm.computeValue(node2.getScope()); //edm.getDoubleValue(node2.getScope());
					if(d1 == null && d2 == null)
						return this.doCompare(node1, node2);
					if(d1 == null) 
						return 1;
					if(d2 == null)
						return -1;
					if(d1.doubleValue()>d2.doubleValue()) return -1;
					if(d1.doubleValue()<d2.doubleValue()) return 1;
					*/
				} else {
					MetricValue mv1 = node1.getScope().getMetricValue(metric);
					MetricValue mv2 = node2.getScope().getMetricValue(metric);
					
					if (mv1.getValue()>mv2.getValue()) return -1;
					if (mv1.getValue()<mv2.getValue()) return 1;
				}
				// if the two values are equal, look at the text of the tree node
				return this.doCompare(node1, node2);
			}
		}
		return 0;
	}

	public void sort(Viewer viewer,
            Object[] elements) {
		super.sort(viewer, elements);
	}
	

}
