/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.CoolBar;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.util.PreferenceConstants;

/**
 * @author laksonoadhianto
 *
 */
public abstract class  ScopeActions {
	// public preference
	static public double fTHRESHOLD = 0.6; 
	protected IScopeActionsGUI objActionsGUI;
	protected Composite parent;
    protected Shell				objShell;

	public ScopeActions(Shell shell, Composite parent, CoolBar coolbar) {
    	IPreferenceStore objPref = Activator.getDefault().getPreferenceStore();
    	double fDefaultThreshold = objPref.getDouble(PreferenceConstants.P_THRESHOLD);
    	if(fDefaultThreshold > 0.0)
    		ScopeActions.fTHRESHOLD= fDefaultThreshold;
    	this.parent = parent;
    	this.objShell = shell;
	}
	
	public void setColumnStatus(boolean []status) {
		this.objActionsGUI.setColumnsStatus(status);
	}
	/**
	 * Create your own specific GUI
	 * @param parent
	 * @return
	 */
	protected abstract Composite createGUI(Composite parent, CoolBar coolbar);
	  /**
     * The tree has been updated or has new content. This object needs to refresh
     * the data and variable initialization too.
     * @param exp
     * @param scope
     * @param columns
     */
	public abstract void updateContent(Experiment exp, RootScope scope);
	
	/**
	 * Resize the columns
	 */
	public abstract void resizeColumns();
	
    
    /**
     * Check if zooms and hot-path button need to be disabled or not
     * This is required to solve bug no 132: 
     * https://outreach.scidac.gov/tracker/index.php?func=detail&aid=132&group_id=22&atid=169
     */
    public abstract void checkNodeButtons();
    
    /**
     * Update the content of tree viewer
     * @param tree
     */
    public abstract void setTreeViewer(ScopeTreeViewer tree);
}
