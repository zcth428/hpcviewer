package edu.rice.cs.hpc;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.IPlaceholderFolderLayout;

import edu.rice.cs.hpc.viewer.scope.ScopeView;
import edu.rice.cs.hpc.viewer.scope.CallerScopeView;
import edu.rice.cs.hpc.viewer.scope.FlatScopeView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		IPlaceholderFolderLayout objLayout = layout.createPlaceholderFolder("bottomViews", IPageLayout.BOTTOM, 0.5f, editorArea);
		objLayout.addPlaceholder(ScopeView.ID);
		objLayout.addPlaceholder(CallerScopeView.ID);
		objLayout.addPlaceholder(FlatScopeView.ID);
		// objLayout.addView(ScopeView.ID);
		// objLayout.addView(FlatScopeView.ID)
		/*
		layout.addView(ScopeView.ID, IPageLayout.BOTTOM, 0.66f, editorArea);
		layout.addView(FlatScopeView.ID, IPageLayout.RIGHT, 0.66f, ScopeView.ID); 
		*/
		IViewLayout objView = layout.getViewLayout(ScopeView.ID);
		objView.setCloseable(false);
		layout.getViewLayout(CallerScopeView.ID).setCloseable(false);
		objView = layout.getViewLayout(FlatScopeView.ID);
		objView.setCloseable(false);
		//layout.addStandaloneView(edu.rice.cs.hpcvision.scope.ScopeView.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
		//layout.addStandaloneView(View.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
	}

}
