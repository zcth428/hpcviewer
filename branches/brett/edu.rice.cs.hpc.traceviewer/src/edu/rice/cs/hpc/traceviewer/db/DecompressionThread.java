package edu.rice.cs.hpc.traceviewer.db;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

import edu.rice.cs.hpc.traceviewer.db.TraceDataByRank.Record;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.util.Constants;
import edu.rice.cs.hpc.traceviewer.util.Debugger;

//Perhaps this would all be more suited to a ThreadPool 

/*
 * Philip 5/29/13 Moved rendering code to the canvases to align the remote
 * version with changes made to the local version. This used to be responsible
 * for rendering and decompressing, but now is not. I'm keeping the WorkItemToDo
 * structure just in case this expands again. 
 * Philip 7/23/13 Got rid of WorkItemToDo. This thread looks like it's only going
 * to be for decompression now that we have a decent way to do the rendering in
 * parallel without waiting for all threads to be decompressed.
 */

public class DecompressionThread extends Thread {

	private static ConcurrentLinkedQueue<DecompressionItemToDo> workToDo = new ConcurrentLinkedQueue<DecompressionItemToDo>();
	private static ConcurrentLinkedQueue<Integer> timelinesAvailableForRendering = new ConcurrentLinkedQueue<Integer>();
	// Variables for decompression
	final ProcessTimelineService timelineServ;
	final HashMap<Integer, CallPath> scopeMap;
	final int ranksExpected;
	final long t0;
	final long tn;
	
	final static int COMPRESSION_TYPE_MASK = 0xFFFF;//Save two bytes for formatting versions
	final static short ZLIB_COMPRESSSED  = 1;
	
	static boolean first = true;

	static AtomicInteger ranksRemainingToDecompress;


	public DecompressionThread(ProcessTimelineService ptlService,
			HashMap<Integer, CallPath> _scopeMap, int _ranksExpected,
			long _t0, long _tn) {
		timelineServ = ptlService;
		scopeMap = _scopeMap;
		ranksExpected = _ranksExpected;
		t0 = _t0;
		tn = _tn;
		
	}
	
	public static void setTotalRanksExpected(int ranks){
		ranksRemainingToDecompress = new AtomicInteger(ranks);
	}
	
	public static void addWorkItemToDo(DecompressionItemToDo add) {
		workToDo.add(add);
	}
	
	/**
	 * @return The index of a {@link ProcessTimeline} that has been uncompressed
	 * and is now ready for rendering. Returns null if there aren't any yet for
	 * any reason. The unavailability may be a temporary condition that will be
	 * resolved in a moment, or it may mean that all available timelines have 
	 * been processed. Unless this method returns null, it will not return a
	 * value that it has returned before.
	 */
	public static Integer getNextTimelineToRender() {
		return timelinesAvailableForRendering.poll();
	}
	
	@Override
	public void run() {
		while (ranksRemainingToDecompress.get() > 0)
		{
			DecompressionItemToDo wi = workToDo.poll();
			if (wi == null)
			{
				//There is still work that needs to get done, but it is not available to be worked on at the moment.
				//Wait a little and try again
				try {
					Thread.sleep(50);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			if (first){
				first = false;
				Debugger.printTimestampDebug("First decompression beginning.");
			}
			ranksRemainingToDecompress.getAndDecrement();
			DecompressionItemToDo toDecomp = (DecompressionItemToDo)wi;
			try {
				decompress(toDecomp);
			} catch (IOException e) {
				Debugger.printDebug(1, "IO Exception in decompression algorithm.");
				e.printStackTrace();
			}
			continue;


		}
		
	}

	private void decompress(DecompressionItemToDo toDecomp) throws IOException
	{
		Record[] ranksData = readTimeCPIDArray(toDecomp.packet, toDecomp.itemCount, toDecomp.startTime, toDecomp.endTime, toDecomp.compressed);
		TraceDataByRank dataAsTraceDBR = new TraceDataByRank(ranksData);

		int lineNumber = toDecomp.rankNumber;

		ProcessTimeline ptl = new ProcessTimeline(dataAsTraceDBR, scopeMap, lineNumber, ranksExpected, tn-t0, t0);
		timelineServ.setProcessTimeline(lineNumber, ptl);
		timelinesAvailableForRendering.add(lineNumber);
	}

	/**
	 * Reads from the stream and creates an array of Timestamp-CPID pairs containing the data for this rank
	 * @param packedTraceLine 
	 * @param length The number of Timestamp-CPID pairs in this rank (not the length in bytes)
	 * @param t0 The start time
	 * @param tn The end time
	 * @param compressed 
	 * @return The array of data for this rank
	 * @throws IOException
	 */
	private Record[] readTimeCPIDArray(byte[] packedTraceLine, int length, long t0, long tn, int compressed) throws IOException {

		DataInputStream decompressor;
		if ((compressed & COMPRESSION_TYPE_MASK) == ZLIB_COMPRESSSED)
			decompressor= new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(packedTraceLine)));
		else
			decompressor = new DataInputStream(new ByteArrayInputStream(packedTraceLine));
		Record[] toReturn = new Record[length];
		long currentTime = t0;
		for (int i = 0; i < toReturn.length; i++) {
			// There are more efficient ways to send the timestamps. Namely,
			// instead of sending t_n - t_(n-1), we can send (t_n - t_(n-1))-T,
			// where T is the expected delta T, calculated by
			// (t_n-t_0)/(length-1). These will fit in three bytes for certain
			// and often will fit in two. Because of the gzip layer on top,
			// though, the actual savings may be marginal, which is why it is
			// implemented more simply right now. This is left as a possible
			// extension with the compression type flag.
			int deltaT = decompressor.readInt();
			currentTime += deltaT;
			int CPID = decompressor.readInt();
			/*if (CPID <= 0)
				System.out.println("CPID too small");*/
			toReturn[i] = new Record(currentTime, CPID, Constants.dataIdxNULL);
		}
		return toReturn;
	}




public static class DecompressionItemToDo {
	final byte[] packet;
	final int itemCount;//The number of Time-CPID pairs
	final long startTime, endTime;
	final int rankNumber;
	final int compressed;
	public DecompressionItemToDo(byte[] _packet, int _itemCount, long _startTime, long _endTime, int _rankNumber, int _compressionType) {
		packet = _packet;
		itemCount = _itemCount;
		startTime = _startTime;
		endTime = _endTime;
		rankNumber = _rankNumber;
		compressed = _compressionType;
	}
}
}
