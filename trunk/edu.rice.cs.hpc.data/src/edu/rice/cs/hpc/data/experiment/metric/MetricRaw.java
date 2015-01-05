package edu.rice.cs.hpc.data.experiment.metric;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

/****************************************
 * Raw metric class
 * @author laksonoadhianto
 *
 ****************************************/
public class MetricRaw  extends BaseMetric {

	private int ID;
	private String db_glob;
	private int db_id;
	private int num_metrics;
	
	public MetricRaw(String sID, String sDisplayName, boolean displayed, String format, AnnotationType annotationType, int index) {
		super(sID, sDisplayName, displayed, format, annotationType, index, MetricType.EXCLUSIVE);
	}
	
	
	public MetricRaw(int id, String title, String db_pattern, 
			int db_num, int metrics) {
		super( String.valueOf(id), title, true, null, AnnotationType.NONE, db_num, MetricType.EXCLUSIVE);
		this.ID = id;
		this.db_glob = db_pattern;
		this.db_id = db_num;
		this.num_metrics = metrics;
	}
		
	
	/***
	 * return the glob pattern of files of this raw metric
	 * @return
	 */
	public String getGlob() {
		return this.db_glob;
	}
	
	
	/***
	 * retrieve the "local" ID of the raw metric
	 * This ID is unique among raw metrics in the same experiment 
	 * @return
	 */
	public int getRawID() {
		return this.db_id;
	}
	
	
	/***
	 * retrieve the number of raw metrics in this experiment
	 * @return
	 */
	public int getSize() {
		return this.num_metrics;
	}
	
	
	/***
	 * return the ID of the raw metric
	 * The ID is unique for all raw metric across experiments 
	 * @return
	 */
	public int getID() {
		return this.ID;
	}


	//@Override
	public MetricValue getValue(Scope s) {
		return null;
	}


	//@Override
	public BaseMetric duplicate() {
		return new MetricRaw(ID, this.displayName, this.db_glob, this.db_id, this.num_metrics);
	}
	
}
