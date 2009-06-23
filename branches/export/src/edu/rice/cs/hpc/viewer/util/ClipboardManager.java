/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.ArrayOfNodes;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * @author laksonoadhianto
 *
 */
public class ClipboardManager {
	private Shell objShell;
	private TreeViewer treeViewer;
	
	public ClipboardManager (Shell shell, TreeViewer viewer) {
		objShell = shell;
		treeViewer = viewer;
	}
	
	
	public void copyTable() {
		final Clipboard cb = new Clipboard(this.objShell.getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		Object []arrObjects = this.treeViewer.getExpandedElements();
		//this.treeViewer.getTree().getColumn(0).get
		// arrObjects can be either array of Scopes or ArrayOfNodes
		if(arrObjects != null) {
			int nbElements = arrObjects.length;
			if(nbElements>0) {
				StringBuilder strText = new StringBuilder();
				if(arrObjects[0] instanceof Scope.Node) {
					for (int i=0; i<nbElements; i++) {
						Scope.Node objNode = (Scope.Node) arrObjects[i];
						strText.append(objNode.getScope().getName());
						
					}
				} else if(arrObjects[0] instanceof ArrayOfNodes) {
					
				} else {
					System.err.println("SVA error: data is unknown type: "+arrObjects[0].getClass());
				}
			}
		}
	}
	
	private String getMetricText(Scope.Node objNode) {
		TreeColumn []arrVisibleColumns = this.treeViewer.getTree().getColumns();
		StringBuilder strText = new StringBuilder();
		for (int i=0; i<arrVisibleColumns.length; i++) {
			TreeColumn objColumn = arrVisibleColumns[i];
			if(objColumn.getWidth()>0) {
				BaseMetric metric = (BaseMetric)objColumn.getData();
				//strText.append(objNode.getScope().getMetricValue(metric));
			}
		}
		return strText.toString();
	}

}
