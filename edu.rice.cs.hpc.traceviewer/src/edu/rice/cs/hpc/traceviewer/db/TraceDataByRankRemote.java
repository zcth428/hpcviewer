package edu.rice.cs.hpc.traceviewer.db;

import java.util.Arrays;
import java.util.Vector;

/**
 * A TraceDataByRank that can be constructed by directly passing an array of
 * TimeCPID.
 * 
 * Note: If this class doesn't do anything else, we should consider merging
 * TraceDataByRank, TraceDataByRankLocal, and TraceDataByRankRemote together and
 * just having this constructor as an alternative constructor.
 * 
 * @author Philip Taffet
 * 
 */
public class TraceDataByRankRemote extends TraceDataByRank {

	public TraceDataByRankRemote(TimeCPID[] data) {
		super.listcpid = new Vector<TimeCPID>(Arrays.asList(data));
	}

}
