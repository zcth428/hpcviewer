package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

/**
 * 
 * @author laksonoadhianto
 *
 */
abstract public class BaseScopeView  extends AbstractBaseScopeView {
	
    //======================================================
    // ................ ATTRIBUTES..........................
    //======================================================
	

    //======================================================
    // ................ METHODS  ..........................
    //======================================================
	
	
	
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
	protected void initTableColumns(boolean keepColumnStatus) {
		
        final Experiment myExperiment = database.getExperiment();        
        int nbMetrics = myExperiment.getMetricCount();
        boolean status[] = new boolean[nbMetrics];

        int iColCount = this.treeViewer.getTree().getColumnCount();
        if(iColCount>1) {
        	// remove the metric columns blindly
        	// TODO we need to have a more elegant solution here
        	for(int i=1;i<iColCount;i++) {
        		TreeColumn column = treeViewer.getTree().getColumn(1);
        		
        		// bug fix: for callers view activation, we have to reserve the current status
        		if (keepColumnStatus) {
        			status[i-1] = column.getWidth()>0;
        		}
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
