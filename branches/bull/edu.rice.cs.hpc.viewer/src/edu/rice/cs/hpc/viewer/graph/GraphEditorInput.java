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
	private final int _database;
	
	private final GraphType.PlotType _type;
	
	/***
	 * Create a new editor input for a give scope, metric, plot type and database
	 * @param experiment
	 * @param scope
	 * @param metric
	 * @param type
	 * @param database
	 */
	public GraphEditorInput(Experiment experiment, Scope scope, MetricRaw metric, 
			GraphType.PlotType type, int database) {
		this._experiment = experiment;
		this._scope = scope;
		this._metric = metric;
		this._type = type;
		this._database = database;
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
		return getName(_scope, _metric, _type, _database);
	}

	public String getID() {
		return getID(_scope, _metric, _type, _database);
	}
	
	static public String getName(Scope scope, MetricRaw metric, GraphType.PlotType type, int database) {
		return database + "-[" + GraphType.toString(type) + "] " + scope.getName()+": " + metric.getDisplayName();
	}

	/****
	 * return the ID for the editor graph from the combination of scope, metric, graph type and database
	 * @param scope
	 * @param metric
	 * @param type
	 * @param database
	 * @return
	 */
	static public String getID(Scope scope, MetricRaw metric, GraphType.PlotType type, int database) {
		return Integer.toString(database) + GraphType.toString(type) + ":" + 
		scope.getCCTIndex()+":" + metric.getID();
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
