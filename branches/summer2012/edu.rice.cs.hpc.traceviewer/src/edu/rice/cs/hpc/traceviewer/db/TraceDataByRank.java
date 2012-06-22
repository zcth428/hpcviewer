package edu.rice.cs.hpc.traceviewer.db;

import java.util.Vector;

import edu.rice.cs.hpc.traceviewer.db.TimeCPID;

public abstract class TraceDataByRank {
	
	/**The size of one trace record in bytes (cpid (= 4 bytes) + timeStamp (= 8 bytes)).*/
	public final static byte SIZE_OF_TRACE_RECORD = 12;

	protected Vector<TimeCPID> listcpid;

	public TraceDataByRank() {
		super();
	}
	

	/** Gets the time that corresponds to the index sample in times. */
	public double getTime(int sample) {
		if (sample < 0)
			return 0;

		final int last_index = listcpid.size();
		if (sample >= last_index) {
			return listcpid.get(last_index - 1).timestamp;
		}
		return listcpid.get(sample).timestamp;
	}

	/** Gets the cpid that corresponds to the index sample in timeLine. */
	public int getCpid(int sample) {
		return listcpid.get(sample).cpid;
	}

	/**
	 * Shifts all the times in the ProcessTimeline to the left by
	 * lowestStartingTime.
	 */
	public void shiftTimeBy(double lowestStartingTime) {
		for (int i = 0; i < listcpid.size(); i++) {
			TimeCPID timecpid = listcpid.get(i);
			timecpid.timestamp = timecpid.timestamp - lowestStartingTime;
			listcpid.set(i, timecpid);
		}
	}

	/** Returns the number of elements in this ProcessTimeline. */
	public int size() {
		return listcpid.size();
	}

	/**
	 * Finds the sample to which 'time' most closely corresponds in the
	 * ProcessTimeline.
	 * 
	 * @param time
	 *            : the requested time
	 * @return the index of the sample if the time is within the range, -1
	 *         otherwise
	 * */
	// TODO: Compare the performance of this method to remoting it when remoting
	// is available. It seems like since everything is already in memory
	// locally, it should actually be pretty fast, and the network latency would
	// be too much to overcome for such a quick method call. That's why it is
	// defined here as opposed to in the local subclass.
	public int findMidpointBefore(double time) {
		int low = 0;
		int high = listcpid.size() - 1;

		// do not search the sample if the time is out of range
		if (time < listcpid.get(low).timestamp
				|| time > listcpid.get(high).timestamp)
			return -1;

		int mid = (low + high) / 2;

		while (low != mid) {
			final double time_current = getTimeMidPoint(mid, mid + 1);

			if (time > time_current)
				low = mid;
			else
				high = mid;
			mid = (low + high) / 2;

		}
		if (time >= getTimeMidPoint(low, low + 1))
			return low + 1;
		else
			return low;
	}

	private double getTimeMidPoint(int left, int right) {
		return (listcpid.get(left).timestamp + listcpid.get(right).timestamp) / 2.0;
	}

	public Vector<TimeCPID> getListOfData() {
		return this.listcpid;
	}

	public void setListOfData(Vector<TimeCPID> anotherList) {
		this.listcpid = anotherList;
	}

}