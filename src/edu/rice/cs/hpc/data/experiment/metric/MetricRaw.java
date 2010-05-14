package edu.rice.cs.hpc.data.experiment.metric;

/****************************************
 * Raw metric class
 * @author laksonoadhianto
 *
 ****************************************/
public class MetricRaw {

	private int ID;
	private String name;
	private String db_glob;
	private int db_id;
	private int num_metrics;
	
	public MetricRaw(int id, String title, String db_pattern, 
			int db_num, int metrics) {
		this.ID = id;
		this.name = title;
		this.db_glob = db_pattern;
		this.db_id = db_num;
		this.num_metrics = metrics;
	}
	
	
	/****
	 * retrieve the title of raw metric
	 * @return
	 */
	public String getTitle() {
		return this.name;
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
}
