package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

//======================================================
// ................ SORTING ............................
//======================================================
public class ColumnViewerSorter extends ViewerComparator {
	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;
	private int direction = 0;
	private TreeColumn column;
	private ColumnViewer viewer;
	private int iColNumber;
	private Metric metric;
	
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
	public ColumnViewerSorter(ColumnViewer viewer, TreeColumn column, Metric newMetric, int colNum) {
		this.column = column;
		this.iColNumber = colNum;
		this.viewer = viewer;
		this.metric = newMetric;
		this.column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if( ColumnViewerSorter.this.viewer.getComparator() != null ) {
					if( ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this ) {
						int tdirection = ColumnViewerSorter.this.direction;
						
						if( tdirection == ASC ) {
							setSorter(ColumnViewerSorter.this, DESC);
						} else if( tdirection == DESC ) {
							setSorter(ColumnViewerSorter.this, NONE);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				} else {
					setSorter(ColumnViewerSorter.this, ASC);
				}
			}
		});
	}
	
	/**
	 * Sort the column according to the direction
	 * @param sorter
	 * @param direction
	 */
	public void setSorter(ColumnViewerSorter sorter, int direction) {
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
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}
			
		}
	}

	/**
	 * general comparison for sorting
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
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
				String text1 = node1.getScope().getShortName();
				String text2 = node2.getScope().getShortName();
				return text1.compareTo(text2);
			} else {
				// get the metric
				MetricValue mv1 = node1.getScope().getMetricValue(metric);
				MetricValue mv2 = node2.getScope().getMetricValue(metric);
				
				if (mv1.getValue()>mv2.getValue()) return -1;
				if (mv1.getValue()<mv2.getValue()) return 1;
			}
		}
		return 0;
	}

}
