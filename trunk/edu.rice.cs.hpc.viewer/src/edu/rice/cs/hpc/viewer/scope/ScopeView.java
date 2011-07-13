package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.graph.GraphEditorBase;
import edu.rice.cs.hpc.viewer.graph.GraphEditorHisto;
import edu.rice.cs.hpc.viewer.graph.GraphEditorInput;
import edu.rice.cs.hpc.viewer.graph.GraphEditorPlot;
import edu.rice.cs.hpc.viewer.graph.GraphEditorPlotSort;
import edu.rice.cs.hpc.viewer.graph.GraphType;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/**
 * Basic class for scope views: calling context and caller view
 * @author laksonoadhianto
 *
 */
public class ScopeView extends BaseScopeView {
	
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.ScopeView";
	
	@Override
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
				MenuManager subMenu = new MenuManager("Graph "+ metrics[i].getDisplayName() );
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
	
	
	/****
	 * If the editor has been displayed, we need to activate it
	 * If not, return null and let the caller to create a new editor
	 * @param id
	 * @return
	 */
	private GraphEditorInput getGraphEditorInput(String id) {
		IEditorReference editors[] = this.getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
		if (editors == null)
			return null;
		
		//-------------------------------------------------------------------
		// look at all active editors if our editor has been there or not
		//-------------------------------------------------------------------
		for (int i = 0; i<editors.length; i++) {
			String name = editors[i].getName();
			
			// check if it is a graph editor (started with [....])
			if (name != null && name.charAt(0)=='[') {
				try {
					IEditorInput input = editors[i].getEditorInput();
					if (input instanceof GraphEditorInput) {
						String editor_id = ((GraphEditorInput)input).getID();
						if (editor_id.equals(id)) {
							// we found out editor !
							return (GraphEditorInput) input;
						}
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return null;
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
			IWorkbenchWindow window = getSite().getWorkbenchWindow();
			IWorkbenchPage objPage = window.getActivePage();
        	Experiment exp = getExperiment();
        	
			try {
				// support for multiple database in one window
				ViewerWindow vWindow = ViewerWindowManager.getViewerWindow(window);
				int database = 1+vWindow.getDbNum(this.scope.getExperiment().getXMLExperimentFile().getPath());
				
				String id = GraphEditorInput.getID(scope, metric, graph_type, database);
	        	GraphEditorInput objInput = getGraphEditorInput(id);
	        	
	        	if (objInput == null) {
	        		objInput = new GraphEditorInput(exp, scope, metric, graph_type, database, window);
	        	}
	        	IEditorPart editor = null;
	        	switch (graph_type) {
	        	case PLOT:
	        		editor = objPage.openEditor(objInput, GraphEditorPlot.ID);
		        	break;
	        	case SORTED:
	        		editor = objPage.openEditor(objInput, GraphEditorPlotSort.ID);
	        		break;
	        	case HISTO:
	        		editor = objPage.openEditor(objInput, GraphEditorHisto.ID);
	        		break;
	        	}
	        	
	        	if (editor instanceof GraphEditorBase) {
	        		((GraphEditorBase)editor).finalize();
	        	}
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

    }


	@Override
	protected void updateDatabase(Experiment newDatabase) {}
    
}
