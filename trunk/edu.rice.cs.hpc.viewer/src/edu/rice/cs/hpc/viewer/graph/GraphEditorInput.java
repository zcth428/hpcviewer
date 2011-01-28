package edu.rice.cs.hpc.viewer.graph;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphEditorInput implements IEditorInput {
	private final Experiment _experiment;
	private final Scope _scope;
	private final MetricRaw _metric;
	
	private final GraphType.PlotType _type;
	
	public GraphEditorInput(Experiment experiment, Scope scope, MetricRaw metric, GraphType.PlotType type) {
		this._experiment = experiment;
		this._scope = scope;
		this._metric = metric;
		this._type = type;
	}
	
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return getName(_scope, _metric, _type);
	}

	public String getID() {
		return getID(_scope, _metric, _type);
	}
	
	static public String getName(Scope scope, MetricRaw metric, GraphType.PlotType type) {
		return "[" + GraphType.toString(type) + "] " + scope.getName()+": " + metric.getDisplayName();
	}

	/****
	 * return the ID for the editor graph from the combination of scope, metric and graph type
	 * @param scope
	 * @param metric
	 * @param type
	 * @return
	 */
	static public String getID(Scope scope, MetricRaw metric, GraphType.PlotType type) {
		return GraphType.toString(type) + ":" + scope.getCCTIndex()+":" + metric.getID();
	}

	
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTipText() {
		return getName();
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	public ThreadLevelDataManager getThreadLevelDataManager() {
		return _experiment.getThreadLevelDataManager();
	}
	
	public GraphType.PlotType getType() {
		return this._type;
	}

	public Experiment getExperiment() {
		return this._experiment;
	}
	
	public Scope getScope() {
		return this._scope;
	}
	
	public MetricRaw getMetric() {
		return this._metric;
	}
}
