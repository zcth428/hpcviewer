package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeCallerView;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.util.Utilities;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

public class StyledScopeLabelProvider extends StyledCellLabelProvider {
	
	final static protected Icons iconCollection = Icons.getInstance();
	final private Styler STYLE_ACTIVE_LINK;
	final private ViewerWindow viewerWindow;

	
	public StyledScopeLabelProvider(IWorkbenchWindow window) {
		super();
		STYLE_ACTIVE_LINK = StyledString.createColorRegistryStyler(JFacePreferences.ACTIVE_HYPERLINK_COLOR, null); 
		viewerWindow = ViewerWindowManager.getViewerWindow(window);
	}
	
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		
		if (element instanceof Scope) {
			Scope node = (Scope) element;
			final String text = getText(node);
			
			StyledString styledString= new StyledString();
			
			if (element instanceof CallSiteScope) {
				final CallSiteScope cs = (CallSiteScope) element;
				int line = cs.getLineScope().getFirstLineNumber();
				
				if (line>0) {
					styledString.append(String.valueOf(line)+": ", StyledString.COUNTER_STYLER);
				}
			}
			if(Utilities.isFileReadable(node)) {
				styledString.append( text, STYLE_ACTIVE_LINK );
			} else {
				styledString.append( text );
			}
			cell.setText(styledString.toString());
			cell.setStyleRanges(styledString.getStyleRanges());
			
			final Image image = Utilities.getScopeNavButton(node);
			cell.setImage(image);
		}
	}

	
	/**
	 * Return the text of the scope tree. By default is the scope name.
	 */
	private String getText(Scope node) 
	{
		String text = "";
			
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
		return text;
	}

}
