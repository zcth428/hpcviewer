package edu.rice.cs.hpc.traceviewer.db;

public class TraceDataByRankRemote extends TraceDataByRank {

	// How do we want to do this. For right now, we will make a network call for
	// each rank, but that is very inefficient. If we make one call in the form
	// of GetData(P_0, P_n, t_0, t_m) instead of n calls of GetData(P_k, t_0,
	// t_m) then have less network latency and more information available at
	// once for the supercomputer to distribute to all the processors.
	// Ideally, this method should query a cache and 

	@Override
	public void getData(double timeStart, double timeRange, double pixelLength) {
		// TODO Auto-generated method stub

	}

}
