package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineThread;

/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController {

	
	ImageTraceAttributes oldAtributes;

	private BaseDataFile dataTrace;



	private int HEADER_SIZE;


	

	IStatusLineManager statusMgr;

	public static void dumpAllCounts() {
		String[] MethodNames = { "Constructor", "getNextTrace",
				"getNextDepthTrace", "addNextTrace", "getProcess",
				"launchDetailViewThreads", "lineToPaint",
				"prepareDepthViewportPainting", "prepareViewportPainting",
				"getPainter" };

		for (int i = 0; i < MethodNames.length; i++) {
			System.out.println(MethodNames[i] + ": " + MethodCounts[i]);
		}
		System.out.println();
	}

	public SpaceTimeDataControllerLocal(

	IWorkbenchWindow _window, IStatusLineManager _statusMgr, File expFile,
			File traceFile) {

		MethodCounts[0]++;

		statusMgr = _statusMgr;

		attributes = new ImageTraceAttributes();
		oldAtributes = new ImageTraceAttributes();

		try {
			dataTrace = new BaseDataFile(traceFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Master buffer could not be created");
		}

		BaseExperiment exp = new ExperimentWithoutMetrics();
		try {
			exp.open(expFile, new ProcedureAliasMap());
		} catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line "
					+ e.getLineNumber());
			e.printStackTrace();
			// return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Height = dataTrace.getNumberOfFiles();
		
		buildScopeMapAndColorTable(_window, exp);

		TraceAttribute attribute = exp.getTraceAttribute();
		minBegTime = attribute.dbTimeMin;
		maxEndTime = attribute.dbTimeMax;
		HEADER_SIZE = attribute.dbHeaderSize;

		dbName = exp.getName();

		super.painter = new PaintManager(attributes, oldAtributes, _window,
				_statusMgr, colorTable, maxDepth, minBegTime);
		
	}






	/***********************************************************************
	 * Gets the next available trace to be filled/painted
	 * 
	 * @param changedBounds
	 *            Whether or not the thread should get the data.
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextTrace(boolean changedBounds) {

		MethodCounts[1]++;

		if (attributes.lineNum < Math.min(attributes.numPixelsV,
				attributes.endProcess - attributes.begProcess)) {
			attributes.lineNum++;
			if (changedBounds)
				return new ProcessTimeline(attributes.lineNum - 1, scopeMap,
						dataTrace, lineToPaint(attributes.lineNum - 1),
						attributes.numPixelsH, attributes.endTime
								- attributes.begTime, minBegTime
								+ attributes.begTime, HEADER_SIZE);
			else {
				if (traces.length >= attributes.lineNum) {
					if (traces[attributes.lineNum - 1] == null)// Why is it
																// sometimes
																// null????
					{
						System.out.println("Was null, auto-fixing");
						traces[attributes.lineNum - 1] = new ProcessTimeline(
								attributes.lineNum - 1, scopeMap, dataTrace,
								lineToPaint(attributes.lineNum - 1),
								attributes.numPixelsH, attributes.endTime
										- attributes.begTime, minBegTime
										+ attributes.begTime, HEADER_SIZE);
					}
					return traces[attributes.lineNum - 1];
				} else
					System.err.println("STD error: trace paints "
							+ traces.length + " < line number "
							+ attributes.lineNum);
			}
		}
		return null;
	}

	/***********************************************************************
	 * Gets the next available trace to be filled/painted from the DepthTimeView
	 * 
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextDepthTrace() {

		MethodCounts[2]++;

		if (attributes.lineNum < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			if (attributes.lineNum == 0) {
				attributes.lineNum++;
				return depthTrace;
			}
			ProcessTimeline toDonate = new ProcessTimeline(attributes.lineNum,
					scopeMap, dataTrace, getCurrentlySelectedProcess(), attributes.numPixelsH,
					attributes.endTime - attributes.begTime, minBegTime
							+ attributes.begTime, HEADER_SIZE);
			toDonate.copyDataFrom(depthTrace);

			attributes.lineNum++;
			return toDonate;
		} else
			return null;
	}

	/** Adds a filled ProcessTimeline to traces - used by TimelineThreads. */

	// Does this need to be part of the superclass? I think the remote way would
	// not need this.

	public synchronized void addNextTrace(ProcessTimeline nextPtl) {
		MethodCounts[3]++;
		if (nextPtl == null)
			System.out.println("Saving a null PTL?");
		traces[nextPtl.line()] = nextPtl;
	}

	/*************************************************************************
	 * Returns the process that has been specified.
	 ************************************************************************/
	@Override
	public ProcessTimeline getProcess(int process) {
		MethodCounts[4]++;
		int relativeProcess = process - attributes.begProcess;

		// in case of single process displayed
		if (relativeProcess >= traces.length)
			relativeProcess = traces.length - 1;

		return traces[relativeProcess];
	}

	// From BaseViewPaint
	/**
	 * This fills the Traces array with the proper ProcessTimelines. In the
	 * local implementation, it launches n TimelineThreads, where n is the
	 * available number of processors on the local computer, which then do the
	 * actual work of creating the ProcessTimelines.
	 */
	@Override
	public void fillTraces(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds) {
		System.out.println("Lines to Paint: "+ linesToPaint+ ", attr.max-min: "+ (attributes.endProcess - attributes.endProcess));
		MethodCounts[5]++;

		final int num_threads = Math.min(linesToPaint, Runtime.getRuntime()
				.availableProcessors());
		attributes.lineNum = 0;

		TimelineProgressMonitor monitor = new TimelineProgressMonitor(statusMgr);
		
		TimelineThread[] threads = new TimelineThread[num_threads];
		for (int threadNum = 0; threadNum < threads.length; threadNum++) {
			threads[threadNum] = new TimelineThread(this, changedBounds,
					canvas, attributes.numPixelsH, xscale, yscale, monitor);
			threads[threadNum].start();
		}

		int numThreads = threads.length;
		try {
			// listen all threads (one by one) if they are all finish
			// somehow, a thread can be alive forever waiting to lock a
			// resource,
			// especially when we resize the window. this approach should reduce
			// deadlock by polling each thread
			while (numThreads > 0) {
				for (TimelineThread thread : threads) {
					if (thread.isAlive()) {
						monitor.reportProgress();
					} else {
						if (!thread.getName().equals("end")) {
							numThreads--;
							// mark that this thread has ended
							thread.setName("end");
						}
					}
					Thread.sleep(30);
				}
			}
			/*
			 * for (int threadNum = 0; threadNum < threads.length; threadNum++)
			 * { while (threads[threadNum].isAlive()) { Thread.sleep(30);
			 * monitor.reportProgress(); } }
			 */
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/** Returns the index of the file to which the line-th line corresponds. */

	private int lineToPaint(int line) {
		MethodCounts[6]++;
		int numTimelinesToPaint = attributes.endProcess - attributes.begProcess;
		if (numTimelinesToPaint > attributes.numPixelsV)
			return attributes.begProcess + (line * numTimelinesToPaint)
					/ (attributes.numPixelsV);
		else
			return attributes.begProcess + line;
	}


	@Override
	//The only part of TraceData that is needed externally is the list of processes, so we do not need to expose the whole TraceData
	public String[] getTraceDataValuesX() {
		return this.dataTrace.getValuesX();
	}

	@Override
	/**
	 * Makes and fills the depthTrace ProcessTimeline
	 */
	void prepareDepthViewportPainting() {
		MethodCounts[7]++;
		int lineNum = 0;// ?? It doesn't seem like lineNum is changed in the
						// BaseViewPaint, but I'm not sure if it is changed
						// elsewhere.
		depthTrace = new ProcessTimeline(lineNum, scopeMap, dataTrace,
				getCurrentlySelectedProcess(), attributes.numPixelsH, attributes.endTime
						- attributes.begTime, minBegTime + attributes.begTime);

		depthTrace.readInData();
		depthTrace.shiftTimeBy(minBegTime);
		
	}

	@Override
	public void prepareViewportPainting(boolean changedBounds) {
		MethodCounts[8]++;
		if (changedBounds) {
			int numTraces = Math.min(attributes.numPixelsV,
					attributes.endProcess - attributes.begProcess);
			traces = new ProcessTimeline[numTraces];
		}
	}

	@Override
	public IBaseData getBaseData() {
		// TODO Auto-generated method stub
		return null;
	}

}
