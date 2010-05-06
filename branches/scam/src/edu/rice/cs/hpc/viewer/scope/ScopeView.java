package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
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
        	ThreadLevelDataManager objDataManager = exp.getThreadLevelDataManager();
        	
        	// return immediately if the experiment doesn't contain thread level data
        	if (!objDataManager.isDataAvailable())
        		return;
        	
        	if (this.selectedColumn == 0) {
        		final int num_metrics = objDataManager.getNumMetrics();
        		//final int num_metrics = GraphScopeView.getNormalizedMetricIndex( exp.getMetricCount() );
        		for (int i=0; i<num_metrics; i++) {
        			final BaseMetric metric = exp.getMetric( GraphScopeView.getStandardMetricIndex(i) );
        			final String menu_title = GraphScopeView.getGraphTitle(scope, metric, i);
        			final MenuManager subMenu = new MenuManager("Graph "+menu_title);
        			
        			this.createGraphMenus(subMenu, scope, metric, i);
        			mgr.add(subMenu);

        			/*
        			//final String status = (i%2==0? " (I)" : " (E)");
        			mgr.add( new ScopeGraphAction("Graph "+menu_title, scope, metric, i));
        			*/
        			
        		}
        		
        	} else {
    			final BaseMetric metric = exp.getMetric(selectedColumn-1);
    			final int metric_index = GraphScopeView.getNormalizedMetricIndex( selectedColumn-1);
    			final String menu_title = GraphScopeView.getGraphTitle(scope, metric, metric_index);    			
    			final MenuManager subMenu = new MenuManager("Graph "+menu_title);
    			
    			this.createGraphMenus(subMenu, scope, metric, metric_index);
    			mgr.add(subMenu);
            	//mgr.add( new ScopeGraphAction("Graph "+ menu_title, scope, metric, metric_index));
        	}
        }		
	} 


	@Override
	protected ScopeTreeContentProvider getScopeContentProvider() {
		return new ScopeTreeContentProvider();
	}

	
	/***
	 * Create 3 submenus for plotting graph: plot, sorted and histo
	 * @param menu
	 * @param scope
	 * @param m
	 * @param index
	 */
	private void createGraphMenus(IMenuManager menu, Scope scope, BaseMetric m, int index) {
		menu.add( this.createGraphMenu(scope, m, index, GraphType.PLOT) );
		menu.add( this.createGraphMenu(scope, m, index, GraphType.SORTED) );
		menu.add( this.createGraphMenu(scope, m, index, GraphType.HISTO) );
	}
	
	/***
	 * Create a menu action for graph
	 * @param scope
	 * @param m
	 * @param index
	 * @param t
	 * @return
	 */
	private ScopeGraphAction createGraphMenu( Scope scope, BaseMetric m, int index, GraphType t) {
		String sTitle = "Plot graph";
		if (t == GraphType.HISTO)
			sTitle = "Histogram graph";
		else if (t == GraphType.SORTED)
			sTitle = "Sorted plot graph";
		return new ScopeGraphAction( sTitle, scope, m, index, t);
	}
	
	
	/****
	 * type of graph:
	 *	- plot, sorted and histo
	 */
	static private enum GraphType {PLOT, SORTED, HISTO} ;
	
    /********************************************************************************
     * class to initialize an action for displaying a graph
     * @author laksonoadhianto
     *
     ********************************************************************************/
    private class ScopeGraphAction extends ScopeViewTreeAction {
    	private BaseMetric metric;
    	private int metric_index;
    	private GraphType graph_type;
    	
		public ScopeGraphAction(String sTitle, Scope scopeCurrent, BaseMetric m, int m_index, 
				GraphType type) {
			
			super(sTitle, scopeCurrent);
			this.metric = m;
			this.metric_index = m_index;
			this.graph_type = type;
		}
    	
		public void run() {
			IWorkbenchPage objPage = getSite().getWorkbenchWindow().getActivePage();
        	Experiment exp = getExperiment();

			try {
				switch (graph_type) {
				case PLOT:
					GraphScopeView objview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
					objview.plotData(exp, scope, metric, exp.getMetricCount());
					break;
				
				case SORTED:
					// sorted data
					GraphScopeView objSortedview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_s_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
					objSortedview.plotSortedData(exp, scope, metric, exp.getMetricCount());
					break;
					
				case HISTO:
					GraphScopeView objHistoview = (GraphScopeView) objPage.showView(GraphScopeView.ID, 
						scope.getCCTIndex()+"_h_"+metric_index, 
						IWorkbenchPage.VIEW_ACTIVATE);
					objHistoview.plotHistogram(exp, scope, metric, exp.getMetricCount());
					break;
				}
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

    }

    
}
