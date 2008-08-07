package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;


public class ScopeView extends BaseScopeView {
    public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";

    protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
        return new ScopeViewActions(this.getViewSite().getShell(), parent, coolbar); 
    }

}
