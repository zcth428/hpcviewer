package edu.rice.cs.hpc.viewer.graph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.editor.BaseEditorManager;
import edu.rice.cs.hpc.viewer.metric.ThreadLevelDataManager;
import edu.rice.cs.hpc.viewer.window.Database;

/****
 * 
 * Class to handle metric graph menus (plot, sorted and histo)
 *
 */
public class GraphMenu 
{
	final private IWorkbenchWindow window;
	private Database database;
	
	public GraphMenu(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void createAdditionalContextMenu(IMenuManager mgr, Database database, Scope scope) {
		if (scope != null) {
			this.database = database;
			
			ThreadLevelDataManager objDataManager = database.getThreadLevelDataManager();
			if (objDataManager == null)
				return;

			// return immediately if the experiment doesn't contain thread level data
			if (!objDataManager.isDataAvailable())
				return;

			final MetricRaw []metrics = database.getExperiment().getMetricRaw();
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

	/***
	 * Create 3 submenus for plotting graph: plot, sorted and histo
	 * @param menu
	 * @param scope
	 * @param m
	 * @param index
	 */
	private void createGraphMenus(IMenuManager menu, Scope scope, MetricRaw m) {
		menu.add( createGraphMenu(scope, m, GraphType.PlotType.PLOT) );
		menu.add( createGraphMenu(scope, m, GraphType.PlotType.SORTED) );
		menu.add( createGraphMenu(scope, m, GraphType.PlotType.HISTO) );
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
    private class ScopeGraphAction extends Action {
    	final private GraphType.PlotType graph_type;
    	final private MetricRaw metric;	
    	final private Scope scope;
    	
		public ScopeGraphAction(String sTitle, Scope scopeCurrent, MetricRaw m, GraphType.PlotType type) {
			
			super(sTitle);
			this.metric = m;
			this.graph_type = type;
			scope = scopeCurrent;
		}
    	
		public void run() {
			IWorkbenchPage objPage = window.getActivePage();
        	
			try {
				final Experiment experiment = (Experiment) this.scope.getExperiment();
				
				// prepare to split the editor pane
				boolean needNewPartition = BaseEditorManager.splitBegin(objPage, experiment);
				
				String id = GraphEditorInput.getID(scope, metric, graph_type, database);
	        	GraphEditorInput objInput = getGraphEditorInput(id);
	        	
	        	if (objInput == null) {
	        		objInput = new GraphEditorInput(database, scope, metric, graph_type, window);
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
	        	
	        	// finalize the pane splitting if needed
	        	BaseEditorManager.splitEnd(needNewPartition, editor);
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
    }

	/****
	 * If the editor has been displayed, we need to activate it
	 * If not, return null and let the caller to create a new editor
	 * @param id
	 * @return
	 */
	private GraphEditorInput getGraphEditorInput(String id) {
		IEditorReference editors[] = window.getActivePage().getEditorReferences();
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
	

}
