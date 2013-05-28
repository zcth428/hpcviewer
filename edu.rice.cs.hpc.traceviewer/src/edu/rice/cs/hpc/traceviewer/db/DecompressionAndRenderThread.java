package edu.rice.cs.hpc.traceviewer.db;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;
import org.eclipse.swt.widgets.Canvas;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.PaintManager;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

//Perhaps this would all be more suited to a ThreadPool 
public class DecompressionAndRenderThread extends Thread {

	public static ConcurrentLinkedQueue<WorkItemToDo> workToDo = new ConcurrentLinkedQueue<WorkItemToDo>();
	// Variables for decompression
	final ProcessTimeline[] timelines;
	final HashMap<Integer, CallPath> scopeMap;
	final int ranksExpected;
	final double t0;
	final double tn;

	static AtomicInteger ranksRemainingToDecompress;

	// Variables for rendering
	final PaintManager painter;
	final Canvas canvas;
	final boolean changedBounds;
	final double scaleX, scaleY;
	final int width;
	//final TimelineProgressMonitor monitor;

	static AtomicInteger ranksRemainingToRender;

	public DecompressionAndRenderThread(ProcessTimeline[] _timelines,
			HashMap<Integer, CallPath> _scopeMap, int _ranksExpected,
			long _t0, long _tn, PaintManager _painter, Canvas _canvas,
			boolean _changedBounds, double _scaleX, double _scaleY, int _width,
			boolean renderOnly) {
		timelines = _timelines;
		scopeMap = _scopeMap;
		ranksExpected = _ranksExpected;
		t0 = _t0;
		tn = _tn;

		painter = _painter;
		canvas = _canvas;
		changedBounds = _changedBounds;
		scaleX = _scaleX;
		scaleY = _scaleY;
		width = _width;
		//monitor = _monitor;

		ranksRemainingToDecompress = new AtomicInteger(renderOnly ? 0: ranksExpected);
		ranksRemainingToRender = new AtomicInteger(ranksExpected);
		
	}
	@Override
	public void run() {
		while ((ranksRemainingToDecompress.get() > 0) || (ranksRemainingToRender.get() > 0))
		{
			WorkItemToDo wi = workToDo.poll();
			if (wi == null)
			{
				//There is still work that needs to get done, but it is not available to be worked on at the moment.
				//Wait a little and try again
				try {
					Thread.sleep(50);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			if (wi instanceof DecompressionItemToDo)
			{
				//System.out.println("Decompressing, rem=" + ranksRemainingToDecompress);
				ranksRemainingToDecompress.getAndDecrement();
				DecompressionItemToDo toDecomp = (DecompressionItemToDo)wi;
				try {
					decompress(toDecomp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Now that it has been compressed, make it available for rendering
				workToDo.add(new RenderItemToDo(toDecomp.rankNumber));
				continue;

			}
			if (wi instanceof RenderItemToDo)
			{
				ranksRemainingToRender.getAndDecrement();
				//System.out.println("Rendering, rem=" + ranksRemainingToRender);
				
				RenderItemToDo toRender = (RenderItemToDo)wi;
				painter.renderTrace(canvas, changedBounds, scaleX, scaleY, width,
						timelines[toRender.index]);
			}
		}
		
	}
	private void decompress(DecompressionItemToDo toDecomp) throws IOException
	{
		TimeCPID[] ranksData = readTimeCPIDArray(toDecomp.Packet, toDecomp.itemCount, toDecomp.startTime, toDecomp.endTime, toDecomp.compressed);
		TraceDataByRank dataAsTraceDBR = new TraceDataByRank(ranksData);


		int lineNumber = toDecomp.rankNumber;
		/*if (false)//if (Pn-P0 > vertRes)
			lineNumber = (int)Math.round((RankNumber-P0)*(double)vertRes/(Pn-P0));//Its like a line: P0 -> 0, the slope is number of pixels/number of ranks
		else
			lineNumber = RankNumber-P0;*/

		ProcessTimeline PTl = new ProcessTimeline(dataAsTraceDBR, scopeMap, lineNumber, ranksExpected, tn-t0, t0);
		timelines[lineNumber]= PTl;//RankNumber or RankNumber-P0??
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
	private TimeCPID[] readTimeCPIDArray(byte[] packedTraceLine, int length, double t0, double tn, boolean compressed) throws IOException {

		DataInputStream decompressor;
		if (compressed)
			decompressor= new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(packedTraceLine)));
		else
			decompressor = new DataInputStream(new ByteArrayInputStream(packedTraceLine));
		TimeCPID[] ToReturn = new TimeCPID[length];
		double deltaT = (tn-t0)/length;
		for (int i = 0; i < ToReturn.length; i++) {
			int CPID = decompressor.readInt();
			/*if (CPID <= 0)
				System.out.println("CPID too small");*/
			ToReturn[i] = new TimeCPID(t0+i*deltaT, CPID);//Does this method of getting timestamps actually work???
		}
		return ToReturn;
	}



public static class WorkItemToDo {

}
public static class DecompressionItemToDo extends WorkItemToDo {
	final byte[] Packet;
	final int itemCount;//The number of Time-CPID pairs
	final double startTime, endTime;
	final int rankNumber;
	final boolean compressed;
	public DecompressionItemToDo(byte[] _packet, int _itemCount, double _startTime, double _endTime, int _rankNumber, boolean _dataCompressed) {
		Packet = _packet;
		itemCount = _itemCount;
		startTime = _startTime;
		endTime = _endTime;
		rankNumber = _rankNumber;
		compressed = _dataCompressed;
	}
}
public static class RenderItemToDo extends WorkItemToDo {
	final int index;//The index in the Timelines array
	public RenderItemToDo(int _index) {
		index = _index;
	}
}
}
