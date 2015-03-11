package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.filter.FilterMap;

/**
 * 
 *
 */
abstract public class BaseScopeView  extends AbstractBaseScopeView {
	
    //======================================================
    // ................ ATTRIBUTES..........................
    //======================================================

    //======================================================
    // ................ METHODS  ..........................
    //======================================================
	public BaseScopeView()
	{
		super();
	}
	
	@Override
    public void dispose() 
    {
    	//serviceProvider.removeSourceProviderListener(listener);
    	super.dispose();
    }
    /// ---------------------------------------------
    /// filter feature
    /// ---------------------------------------------
    
    /****
     * enable/disable filter
     * 
     * @param isEnabled
     */
	protected void enableFilter(boolean isEnabled)
    {
    	if (treeViewer.getTree().isDisposed())
    		return;
    	
    	AbstractContentProvider provider = (AbstractContentProvider) treeViewer.getContentProvider();
		provider.setEnableFilter(isEnabled);
		
		// update the content of the view
		updateDisplay();
    }
    

	
    //======================================================
    // ................ UPDATE ............................
    //======================================================
    
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView#updateDisplay()
	 */
	public void updateDisplay() {
        if (database == null)
        	return;
        
        // ------------------------------------------------------------
        // Tell children to update the content with the new database
        // ------------------------------------------------------------
        final Experiment myExperiment = database.getExperiment();        
        this.updateDatabase(myExperiment);

        // Update root scope
        if (myRootScope.getChildCount() > 0) {
            treeViewer.setInput(myRootScope);
            
            this.objViewActions.updateContent(getExperiment(), myRootScope);

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
   	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView#initTableColumns()
	 */
	protected void initTableColumns(boolean keepColumnStatus) {
		
        if (treeViewer != null) {
        	Tree tree = treeViewer.getTree();
        	if (tree != null && !tree.isDisposed())
        	{
        		AbstractContentProvider provider = (AbstractContentProvider) treeViewer.getContentProvider();
        		FilterMap filter = FilterMap.getInstance();
        		provider.setEnableFilter(filter.isFilterEnabled());
        		
        		initTableColumns(tree, keepColumnStatus);
        	}
        }
	}

	/******
	 * The same version as {@link BaseScopeView.initTableColumns} but without
	 * 	worrying if the tree has been disposed or not.
	 * 
	 * @param tree
	 * @param keepColumnStatus
	 */
	private void initTableColumns(Tree tree, boolean keepColumnStatus) 
	{
        final Experiment myExperiment = database.getExperiment();        
        int nbMetrics = myExperiment.getMetricCount();
        boolean status[] = new boolean[nbMetrics];

        int iColCount = tree.getColumnCount();
        if(iColCount>1) {
        	TreeColumn []columns = tree.getColumns();
        	
        	// this is Eclipse Indigo bug: when a column is disposed, the next column will have
        	//	zero as its width. Somehow they didn't preserve the width of the columns.
        	// Hence, we have to retrieve the information of column width before the dispose action
        	for(int i=1;i<iColCount;i++) {        		
        		// bug fix: for callers view activation, we have to reserve the current status
        		if (keepColumnStatus) {
        			int width = columns[i].getWidth();
        			status[i-1] = (width > 0);
        		}
        	}
        	
        	// remove the metric columns blindly
        	// TODO we need to have a more elegant solution here
        	for(int i=1;i<iColCount;i++) {
        		TreeColumn column = columns[i]; //treeViewer.getTree().getColumn(1);
        		column.dispose();
        	}
        }
        // prepare the data for the sorter class for tree
        sorterTreeColumn.setMetric(myExperiment.getMetric(0));

        // dirty solution to update titles
        TreeViewerColumn []colMetrics = new TreeViewerColumn[nbMetrics];
        {
            // Update metric title labels
            String[] titles = new String[nbMetrics+1];
            titles[0] = "Scope";	// unused element. Already defined
            // add table column for each metric
        	for (int i=0; i<nbMetrics; i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();	// get the title
        		colMetrics[i] = this.treeViewer.addTreeColumn(myExperiment.getMetric(i), (i==0));
        		
        		// bug fix: for view initialization, we need to reset the status of hide/view
        		if (!keepColumnStatus) {
            		status[i] = myExperiment.getMetric(i).getDisplayed();
        		}
        	}
            treeViewer.setColumnProperties(titles); // do we need this ??
        }
        // update the root scope of the actions !
        this.objViewActions.updateContent(myExperiment, (RootScope)this.myRootScope);
    	this.objViewActions.objActionsGUI.setColumnsStatus(status);

	}
    /**
     * Tell children to update the content with the new database
     * @param new_database
     */
    abstract protected void updateDatabase(Experiment new_database);

}
