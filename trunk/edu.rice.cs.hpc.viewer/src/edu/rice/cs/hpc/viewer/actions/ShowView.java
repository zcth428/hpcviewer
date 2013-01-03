package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;


/****
 * 
 * Command action to activate a (hidden) view
 *
 */
public class ShowView extends AbstractHandler {

	//@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		
		// ------------------------------------------------------------
		// gather data: databases and its views
		// ------------------------------------------------------------
		Database []databases = vWin.getDatabases();
		TreeNode []dbNode = new TreeNode[ databases.length ];
		
		for(int i=0; i<databases.length; i++) {
			
			Database db = databases[i];
			dbNode[i] = new TreeNode(db.getExperiment().getName());
			BaseScopeView []views = db.getExperimentView().getViews();
			
			TreeNode []viewNode = new TreeNode[views.length];
			for(int j=0; j<views.length; j++) {
				viewNode[j] = new TreeNode(views[j]);
				viewNode[j].setParent(dbNode[i]);
			}
			dbNode[i].setChildren(viewNode);
		}
		
		// ------------------------------------------------------------
		// show dialog box so that users can choose which view to show
		// ------------------------------------------------------------
		ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(window.getShell(),
				new DatabaseLabelProvider(), new TreeNodeContentProvider());
		dlg.setInput(dbNode);
		dlg.setMessage("Please select a view to activate");
		dlg.setTitle("Show a view");
		
		if ( dlg.open() == Dialog.OK ) {
			Object []results = dlg.getResult();
			if (results != null && results.length>0) {
				for (Object obj: results) {
					BaseScopeView view = (BaseScopeView) ((TreeNode) obj).getValue();
					try {
						IViewSite site = (IViewSite) view.getSite();
						IWorkbenchPage page = window.getActivePage();

						// ------------------------------------------------------------
						// Activate the view
						// ------------------------------------------------------------
						BaseScopeView.openView(page, view.getRootScope(), site.getSecondaryId(), 
									view.getDatabase(), IWorkbenchPage.VIEW_ACTIVATE);

					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		dispose(dbNode);
		
		return null;
	}
	
	private class DatabaseLabelProvider extends BaseLabelProvider implements ILabelProvider
	{
		//@Override
		public Image getImage(Object element) {
			Object o = ((TreeNode)element).getValue();
			if (o instanceof BaseScopeView) {
				BaseScopeView view = (BaseScopeView) o;
				return view.getTitleImage();
			}
			return null;
		}

		//@Override
		public String getText(Object element) {
			Object o = ((TreeNode)element).getValue();
			if (o instanceof BaseScopeView) {
				BaseScopeView view = (BaseScopeView) o;
				String title = view.getTitle();
				if (view.getTreeViewer().getTree().isDisposed()) {
					title += " (closed)"; 
				}
				return title;
			}
			return (String)o;
		}
	}
	
	private void dispose(TreeNode []root) {
		
		for(TreeNode node: root ) {
			
			for(TreeNode viewNode: node.getChildren()) {
				viewNode.setParent(null);
			}
			node.setChildren(null);
			node.setParent(null);
		}
	}
}
