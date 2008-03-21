package edu.rice.cs.hpc;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		layout.addView(edu.rice.cs.hpc.viewer.scope.ScopeView.ID, IPageLayout.BOTTOM, 0.66f, editorArea);
		org.eclipse.ui.IViewLayout objView = layout.getViewLayout(edu.rice.cs.hpc.viewer.scope.ScopeView.ID);
		objView.setCloseable(false);
		//layout.addStandaloneView(edu.rice.cs.hpcvision.scope.ScopeView.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
		//layout.addStandaloneView(View.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
	}

}
