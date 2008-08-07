package edu.rice.cs.hpc;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import edu.rice.cs.hpc.viewer.scope.ScopeView;
import edu.rice.cs.hpc.viewer.scope.FlatScopeView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		layout.addView(ScopeView.ID, IPageLayout.BOTTOM, 0.66f, editorArea);
		layout.addView(FlatScopeView.ID, IPageLayout.BOTTOM, 0.66f, editorArea);
		IViewLayout objView = layout.getViewLayout(ScopeView.ID);
		objView.setCloseable(false);
		objView = layout.getViewLayout(FlatScopeView.ID);
		objView.setCloseable(false);
		//layout.addStandaloneView(edu.rice.cs.hpcvision.scope.ScopeView.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
		//layout.addStandaloneView(View.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
	}

}
