package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.swt.SWT;

import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.util.Utilities;

public class ScopeLabelProvider extends ColumnLabelProvider {
	static protected Icons iconCollection = Icons.getInstance();
	private IWorkbenchWindow windowCurrent;
	static private boolean debug = false;
	
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
			return Utilities.getScopeNavButton(scope);
		}
		return null;
	}
	
	/**
	 * Return the text of the scope tree. By default is the scope name.
	 */
	public String getText(Object element) {
		String text = null;
		if (element instanceof Scope.Node){
			Scope.Node node = (Scope.Node) element;
			if (debug)  {
				Scope scope = node.getScope();
				text = "[" + scope.getCCTIndex() + "] " + scope.getName();
			} else
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
			if(Utilities.isFileReadable(scope)) {
				node.hasSourceCodeFile = true; //update the indicator flag in the node
				// put the color blue
				return this.windowCurrent.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		return Utilities.fontGeneral;
	}
}
