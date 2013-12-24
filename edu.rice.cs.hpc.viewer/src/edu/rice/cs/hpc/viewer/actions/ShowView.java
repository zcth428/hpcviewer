package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.util.WindowTitle;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;


/****
 * 
 * Command action to activate a (hidden) view
 *
 */
public class ShowView extends AbstractHandler {
	
	private IWorkbenchWindow window;
	//@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		window= HandlerUtil.getActiveWorkbenchWindow(event);
		ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		
		// ------------------------------------------------------------
		// gather data: databases and its views
		// ------------------------------------------------------------
		Database []databases = vWin.getDatabases();
		TreeNode []dbNode = new TreeNode[ databases.length ];
		
		for(int i=0; i<databases.length; i++) {
			
			Database db = databases[i];
			Experiment exp = db.getExperiment();
			
			// set the name of the root nodes
			dbNode[i] = new TreeNode(exp.getName() + " (" + 
					exp.getXMLExperimentFile().getParent() + ") ");
			
			final ExperimentView ev = db.getExperimentView();
			BaseScopeView []views = ev.getViews();
			
			TreeNode []viewNode = new TreeNode[views.length];
			
			// gather all the views of this database
			for(int j=0; j<views.length; j++) {
				TreeItemNode item = new TreeItemNode(ev, j, views[j]);
				viewNode[j] = new TreeNode(item);
				viewNode[j].setParent(dbNode[i]);
			}
			dbNode[i].setChildren(viewNode);
		}
		
		// ------------------------------------------------------------
		// show dialog box so that users can choose which view to show
		// ------------------------------------------------------------
		final DatabaseLabelProvider dbLabelProvider = new DatabaseLabelProvider();
		
		ViewTreeDialog dlg = new ViewTreeDialog(window.getShell(),
				dbLabelProvider, new TreeNodeContentProvider());
		
		dlg.setInput(dbNode);
		
		if ( dlg.open() == Dialog.OK ) {
			Object []results = dlg.getResult();
			if (results != null && results.length>0) {
				for (Object obj: results) {
					Object item = ((TreeNode) obj).getValue();
					
					if (item instanceof TreeItemNode) {
						TreeItemNode itemNode = (TreeItemNode) item;
						BaseScopeView view = itemNode.view;
						try {
							IViewSite site = (IViewSite) view.getSite();
							IWorkbenchPage page = window.getActivePage();

							// ------------------------------------------------------------
							// Activate the view
							// ------------------------------------------------------------
							BaseScopeView newView = ExperimentView.openView(page, 
									view.getRootScope(), site.getSecondaryId(), 
									view.getDatabase(), IWorkbenchPage.VIEW_ACTIVATE);
							
							BaseScopeView []views = itemNode.ev.getViews();
							views[itemNode.index] = newView;
							itemNode.ev.setViews(views);
							
							// when a view is closed, we lose the information of hide/show columns
							// at the moment, the only thing to fix this, is to reset the column status
							
							newView.setInput(view.getDatabase(), view.getRootScope(), false);
	   
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		// hack: force all parts to refresh the title
		// we should instead refresh only activated parts
		dbLabelProvider.wt.refreshViewTitles(window);
		
		dispose(dbNode);
		
		return null;
	}
	
	/***
	 * 
	 * Label provider for the tree node item
	 *
	 */
	private class DatabaseLabelProvider 
		extends BaseLabelProvider implements ILabelProvider
	{
		final private WindowTitle wt = new WindowTitle();
		
		//@Override
		public Image getImage(Object element) {
			Object o = ((TreeNode)element).getValue();
			if (o instanceof TreeItemNode) {
				BaseScopeView view = (BaseScopeView) ((TreeItemNode)o).view;
				return view.getTitleImage();
			}
			return null;
		}

		//@Override
		public String getText(Object element) {
			
			TreeNode node = (TreeNode)element; 
			Object o = node.getValue();
			
			if (o instanceof TreeItemNode) {
				BaseScopeView view = (BaseScopeView) ((TreeItemNode)o).view;
				String title = wt.setTitle(window, view);

				if (view.getTreeViewer().getTree().isDisposed()) {
					title += " *closed*"; 
				}
				return title;
			}
			return (String)o;
		}
	}
	
	/***
	 * 
	 * class to store the information of views
	 *
	 */
	private class TreeItemNode {
		private ExperimentView ev;
		private int index;
		private BaseScopeView view;
		
		public TreeItemNode(ExperimentView ev, int index, BaseScopeView view) {
			this.ev = ev;
			this.index = index;
			this.view = view;
		}
	}
	
	/****
	 * remove the tree to allow garbage collector to retain the memory
	 * 
	 * @param root
	 */
	private void dispose(TreeNode []root) {
		
		for(TreeNode node: root ) {
			
			for(TreeNode viewNode: node.getChildren()) {
				viewNode.setParent(null);
			}
			node.setChildren(null);
			node.setParent(null);
		}
	}
	
	/**************************
	 * 
	 * private class to show all "created "views, whether it's opened or not
	 *
	 */
	private class ViewTreeDialog extends ElementTreeSelectionDialog
	{
		/***
		 * create a view tree dialog
		 * @param parent
		 * @param labelProvider
		 * @param contentProvider
		 */
		public ViewTreeDialog(Shell parent, ILabelProvider labelProvider,
				ITreeContentProvider contentProvider) {
			
			super(parent, labelProvider, contentProvider);
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			// force to expand all trees
	    	final TreeViewer tree = getTreeViewer();
	    	tree.expandAll();

	    	setMessage("Please select a view to activate");
			setTitle("Show a view");
			
			return composite;			
		}
		
		   /*
	     *  (non-Javadoc)
	     * @see org.eclipse.jface.window.Window#open()
	     */
	    public int open() {
	    	
	        super.open();
	        return getReturnCode();
	    }
	}
}
