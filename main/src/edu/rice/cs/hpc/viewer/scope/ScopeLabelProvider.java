package edu.rice.cs.hpc.viewer.scope;

import javax.swing.Icon;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.SWT;

import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.viewer.resources.Icons;

public class ScopeLabelProvider extends ColumnLabelProvider {
	static protected Icons iconCollection = Icons.getInstance();
	private IWorkbenchWindow windowCurrent;
	/**
	 * Default constructor
	 */
	public ScopeLabelProvider(IWorkbenchWindow window) {
		// TODO Auto-generated constructor stub
		super();
		this.windowCurrent = window;
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
				return ScopeLabelProvider.iconCollection.imgCallTo;
			} else {
				return ScopeLabelProvider.iconCollection.imgCallTo;
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
	 * Return the image of the column. By default no image
	 */
	public Image getImage(Object element) {
		if(element instanceof Scope.Node) {
			Scope.Node node;
			node = (Scope.Node) element;
			Scope scope = node.getScope();
			return ScopeLabelProvider.getScopeNavButton(scope);
		}
		return null;
	}
	
	/**
	 * Return the text of the scope tree. By default is the scope name.
	 */
	public String getText(Object element) {
		String text = "-";
		if (element instanceof Scope.Node){
			Scope.Node node = (Scope.Node) element;
			text = node.getScope().getName();			
		} else
			text = element.getClass().toString();
		return text;
	}
	
	/**
	 * Mark blue for node that has source code file name information.
	 * Attention: we do not verify if the source code exist or not !!
	 */
	public Color getForeground(Object element) {
		if(element instanceof Scope.Node) {
			Scope.Node node = (Scope.Node) element;
			Scope scope = node.getScope();
			SourceFile newFile = ((SourceFile)scope.getSourceFile());
			if((newFile != null && (newFile != SourceFile.NONE)
				|| (newFile.isAvailable()))  ){
				if(newFile instanceof FileSystemSourceFile) {
					FileSystemSourceFile srcFile = (FileSystemSourceFile) newFile;
					if(srcFile !=null && srcFile.isAvailable()) {
						node.hasSourceCodeFile = true; //update the indicator flag in the node
						// put the color blue
						return this.windowCurrent.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
					}
				}
			}
		}
		return null;
	}
}
