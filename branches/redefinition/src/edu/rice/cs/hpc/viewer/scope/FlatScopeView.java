/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;

/**
 * Class for flat view scope. 
 * This class has special actions differed from calling context and caller view
 * @author laksonoadhianto
 *
 */
public class FlatScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.FlatScopeView";

    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
        return new FlatScopeViewActions(this.getViewSite().getShell(), parent, coolbar); 
    }


}
