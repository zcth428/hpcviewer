package edu.rice.cs.hpc.viewer.framework;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import edu.rice.cs.hpc.viewer.scope.ScopeView;
import edu.rice.cs.hpc.viewer.scope.CallerScopeView;
import edu.rice.cs.hpc.viewer.scope.FlatScopeView;

public class Perspective implements IPerspectiveFactory {
	private IPlaceholderFolderLayout objLayout0 = null;
	private IPlaceholderFolderLayout objLayout1 = null;

	public void createInitialLayout(IPageLayout layout) {
		// get the main area
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
		// configure the view layout, create a separate partition for each of the databases that can be opened
		// this will group the views for each database so they can be treated as a one thing (minimize, restore, resizing, relocating)
		objLayout0 = layout.createPlaceholderFolder("metricViews0", IPageLayout.TOP, 0.2f, editorArea);
		objLayout1 = layout.createPlaceholderFolder("metricViews1", IPageLayout.BOTTOM, 0.2f, "metricViews");
		objLayout0.addPlaceholder(ScopeView.ID);
		objLayout0.addPlaceholder(CallerScopeView.ID);
		objLayout0.addPlaceholder(FlatScopeView.ID);
	}

}
