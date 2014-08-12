package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.graph.GraphMenu;

/**
 * Basic class for scope views: calling context and caller view
 * @author laksonoadhianto
 *
 */
public class ScopeView extends BaseScopeView {
	
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.ScopeView";
	
    private GraphMenu graphMenu;
    private int lastClickColumn = -1;
    
    
    /***
     * retrieve the last clicked column
     * 
     * @return the index of the last selected column. -1 if user hasn't clicked yet
     */
    public int getSelectedColumn() {
    	return lastClickColumn;
    }
    
	//@Override
    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
    	ScopeViewActions action = new CallingContextViewActions(this.getViewSite().getShell(), window, parent, coolbar);

		graphMenu = new GraphMenu(getSite().getWorkbenchWindow());
    	
    	return action;
    }

	//@Override
	protected CellLabelProvider getLabelProvider() {
		return new StyledScopeLabelProvider( this.getSite().getWorkbenchWindow() ); 
				//ScopeLabelProvider(this.getSite().getWorkbenchWindow());
	}

	//@Override
	protected void mouseDownEvent(Event event) {
		lastClickColumn = getColumnMouseDown(event);
		
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

	//@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		graphMenu.createAdditionalContextMenu(mgr, database, scope);
	} 


	//@Override
	protected ScopeTreeContentProvider getScopeContentProvider() {
		return new ScopeTreeContentProvider();
	}

	
	
    
	//@Override
	protected void updateDatabase(Experiment newDatabase) {
	}
    
}
