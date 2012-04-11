package edu.rice.cs.hpc.viewer.scope;

import java.util.ArrayList;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ThreadScopeView extends AbstractBaseScopeView {
	
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.ThreadScopeView";
    
    private ArrayList<MetricRaw> metrics = null;

    public void setInput(Experiment ex, RootScope scope, MetricRaw metric, int rank) {
    	
    	if (metrics == null) {
    		metrics = new ArrayList<MetricRaw>();
    	}
    	
    	final boolean add_new = metrics.contains(metric); 
    	if (!add_new) {
        	metrics.add(metric);    		
    	}

    	//super.setInput(ex, scope);
    	this.setPartName( ex.getName() + ": " + rank);

    }
    
    
	//@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
	}

	//@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	protected CellLabelProvider getLabelProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	protected ScopeTreeContentProvider getScopeContentProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	protected void mouseDownEvent(Event event) {
		// TODO Auto-generated method stub
		
	}


	//@Override
	protected void updateDisplay() {
        if (database == null)
        	return;
	
        MetricRaw metrics[] = database.getExperiment().getMetricRaw();
        Object columns[] = this.treeViewer.getColumnProperties();
        int num_columns = 0;
        if (columns != null) {
        	num_columns = columns.length - 1;
        	
        }
        
        // ---------------------------------------------------------------------------------
        // see if we have additional metric by comparing the existing metric in the table
        // ---------------------------------------------------------------------------------
        if (num_columns < metrics.length) {
        	// we have new metric 
        }
        
        
	}


	//@Override
	protected void initTableColumns() {
		// TODO Auto-generated method stub
		
	}

	

}
