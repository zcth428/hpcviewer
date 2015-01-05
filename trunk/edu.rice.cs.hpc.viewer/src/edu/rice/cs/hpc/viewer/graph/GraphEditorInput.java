package edu.rice.cs.hpc.viewer.graph;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.window.Database;

public class GraphEditorInput implements IEditorInput {
	private final Database _database;
	private final Scope _scope;
	private final MetricRaw _metric;
	
	private final GraphType.PlotType _type;
	/***
	 * Create a new editor input for a give scope, metric, plot type and database
	 * @param experiment
	 * @param scope
	 * @param metric
	 * @param type
	 * @param database
	 */
	public GraphEditorInput(Database database, Scope scope, MetricRaw metric, 
			GraphType.PlotType type, IWorkbenchWindow window) {
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
		final String sOrignalTitle = "[" + GraphType.toString(_type) + "] " + _scope.getName()+": " + _metric.getDisplayName();
		return sOrignalTitle;
	}

	public String getID() {
		return getID(_scope, _metric, _type, _database);
	}
	
	/****
	 * return the ID for the editor graph from the combination of scope, metric, graph type and database
	 * @param scope
	 * @param metric
	 * @param type
	 * @param database
	 * @return
	 */
	static public String getID(Scope scope, MetricRaw metric, GraphType.PlotType type, Database database) {
		return Integer.toString(database.getWindowIndex()) + GraphType.toString(type) + ":" + 
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
	
		
	public GraphType.PlotType getType() {
		return this._type;
	}

	public Database getDatabase() {
		return this._database;
	}
	
	public Scope getScope() {
		return this._scope;
	}
	
	public MetricRaw getMetric() {
		return this._metric;
	}
}
