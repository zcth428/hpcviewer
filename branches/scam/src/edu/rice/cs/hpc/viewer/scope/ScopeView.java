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
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.graph.GraphEditor;
import edu.rice.cs.hpc.viewer.graph.GraphEditorInput;
import edu.rice.cs.hpc.viewer.graph.GraphType;

/**
 * Basic class for scope views: calling context and caller view
 * @author laksonoadhianto
 *
 */
public class ScopeView extends BaseScopeView {
    
	public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";
    
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
    	this.getColumnMouseDown(event);
		
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

			final MetricRaw []metrics = this.getExperiment().getMetricRaw();
			if (metrics == null)
				return;
			
			final int num_metrics = metrics.length;

			for (int i=0; i<num_metrics; i++) {
				MenuManager subMenu = new MenuManager("Graph "+ metrics[i].getTitle());
				this.createGraphMenus(subMenu, scope, metrics[i]);
				mgr.add(subMenu);

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
	private void createGraphMenus(IMenuManager menu, Scope scope, MetricRaw m) {
		menu.add( this.createGraphMenu(scope, m, GraphType.PlotType.PLOT) );
		menu.add( this.createGraphMenu(scope, m, GraphType.PlotType.SORTED) );
		menu.add( this.createGraphMenu(scope, m, GraphType.PlotType.HISTO) );
	}
	
	/***
	 * Create a menu action for graph
	 * @param scope
	 * @param m
	 * @param index
	 * @param t
	 * @return
	 */
	private ScopeGraphAction createGraphMenu( Scope scope, MetricRaw m, GraphType.PlotType t) {
		final String sTitle = GraphType.toString(t);
		return new ScopeGraphAction( sTitle, scope, m, t);
	}
	
	
	
    /********************************************************************************
     * class to initialize an action for displaying a graph
     ********************************************************************************/
    private class ScopeGraphAction extends ScopeViewTreeAction {
    	final private GraphType.PlotType graph_type;
    	final private MetricRaw metric;
    	
		public ScopeGraphAction(String sTitle, Scope scopeCurrent, MetricRaw m, GraphType.PlotType type) {
			
			super(sTitle, scopeCurrent);
			this.metric = m;
			this.graph_type = type;
		}
    	
		public void run() {
			IWorkbenchPage objPage = getSite().getWorkbenchWindow().getActivePage();
        	Experiment exp = getExperiment();

        	
			try {
	        	GraphEditorInput objInput = new GraphEditorInput(exp, scope, metric, graph_type);
	        	objPage.openEditor(objInput, GraphEditor.ID);
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

    }


	@Override
	protected void updateDatabase(Experiment newDatabase) {}
    
}
