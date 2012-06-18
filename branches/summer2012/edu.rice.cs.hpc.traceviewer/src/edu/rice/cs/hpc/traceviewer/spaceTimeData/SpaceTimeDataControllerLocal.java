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
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineThread;

/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal {

	public final ImageTraceAttributes attributes;
	ImageTraceAttributes oldAtributes;

	private PaintManager painter;

	private BaseDataFile dataTrace;

	/** The map between the nodes and the cpid's. */
	private HashMap<Integer, CallPath> scopeMap;

	/** The maximum depth of any single CallStackSample in any trace. */
	private int maxDepth;

	/**
	 * The minimum beginning and maximum ending time stamp across all traces (in
	 * microseconds)).
	 */
	private long minBegTime;
	private long maxEndTime;

	private ProcessTimeline traces[];

	private String dbName;

	private int HEADER_SIZE;

	ProcessTimeline depthTrace;

	private int dtProcess;
	
	IStatusLineManager statusMgr;

	public SpaceTimeDataControllerLocal(
			// ?
			IWorkbenchWindow _window, IStatusLineManager _statusMgr,
			File expFile, File traceFile) {
		
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
			//return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);

		// This probably isn't the best way. It seems like ColorTable should be
		// created and initialized by the PaintManager, however initializing the
		// ColorTable requires the experiment file, which the PaintManager
		// should not have because it might be done differently when the data is
		// fetched remotely. Additionally the STDController needs MaxDepth along
		// with PaintManager.
		ColorTable colorTable = new ColorTable(_window.getShell().getDisplay());
		// Initializes the CSS that represents time values outside of the
		// time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);

		TraceAttribute attribute = exp.getTraceAttribute();
		minBegTime = attribute.dbTimeMin;
		maxEndTime = attribute.dbTimeMax;
		HEADER_SIZE = attribute.dbHeaderSize;

		dbName = exp.getName();

		painter = new PaintManager(attributes, oldAtributes, _window,
				_statusMgr, colorTable, maxDepth, minBegTime, this);
	}

	public String getName() {
		return this.dbName;
	}

	/***********************************************************************
	 * Gets the next available trace to be filled/painted
	 * 
	 * @param changedBounds
	 *            Whether or not the thread should get the data.
	 * @return The next trace.
	 **********************************************************************/
	public synchronized ProcessTimeline getNextTrace(boolean changedBounds) {

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
				if (traces.length >= attributes.lineNum)
					return traces[attributes.lineNum - 1];
				else
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
		if (attributes.lineNum < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			if (attributes.lineNum == 0) {
				attributes.lineNum++;
				return depthTrace;
			}
			ProcessTimeline toDonate = new ProcessTimeline(attributes.lineNum,
					scopeMap, dataTrace, dtProcess, attributes.numPixelsH,
					attributes.endTime - attributes.begTime, minBegTime
							+ attributes.begTime, HEADER_SIZE);
			toDonate.copyData(depthTrace);

			attributes.lineNum++;
			return toDonate;
		} else
			return null;
	}

	// KEEP
	/** Adds a filled ProcessTimeline to traces - used by TimelineThreads. */

	// KEEP
	public synchronized void addNextTrace(ProcessTimeline nextPtl) {
		traces[nextPtl.line()] = nextPtl;
	}

	/*************************************************************************
	 * Returns the process that has been specified.
	 ************************************************************************/
	public ProcessTimeline getProcess(int process) {
		int relativeProcess = process - attributes.begProcess;

		// in case of single process displayed
		if (relativeProcess >= traces.length)
			relativeProcess = traces.length - 1;

		return traces[relativeProcess];
	}

	// From BaseViewPaint
	public void launchDetailViewThreads(SpaceTimeCanvas canvas,
			int linesToPaint, double xscale, double yscale, boolean changedBounds) {

		final int num_threads = Math.min(linesToPaint, Runtime.getRuntime()
				.availableProcessors());

		TimelineProgressMonitor monitor = new TimelineProgressMonitor(statusMgr );//FIXME:This will probably break the status reporting by creating a new one instead of using the existing one.
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

	/*************************************************************************
	 * Returns width of the spaceTimeData: The width (the last time in the
	 * ProcessTimeline) of the longest ProcessTimeline.
	 ************************************************************************/
	public long getWidth() {
		return maxEndTime - minBegTime;
	}

	/** Returns the index of the file to which the line-th line corresponds. */

	public int lineToPaint(int line) {
		int numTimelinesToPaint = attributes.endProcess - attributes.begProcess;
		if (numTimelinesToPaint > attributes.numPixelsV)
			return attributes.begProcess + (line * numTimelinesToPaint)
					/ (attributes.numPixelsV);
		else
			return attributes.begProcess + line;
	}

	/******************************************************************************
	 * Returns number of processes (ProcessTimelines) held in this
	 * SpaceTimeData.
	 ******************************************************************************/
	public int getHeight() {
		return dataTrace.getNumberOfFiles();
	}

	public BaseDataFile getTraceData() {
		return this.dataTrace;
	}

	void prepareDepthViewportPainting() {
		int lineNum = 0;// ?? It doesn't seem like lineNum is changed in the
						// BaseViewPaint, but I'm not sure if it is changed
						// elsewhere.
		depthTrace = new ProcessTimeline(lineNum, scopeMap, dataTrace,
				dtProcess, attributes.numPixelsH, attributes.endTime
						- attributes.begTime, minBegTime + attributes.begTime,
				HEADER_SIZE);

		depthTrace.readInData();
		depthTrace.shiftTimeBy(minBegTime);
		// depthTrace;
	}

	public void prepareViewportPainting(boolean changedBounds) {
		if (changedBounds) {
			int numTraces = Math.min(attributes.numPixelsV,
					attributes.endProcess - attributes.begProcess);
			traces = new ProcessTimeline[numTraces];
		}
	}

	/**
	 * Returns a trace from Traces[x]
	 */
	public ProcessTimeline getTrace(int process) {
		return traces[process];
	}

	public ProcessTimeline getDepthTrace() {
		return depthTrace;
	}
	
	public PaintManager getPainter()
	{
		return painter;
	}

	// FIXME: This is bad structure. The controller should receive this
	// notification without needing the PaintManager to notify it of the event.
	public void setCurrentlySelectedProccess(int ProcessNumber) {
		dtProcess = ProcessNumber;
	}
}
