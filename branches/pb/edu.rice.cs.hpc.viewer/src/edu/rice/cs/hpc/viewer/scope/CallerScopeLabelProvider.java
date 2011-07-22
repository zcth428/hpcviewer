package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * this class will specifically label scopes (tree items) in caller view.
 * If the view has "folded" scopes (i.e. multiple number of occurrences into one item)
 * 	then we mark this number of occurrence to inform users that the metric may not be
 * 	exactly correct. 
 * @author laksonoadhianto
 *
 */
public class CallerScopeLabelProvider extends ScopeLabelProvider implements IStyledLabelProvider {

	/**
	 * constructor. need a window for the super class
	 */
	public CallerScopeLabelProvider(IWorkbenchWindow window) {
		super(window);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	public StyledString getStyledText(Object element) {
		/*if (element instanceof Scope) {
			
			Scope objScope = (Scope) element;
			StyledString styledString= new StyledString();

			if (objScope.iCounter > 1) {
				// in case of multiple recursive, where different scopes in cct access the same call sites,
				// we mark the number of occurrences in the caller view, to indicate that this scope is a
				//	"folded" version of different scopes
				styledString.append("[" + objScope.iCounter+"] ", StyledString.COUNTER_STYLER);
			}
			
			styledString.append(super.getText(element));
			return styledString;
		} */
		return null;
	}

}
