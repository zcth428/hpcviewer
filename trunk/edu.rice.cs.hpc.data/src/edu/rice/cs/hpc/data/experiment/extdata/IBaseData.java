package edu.rice.cs.hpc.data.experiment.extdata;

public interface IBaseData {
	/**The size of one trace record in bytes (cpid (= 4 bytes) + timeStamp (= 8 bytes)).*/
	public final static byte SIZE_OF_TRACE_RECORD = 12;

	/***
	 * retrieve the list of rank names ( usual format: process.thread )
	 * @return
	 */
	public String []getListOfRanks();
	
	/****
	 * retrieve the number of ranks 
	 * @return
	 */
	public int getNumberOfRanks();
	
	/***
	 * retrieve the start location of a rank in a database
	 * @param rank
	 * @return
	 */
	public long getMinLoc(int rank);
	
	/****
	 * retrieve the end of file location of a rank
	 * @param rank
	 * @return
	 */
	public long getMaxLoc(int rank);
	
	
	/****
	 * get data in long format
	 * @param position
	 * @return
	 */
	public long getLong(long position);
	
	/***
	 * get data in integer format
	 * @param position
	 * @return
	 */
	public int getInt(long position);
	
	/****
	 * get data in double format
	 * @param position
	 * @return
	 */
	public double getDouble(long position);
}
