package edu.rice.cs.hpc.viewer.scope;

import java.util.HashMap;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.viewer.window.Database;

/**
 * 
 * @author laksonoadhianto
 *
 */
abstract public class BaseScopeView  extends AbstractBaseScopeView {
	
    //======================================================
    // ................ ATTRIBUTES..........................
    //======================================================
	
	private TreeViewerColumn []colMetrics = null;
	/** we have to make sure that the listener is added only once for a given window **/
	static private HashMap<IWorkbenchWindow, DynamicViewListener> hashWindow;

    //======================================================
    // ................ METHODS  ..........................
    //======================================================
	
	/***
	 * Standard method to open a scope view (cct, caller tree or flat tree)
	 * 
	 * @param page : current page where the view has to be hosted
	 * @param root : the root scope
	 * @param secondaryID : aux id for the view
	 * @param db : database
	 * @param viewState : state of the view (VIEW_ACTIVATE, VIEW_VISIBLE, ... )
	 * 
	 * @return	the view
	 * @throws PartInitException
	 */
	static public BaseScopeView openView(IWorkbenchPage page, RootScope root, String secondaryID, 
			Database db, int viewState ) 
			throws PartInitException {
		
		BaseScopeView objView = null;
		
		if (root.getType() == RootScopeType.CallingContextTree) {
			int state = (viewState<=0? IWorkbenchPage.VIEW_ACTIVATE : viewState);
			// using VIEW_ACTIVATE will cause this one to end up with focus (on top).
			objView = (BaseScopeView) page.showView(ScopeView.ID , secondaryID, state); 
			
			if (objView.getTreeViewer().getInput() == null) {
				objView.setInput(db, root);
			}

		} else if (root.getType() == RootScopeType.CallerTree) {
			if (viewState>0) {
				objView = (BaseScopeView) page.showView(CallerScopeView.ID , secondaryID, IWorkbenchPage.VIEW_VISIBLE);

				if (objView.getTreeViewer().getInput() == null) {
					// the view has been closed. Need to set the input again
					objView.setInput(db, root);
				}
				objView = (BaseScopeView) page.showView(CallerScopeView.ID , secondaryID, IWorkbenchPage.VIEW_ACTIVATE);
			} else {
				// default situation (or first creation)
				objView = (BaseScopeView) page.showView(CallerScopeView.ID , secondaryID, IWorkbenchPage.VIEW_VISIBLE); 
				
				if (objView.getTreeViewer().getInput() == null) {
					// we need to initialize the view since hpcviewer requires every view to have database and rootscope 
					objView.initDatabase(db, root);
					
					if (hashWindow == null) {
						hashWindow = new HashMap<IWorkbenchWindow, DynamicViewListener>();
					}
					final IWorkbenchWindow window = page.getWorkbenchWindow();
					
					DynamicViewListener dynamicViewListener = hashWindow.get(window);
					if (dynamicViewListener == null) {
						dynamicViewListener = new DynamicViewListener(window);
						window.getPartService().addPartListener(dynamicViewListener);
						hashWindow.put(window, dynamicViewListener);
					}
					dynamicViewListener.addView(objView, db, root);
				}
			}

		} else if (root.getType() == RootScopeType.Flat) {
			int state = (viewState<=0? IWorkbenchPage.VIEW_VISIBLE : viewState);
			objView = (BaseScopeView) page.showView(FlatScopeView.ID, secondaryID, state); 
			if (objView.getTreeViewer().getInput() == null) {
				objView.setInput(db, root);
			}
		}
		return objView;
	}
	
	static public BaseScopeView openView(IWorkbenchPage page, RootScope root, String secondaryID, 
			Database db) throws PartInitException {
		return openView(page, root, secondaryID, db, -1);
	}
	
    //======================================================
    // ................ UPDATE ............................
    //======================================================
    
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView#updateDisplay()
	 */
	protected void updateDisplay() {
        if (database == null)
        	return;
                
        initTableColumns();
        
        // Update root scope
        if (myRootScope.getChildCount() > 0) {
            treeViewer.setInput(myRootScope);
            
            this.objViewActions.updateContent(getExperiment(), myRootScope, colMetrics);

            // FIXME: For unknown reason, the updateContent method above does not resize the column automatically,
            // so we need to do it here, manually ... sigh
            this.objViewActions.resizeColumns();	// resize the column to fit all metrics
        	
            // Laks 2009.03.17: select the first scope
            TreeItem objItem = this.treeViewer.getTree().getItem(1);
            this.treeViewer.getTree().setSelection(objItem);
            // reset the button
            this.objViewActions.checkNodeButtons();
        } else {
        	// empty experiment data (it should be a warning instead of an error. The error should be on the profile side).
        	this.objViewActions.showErrorMessage("Warning: empty database.");
        }
        
        // ------------------------------------------------------------
        // Tell children to update the content with the new database
        // ------------------------------------------------------------
        final Experiment myExperiment = database.getExperiment();        
        this.updateDatabase(myExperiment);
   	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView#initTableColumns()
	 */
	protected void initTableColumns() {
		if (colMetrics != null)
			return;
		
        final Experiment myExperiment = database.getExperiment();        

        int iColCount = this.treeViewer.getTree().getColumnCount();
        if(iColCount>1) {
        	// remove the metric columns blindly
        	// TODO we need to have a more elegant solution here
        	for(int i=1;i<iColCount;i++) {
        		this.treeViewer.getTree().getColumn(1).dispose();
        	}
        }
        // prepare the data for the sorter class for tree
        sorterTreeColumn.setMetric(myExperiment.getMetric(0));

        int nbMetrics = myExperiment.getMetricCount();
        boolean status[] = new boolean[nbMetrics];
        // dirty solution to update titles
        colMetrics = new TreeViewerColumn[nbMetrics];
        {
            // Update metric title labels
            String[] titles = new String[nbMetrics+1];
            titles[0] = "Scope";	// unused element. Already defined
            // add table column for each metric
        	for (int i=0; i<nbMetrics; i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();	// get the title
        		colMetrics[i] = this.treeViewer.addTreeColumn(myExperiment.getMetric(i), (i==0));
        		status[i] = myExperiment.getMetric(i).getDisplayed();
        	}
            treeViewer.setColumnProperties(titles); // do we need this ??
        }
        // update the root scope of the actions !
        this.objViewActions.updateContent(myExperiment, (RootScope)this.myRootScope, colMetrics);
    	this.objViewActions.objActionsGUI.setColumnsStatus(status);
	}
	
    /**
     * Tell children to update the content with the new database
     * @param new_database
     */
    abstract protected void updateDatabase(Experiment new_database);

}
