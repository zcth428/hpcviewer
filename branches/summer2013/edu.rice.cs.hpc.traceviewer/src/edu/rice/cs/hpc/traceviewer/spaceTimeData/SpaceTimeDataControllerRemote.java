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
import edu.rice.cs.hpc.traceviewer.db.DecompressionThread;
import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.db.DecompressionThread.WorkItemToDo;
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
	
	AtomicInteger lineNum, depthLineNum;
	

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
		depthLineNum = new AtomicInteger(0);

		super.painter = new PaintManager(attributes, oldAtributes, _window,
				_statusMgr, colorTable, maxDepth, minBegTime);

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
	/**
	 * This performs the network request and does a small amount of processing on the reply. Namely, it does not decompress the traces. Instead, it returns threads that will do that work when executed.
	 */
	public Thread[] fillTracesWithData (boolean changedBounds, int numThreadsToLaunch) {
		if (changedBounds) {
			
			DecompressionThread[] workThreads = new DecompressionThread[numThreadsToLaunch];
			int RanksExpected = Math.min(attributes.endProcess-attributes.begProcess, attributes.numPixelsV);
			
			traces = new ProcessTimeline[RanksExpected];

			for (int i = 0; i < workThreads.length; i++) {

				workThreads[i] = new DecompressionThread(traces, scopeMap, RanksExpected, attributes.begTime, attributes.endTime);
			}
			

			try {
				// This will fill the workToDo queue with decompression and
				// rendering. After the threads join, traces will be full
				dataRetriever.getData(attributes.begProcess,
						attributes.endProcess, attributes.begTime,
						attributes.endTime, attributes.numPixelsV,
						attributes.numPixelsH, scopeMap);
			} catch (IOException e) {
				// UI Notify user...
				e.printStackTrace();
			}
			return workThreads;
		}
		return new Thread[0];
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

	

	public ProcessTimeline getNextDepthTrace() {
		int depthLineNumCurrVal = depthLineNum.getAndIncrement();
		if (depthLineNumCurrVal < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			if (depthLineNumCurrVal == 0 && depthTrace != null) {
				return depthTrace;
			}
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(null, scopeMap,
					depthLineNumCurrVal, attributes.numPixelsH,
					attributes.numPixelsV, minBegTime + attributes.begTime);
			int scaledDTProcess = computeScaledProcess();
			toDonate.copyDataFrom(traces[scaledDTProcess]);
			// toDonate.copyDataFrom(depthTrace);

			return toDonate;
		} else
			return null;
	}
	
	@Override
	public void resetDepthCounter() {
		depthLineNum.set(0);
	};

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


	@Override
	public ProcessTimeline getNextTrace(boolean changedBounds) {
		int index = lineNum.getAndIncrement();
		if (index >= traces.length) return null;
		return traces[index];
	}
}
