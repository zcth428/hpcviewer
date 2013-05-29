package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.db.DecompressionAndRenderThread;
import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.db.DecompressionAndRenderThread.WorkItemToDo;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeDetailCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController {

	
	final RemoteDataRetriever dataRetriever;

	public final int HEADER_SIZE;
	/**
	 * This is part of the code from TimelineThread. The separator lines are the
	 * thin white lines in between traces that show up when the view is
	 * sufficiently zoomed (when their heights are greater than this value).
	 */
	
	private final String[] valuesX;
	
	AtomicInteger lineNum;
	

	public SpaceTimeDataControllerRemote(RemoteDataRetriever _dataRet, IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, InputStream expStream, String Name, int _numTraces, String[] _valuesX) {


		attributes = new ImageTraceAttributes();
		ImageTraceAttributes oldAtributes = new ImageTraceAttributes();

		BaseExperiment exp = new ExperimentWithoutMetrics();
		try {
			//Without metrics, so param 3 is false
			exp.open(expStream, new ProcedureAliasMap(), false, Name);
		} catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line "
					+ e.getLineNumber());
			e.printStackTrace();
			// return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Height = dataTrace.getNumberOfFiles();

		buildScopeMapAndColorTable(_window, exp);

		TraceAttribute attribute = exp.getTraceAttribute();
		minBegTime = attribute.dbTimeMin;
		maxEndTime = attribute.dbTimeMax;
		HEADER_SIZE = attribute.dbHeaderSize;

		dbName = exp.getName();
		
		dataRetriever = _dataRet;
		height = _numTraces;
		valuesX = _valuesX;
		
		lineNum = new AtomicInteger(0);

		super.painter = new PaintManager(attributes, oldAtributes, _window,
				_statusMgr, colorTable, maxDepth, minBegTime);

	}


	/**
	 * dtProcess scaled to be the index in traces[] that corresponds to this
	 * process. dtProcess is in the range [0, number of files in data trace]
	 * while scaledDTProcess is in the range [0, number of vertical pixels in
	 * SpaceTimeDetailView]. If it returns 0, chances are the index it should
	 * return would be outside the array, so the 0 is a sort of safeguard.
	 */
	private int computeScaledProcess() {
		if ((getCurrentlySelectedProcess() <= attributes.endProcess) && getCurrentlySelectedProcess() >= attributes.begProcess) {
			int scaledDTProcess = (int) (((double) traces.length - 1)
					/ ((double) attributes.endProcess - attributes.begProcess - 1) * (getCurrentlySelectedProcess() - attributes.begProcess));// -atr.begPro-1??
			return scaledDTProcess;
		} else// So this means that it's in that weird state where the length of
				// traces and attributes has been updated, but the position of
				// the crosshair has not. For now, we have a bad bug fix and
				// just return 0. This may cause it to render depth trace 0
				// first before switching to 0.
		{
			System.out
					.println("Mapping skipped because of state. Returning 0.");
			return 0;
		}

	}

	@Override
	public void prepareViewportPainting(boolean changedBounds) {
		System.out.println("Calling the unimplemented prep Viewp Painting");

	}

	@Override
	void prepareDepthViewportPainting() {
		depthTrace = new ProcessTimeline(null, scopeMap, 0,
				attributes.numPixelsH, attributes.numPixelsV, minBegTime
						+ attributes.begTime);
		int scaledDTProcess = computeScaledProcess();
		depthTrace.copyDataFrom(traces[scaledDTProcess]);

	}

	@Override
	public String[] getTraceDataValuesX() {
		return valuesX;
	}
	//TODO: Figure out how to integrate this with BaseViewPaint.paint() so that it is called.
	@Override
	public void fillTraces(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds) {

		// This relies on the fact that fillTraces will always be called with
		// the DetailCanvas before the DepthViewCanvas. Can I guarantee this?
		if (canvas instanceof SpaceTimeDetailCanvas) {
			int numThreadsToLaunch = Math.min(linesToPaint, Runtime.getRuntime().availableProcessors());
			DecompressionAndRenderThread[] workThreads = new DecompressionAndRenderThread[numThreadsToLaunch];
			int RanksExpected = Math.min(attributes.endProcess-attributes.begProcess, attributes.numPixelsV);
			
			if (changedBounds)
				traces = new ProcessTimeline[RanksExpected];

			for (int i = 0; i < workThreads.length; i++) {

				workThreads[i] = new DecompressionAndRenderThread(traces, scopeMap, RanksExpected, attributes.begTime, attributes.endTime, painter, canvas, changedBounds, xscale, yscale, attributes.numPixelsH, !changedBounds);
				workThreads[i].start();
			}
			
			if (changedBounds) {
				
				try {
					/*traces = */dataRetriever.getData(attributes.begProcess,
							attributes.endProcess, attributes.begTime, attributes.endTime, //minBegTime, maxEndTime,
							attributes.numPixelsV, attributes.numPixelsH,
							scopeMap);//This will fill the workToDo queue with decompression and rendering. After the threads join, traces will be full
				} catch (IOException e) {
					// UI Notify user...
					e.printStackTrace();
				}
				
			}
			else
			{
				painter.renderTraces(traces);
			}
			//Wait until they are all done
			System.out.println("Threads launched. Waiting");
			for (int i = 0; i < workThreads.length; i++) {
				try {
					workThreads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			DecompressionAndRenderThread.workToDo = new ConcurrentLinkedQueue<WorkItemToDo>();
		} else// Depth view
		{
			renderDepthTraces(canvas, changedBounds, xscale, yscale);
		}

	}

	private void renderDepthTraces(SpaceTimeCanvas canvas,
			boolean changedBounds, double scaleX, double scaleY) {
		System.out.println("Rendering Depth Trace");
		ProcessTimeline nextTrace = getNextDepthTrace();
		int width = attributes.numPixelsH;

		while (nextTrace != null) {
			painter.renderDepthTrace(canvas, scaleX, scaleY, nextTrace, width, getDepthTrace());
			nextTrace = getNextDepthTrace();
		}
	}

	

	private ProcessTimeline getNextDepthTrace() {

		if (lineNum.get() < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			if (lineNum.compareAndSet(0, 1)) {
				return depthTrace;
			}
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(null, scopeMap,
					lineNum.get(), attributes.numPixelsH,
					attributes.numPixelsV, minBegTime + attributes.begTime);
			int scaledDTProcess = computeScaledProcess();
			toDonate.copyDataFrom(traces[scaledDTProcess]);
			// toDonate.copyDataFrom(depthTrace);

			lineNum.incrementAndGet();
			return toDonate;
		} else
			return null;
	}


	@Override
	public void dispose() {
		super.dispose();
		try {
			dataRetriever.Close();
		} catch (IOException e) {
			System.out.println("Could not close the connection.");
		}

	}


	@Override
	public IBaseData getBaseData() {
		// TODO Auto-generated method stub
		return null;
	}
}
