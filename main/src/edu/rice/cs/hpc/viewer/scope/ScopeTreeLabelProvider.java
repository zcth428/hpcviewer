package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.pnode.*;;

public class ScopeTreeLabelProvider implements ITableLabelProvider {
	private Metric[] metrics;
	private PNode[] pnodes;
	// Laks: create image descriptor for the tree
	final private org.eclipse.jface.resource.ImageDescriptor imgCALL_FROM = ImageDescriptor.createFromFile(
			this.getClass(),
			"../../../../../../../icons/"+"CallFrom.gif");
	final private org.eclipse.jface.resource.ImageDescriptor imgCALL_TO = ImageDescriptor.createFromFile(
			this.getClass(),
			"../../../../../../../icons/"+"CallTo.gif");
	// Laks: create the cache image.
	// TODO: Need to dispose images once unused !
	private Image imgCallFrom = this.imgCALL_FROM.createImage();
	private Image imgCallTo = this.imgCALL_TO.createImage();
	
	public void setMetrics(Metric[] newMetrics) {
		metrics = newMetrics;
	}
	
	public void setPNodes(PNode[] newPNodes) {
		pnodes = newPNodes;
	}
	
	public Image getColumnImage(Object element, int col) {
		if(col == 0) {
			if(element instanceof Scope.Node) {
				Scope.Node node;
				node = (Scope.Node) element;
				Scope scope = node.getScope();
				if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.CallSiteScope) {
					// call site
					return this.imgCALL_TO.createImage();
				} else if (scope instanceof edu.rice.cs.hpc.data.experiment.scope.ProcedureScope) {
					return this.imgCallFrom;
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
				
				//DEBUG System.err.println("pnode index: "+pnodes[0].getIndex());
				/*
				if (pnodes.length <= 1) {
					mv = node.getScope().getMetricValue(metric, pnodes[0]);
				} else { // multiple nodes
					//TODO average these somehow
					mv = node.getScope().getMetricValue(metric, pnodes[0]);
				}
				*/
				text = metric.getDisplayFormat().format(mv);
				if (text.compareTo("-1.00e00       ") == 0) text = "0.0%";
				}
			}
		}

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
	
	// Since we use image class, we need to dispose the resource once we don't need it
	// For unknown reason Java 1.5 or SWT does not free the resource (why ????)
	protected void finalize() throws Throwable{
		if (this.imgCallFrom != null)this.imgCallFrom.dispose();
		if (this.imgCallTo != null)this.imgCallTo.dispose();
		super.finalize();
	}
}
