/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.TreeViewer;
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
import edu.rice.cs.hpc.viewer.resources.Icons;

/**
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
	
	static public void insertTopRow(TreeViewer treeViewer, Image imgScope, String []sText) {
		if(sText == null)
			return;
    	TreeItem item = new TreeItem(treeViewer.getTree(), SWT.BOLD, 0);
    	if(imgScope != null)
    		item.setImage(0,imgScope);
    	item.setText(sText);
    	item.setData(sText);
    	// make them bold .... why ?? on windows it looks ok :-(
    	for (int i=1; i< sText.length; i++) {
    		item.setFont(i,Utilities.fontMetric);
    	}

	}
	
	/**
	 * Return an image depending on the scope of the node.
	 * The criteria is based on ScopeTreeCellRenderer.getScopeNavButton()
	 * @param scope
	 * @return
	 */
	static public Image getScopeNavButton(Scope scope) {
		if (scope instanceof CallSiteScope) {
			if (((CallSiteScope) scope).getType() == CallSiteScopeType.CALL_TO_PROCEDURE) {
				return Icons.getInstance().imgCallTo;
			} else {
				return Icons.getInstance().imgCallFrom;
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
     * Verify if the file exist or not
     * @param scope
     * @return
     */
    static public boolean isFileReadable(Scope scope) {
		SourceFile newFile = ((SourceFile)scope.getSourceFile());
		if((newFile != null && (newFile != SourceFile.NONE)
			|| (newFile.isAvailable()))  ) {
			if (newFile instanceof FileSystemSourceFile) {
				FileSystemSourceFile objFile = (FileSystemSourceFile) newFile;
				return objFile.isAvailable();
			}
		}
		return false;
    }

}
