package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.graph.GraphMenu;
import edu.rice.cs.hpc.viewer.metric.ThreadLevelDataManager;

/*****************************************************
 * 
 * Action GUI for calling context view
 * 
 * This class will add a graph icon to show metrics graph of a scope
 *
 *****************************************************/
public class CallingContextActionsGUI extends ScopeViewActionsGUI {
	
	private GraphMenu graphMenuManager;
	
	private ToolItem tiGraph;

	public CallingContextActionsGUI(Shell objShell, IWorkbenchWindow window,
			Composite parent, CallingContextViewActions objActions) 
	{
		super(objShell, window, parent, objActions);
		
		graphMenuManager = new GraphMenu(window);
	}

	/**
	 * Method to start to build the GUI for the actions
	 * @param parent
	 * @return toolbar composite
	 */
	public Composite buildGUI(Composite parent, CoolBar coolbar) {
		Composite newParent = this.addTooBarAction(coolbar);
		this.finalizeToolBar(parent, coolbar);

		return newParent;
	}

	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.ScopeViewActionsGUI#addTooBarAction(org.eclipse.swt.widgets.CoolBar)
	 */
	protected Composite addTooBarAction(CoolBar parent)  {
		
		Composite c = super.addTooBarAction(parent);
		 
    	// prepare the toolbar
    	final ToolBar toolbar = new ToolBar(parent, SWT.FLAT);

		final MenuManager mgr = new MenuManager("graph");

    	// prepare the icon
		AbstractUIPlugin plugin = Activator.getDefault();
		ImageRegistry imageRegistry = plugin.getImageRegistry();
		Image imgGraph = imageRegistry.get(Activator.IMG_GRAPH);
		
		// add an item into the toolbar
		tiGraph = new ToolItem(toolbar, SWT.DROP_DOWN);
		tiGraph.setImage(imgGraph);
		tiGraph.setToolTipText("Show the graph of metric values of the selected CCT node for all processes/threads");
		tiGraph.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.ARROW || event.detail == 0 || event.detail == SWT.PUSH) {
					Rectangle rect = tiGraph.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolbar.toDisplay(pt);

					mgr.removeAll();
					mgr.createContextMenu(toolbar);
					
					// create the context menu of graphs
					graphMenuManager.createAdditionalContextMenu(mgr, database, getSelectedScope());
					
					// make the context menu appears next to tool item
					final Menu menu = mgr.getMenu();
					menu.setLocation(pt);
					menu.setVisible(true);
				}
			}			
		});
		
		// associate the tool bar as a cool item
		createCoolItem(parent, toolbar);
		
		return c;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.scope.ScopeViewActionsGUI#enableActions()
	 */
	public void enableActions() {
		if (database != null) {
			ThreadLevelDataManager tld_mgr = database.getThreadLevelDataManager();
			if (tld_mgr != null) {
				boolean available = tld_mgr.isDataAvailable();
				tiGraph.setEnabled(available);
				return;
			}
		}
		tiGraph.setEnabled(false);
	}
	
	private Scope getSelectedScope()
	{
		return objViewActions.getSelectedNode();
	}
}
