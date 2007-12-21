package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.pnode.*;
import edu.rice.cs.hpc.viewer.resources.Icons;

public class ScopeTreeLabelProvider implements ITableLabelProvider {
	private Metric[] metrics;
	final private Icons iconCollection = Icons.getInstance();
	
	public void setMetrics(Metric[] newMetrics) {
		metrics = newMetrics;
	}
	
	public void setPNodes(PNode[] newPNodes) {
	}
	
	public Image getColumnImage(Object element, int col) {
		if(col == 0) {
			if(element instanceof Scope.Node) {
				Scope.Node node;
				node = (Scope.Node) element;
				Scope scope = node.getScope();
				if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.CallSiteScope) {
					// call site
					return this.iconCollection.imgCallTo;
				} else if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.ProcedureScope) {
					return this.iconCollection.imgCallFrom;
				}
			}
		}
		return null;
	}
	
	public String getColumnText(Object element, int col) {
		String text = "";
		if (!(element instanceof Scope.Node))
			return text;
		
		Scope.Node node = (Scope.Node) element;
		if (col == 0)
			text = node.getScope().getShortName();
		else {
			if (metrics != null) {
				if(metrics.length>=col) {
				Metric metric = metrics[col-1];
				MetricValue mv;
				// laks
				mv = node.getScope().getMetricValue(metric);
				if(mv.getPercentValue() == 0.0) text = "";
				else{
					text = metric.getDisplayFormat().format(mv);
					if (text.compareTo("-1.00e00       ") == 0) text = "0.0%";
				}
				}
			}
		}
		//System.err.println(col + ":"+node.getScope().getName());
		return text;
	}
	
	public void addListener(ILabelProviderListener listener) {
		// Throw it away
	}
	
	public void dispose() {}
	
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	
	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}
	
}
