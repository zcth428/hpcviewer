package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.SWT;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.viewer.resources.Icons;

public class ScopeLabelProvider extends ColumnLabelProvider {
	final private Icons iconCollection = Icons.getInstance();
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
	 * Return the image of the column. By default no image
	 */
	public Image getImage(Object element) {
		if(element instanceof Scope.Node) {
			Scope.Node node;
			node = (Scope.Node) element;
			Scope scope = node.getScope();
			if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.CallSiteScope) {
				// call site
				return this.iconCollection.imgCallTo;
			} else if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.ProcedureScope) {
				return this.iconCollection.imgCallFrom;
			}
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
			text = node.getScope().getShortName();			
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
