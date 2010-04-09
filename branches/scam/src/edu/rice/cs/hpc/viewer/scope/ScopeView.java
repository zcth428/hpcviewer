package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView.ScopeViewTreeAction;

/**
 * Basic class for scope views: calling context and caller view
 * @author laksonoadhianto
 *
 */
public class ScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";
	private int selectedColumn = -1;

    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
    }

	@Override
	protected CellLabelProvider getLabelProvider() {
		return new ScopeLabelProvider(this.getSite().getWorkbenchWindow());
	}

	@Override
	protected void mouseDownEvent(Event event) {
    	this.selectedColumn = this.getColumnMouseDown(event);
		
	}

    /**
     * Find which column the user has clicked. Return the index of the column if exist,
     * 		-1 otherwise 
     * @param event
     * @return
     */    
    private int getColumnMouseDown(Event event) {
    	Point p = new Point(event.x, event.y);
    	// the method getCell is only supported in Eclipse 3.4
    	ViewerCell cell = this.getTreeViewer().getCell(p); 
    	if(cell == null)
    		return -1;
    	int iPos = cell.getColumnIndex();
    	return iPos;
    }

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
        if (scope != null && this.hasThreadsLevelData) {
        	Experiment exp = this.getExperiment();
        	if (this.selectedColumn == 0) {
        		final int num_metrics = GraphScopeView.getNormalizedMetricIndex( exp.getMetricCount() );
        		for (int i=0; i<num_metrics; i++) {
        			final BaseMetric metric = exp.getMetric( GraphScopeView.getStandardMetricIndex(i) );
        			final String menu_title = GraphScopeView.getGraphTitle(scope, metric, i);
        			mgr.add( new ScopeGraphAction("View "+ menu_title, scope, metric, i));
        		}
        		
        	} else {
    			final BaseMetric metric = exp.getMetric(selectedColumn-1);
    			final int metric_index = GraphScopeView.getNormalizedMetricIndex( selectedColumn-1);
    			final String menu_title = GraphScopeView.getGraphTitle(scope, metric, metric_index);
    			
            	mgr.add( new ScopeGraphAction("View "+ menu_title, scope, metric, metric_index));
        	}
        }		
	} 

	
    /********************************************************************************
     * class to initialize an action for displaying a graph
     * @author laksonoadhianto
     *
     ********************************************************************************/
    private class ScopeGraphAction extends ScopeViewTreeAction {
    	BaseMetric metric;
    	int metric_index;
    	
		public ScopeGraphAction(String sTitle, Scope scopeCurrent, BaseMetric m, int m_index) {
			super(sTitle, scopeCurrent);
			this.metric = m;
			this.metric_index = m_index;
		}
    	
		public void run() {
			IWorkbenchPage objPage = getSite().getWorkbenchWindow().getActivePage();
        	Experiment exp = getExperiment();

			try {
				GraphScopeView objview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
				objview.plotData(exp, scope, metric, exp.getMetricCount());
				
				// sorted data
				GraphScopeView objSortedview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_s_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
				objSortedview.plotSortedData(exp, scope, metric, exp.getMetricCount());
				
				// sorted data
				GraphScopeView objHistoview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_h_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
				objHistoview.plotHistogram(exp, scope, metric, exp.getMetricCount());
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

    }

}
