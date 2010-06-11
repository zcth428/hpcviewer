package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ThreadScopeView extends AbstractBaseScopeView {
	
    public static final String ID = "edu.rice.cs.hpc.viewer.scope.ThreadScopeView";
    
    private int rank_sequence;

    
    public void setRankSequence(int rank) {
    	this.rank_sequence = rank;
    }
    
	@Override
	protected ScopeViewActions createActions(Composite parent, CoolBar coolbar) {
    	IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        return new BaseScopeViewActions(this.getViewSite().getShell(), window, parent, coolbar); 
	}

	@Override
	protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected CellLabelProvider getLabelProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ScopeTreeContentProvider getScopeContentProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void mouseDownEvent(Event event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void updateDisplay() {
        if (myExperiment == null)
        	return;
	
        MetricRaw metrics[] = this.myExperiment.getMetricRaw();
        
        for (int i=0; i<metrics.length; i++) {
        	MetricRaw metric = metrics[i];
        }
	}

	

}
