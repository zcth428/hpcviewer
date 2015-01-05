/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

/**
 * Class for flat view scope. 
 * This class has special actions differed from calling context and caller view
 * @author laksonoadhianto
 *
 */
public class FlatScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.FlatScopeView";

    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new FlatScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
    }

	//@Override
	protected CellLabelProvider getLabelProvider() {
		return new StyledScopeLabelProvider(this.getSite().getWorkbenchWindow());
	}

	//@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	protected void mouseDownEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	protected ScopeTreeContentProvider getScopeContentProvider() {
		return new ScopeTreeContentProvider();
	}

	//@Override
	protected void updateDatabase(Experiment newDatabase) {}


}
