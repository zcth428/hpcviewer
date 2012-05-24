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
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/*************
 * Generic label provider for the scope column in scope view 
 * @author laksonoadhianto
 *
 */
public class ScopeLabelProvider extends ColumnLabelProvider 
{
	final static protected Icons iconCollection = Icons.getInstance();
	final private Color DARK_BLUE;
	final private ViewerWindow viewerWindow;

	/**
	 * Default constructor
	 */
	public ScopeLabelProvider(IWorkbenchWindow window) {

		super();
		this.DARK_BLUE = window.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
		viewerWindow = ViewerWindowManager.getViewerWindow(window);
	}

	/**
	 * Return the image of the column. By default no image
	 */
	public Image getImage(Object element) {
		if(element instanceof Scope) {
			Scope node = (Scope) element;
			return Utilities.getScopeNavButton(node);
		}
		return null;
	}
	
	/**
	 * Return the text of the scope tree. By default is the scope name.
	 */
	public String getText(Object element) 
	{
		String text = "";
		if (element instanceof Scope){
			Scope node = (Scope) element;
			
			if (viewerWindow.showCCTLabel())  
			{
				//---------------------------------------------------------------
				// label for debugging purpose
				//---------------------------------------------------------------
				if (node instanceof CallSiteScopeCallerView) 
				{
					CallSiteScopeCallerView caller = (CallSiteScopeCallerView) node;
					Object merged[] = caller.getMergedScopes();
					if (merged != null) {
						text = merged.length+ "*";
					}
					Scope cct = caller.getScopeCCT();
					text += "[c:" + caller.getCCTIndex() +"/" + cct.getCCTIndex()  + "] " ;
				} else
					text = "[c:" + node.getCCTIndex() + "] ";
			} 
			if (viewerWindow.showFlatLabel()) {
				text += "[f: " + node.getFlatIndex() + "] ";
			} 
			text += node.getName();	
		} else
			text = element.getClass().toString();
		return text;
	}
	
	/**
	 * Mark blue for node that has source code file name information.
	 * Attention: we do not verify if the source code exist or not !!
	 */
	public Color getForeground(Object element) {
		if(element instanceof Scope) {
			Scope node = (Scope) element;
			if(Utilities.isFileReadable(node)) {

				// put the color blue
				return this.DARK_BLUE;
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	public String getToolTipText(Object element) {
		return getText(element);
	}
}
