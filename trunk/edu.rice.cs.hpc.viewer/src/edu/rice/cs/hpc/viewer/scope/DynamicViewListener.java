package edu.rice.cs.hpc.viewer.scope;

import java.util.ArrayList;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.viewer.window.Database;

/***
 * 
 * class to manage the dynamic creation of caller tree
 * the tree is only created if the user click the tab view header
 *
 */
public class DynamicViewListener implements IPartListener2 
{
	private ArrayList<ViewObjectDatabase> listOfViews;
	
	private class ViewObjectDatabase {
		public Database database;
		public BaseScopeView view;
		public RootScope root;
	}
	
	
	public DynamicViewListener(IWorkbenchWindow window) 
	{
		listOfViews = new ArrayList<ViewObjectDatabase>();
	}
	/***
	 * add view to the list of listeners
	 * @param view
	 */
	public void addView( BaseScopeView view, Database data, RootScope root )
	{
		ViewObjectDatabase obj = new ViewObjectDatabase();
		obj.database = data;
		obj.root = root;
		obj.view = view;
		listOfViews.add(obj);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partActivated(IWorkbenchPartReference partRef) {
		
		for (int i=0; i<listOfViews.size(); i++) {
			ViewObjectDatabase obj = listOfViews.get(i);

			if (obj.view.getPartName().equals(partRef.getPartName())) {
				final Experiment experiment = obj.view.getExperiment();
				TreeNode []roots = experiment.getRootScopeChildren();
				if (roots != null) {
					final RootScope cct = (RootScope)roots[0];
					
					// create the tree
					experiment.createCallersView(cct, obj.root);
					
					// notify the view that we have changed the data
					obj.view.setInput(obj.database, obj.root, true);
					
					// remove this view from the list since we already initialize the tree
					listOfViews.remove(i);
					
					return;
				}
			}
		}
	}
	public void partBroughtToTop(IWorkbenchPartReference partRef) {}
	public void partClosed(IWorkbenchPartReference partRef) {}
	public void partDeactivated(IWorkbenchPartReference partRef) {}
	public void partOpened(IWorkbenchPartReference partRef) {}
	public void partHidden(IWorkbenchPartReference partRef) {}
	public void partVisible(IWorkbenchPartReference partRef) {}
	public void partInputChanged(IWorkbenchPartReference partRef) {}
}
