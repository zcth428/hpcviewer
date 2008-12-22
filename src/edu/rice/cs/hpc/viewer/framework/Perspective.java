package edu.rice.cs.hpc.viewer.framework;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import edu.rice.cs.hpc.viewer.scope.ScopeView;
import edu.rice.cs.hpc.viewer.scope.CallerScopeView;
import edu.rice.cs.hpc.viewer.scope.FlatScopeView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		// get the main area
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		// configure the view layout
		IPlaceholderFolderLayout objLayout = layout.createPlaceholderFolder("bottomViews", IPageLayout.BOTTOM, 0.5f, editorArea);
		objLayout.addPlaceholder(ScopeView.ID);
		objLayout.addPlaceholder(CallerScopeView.ID);
		objLayout.addPlaceholder(FlatScopeView.ID);

		// prevent the views closable
		layout.getViewLayout(ScopeView.ID).setCloseable(false);
		layout.getViewLayout(CallerScopeView.ID).setCloseable(false);
		layout.getViewLayout(FlatScopeView.ID).setCloseable(false);
		//layout.addStandaloneView(edu.rice.cs.hpcvision.scope.ScopeView.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
		//layout.addStandaloneView(View.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
	}

}
