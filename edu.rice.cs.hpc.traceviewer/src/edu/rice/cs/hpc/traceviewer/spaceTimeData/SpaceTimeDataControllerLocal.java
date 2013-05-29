package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
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

	private IBaseData dataTrace;

	private TraceAttribute trAttribute;

	private int headerSize;

	// We probably want to get away from this. The code that needs it should be
	// in one of the threads. It's here so that both local and remote can use
	// the same thread class yet get their information differently.
	private AtomicInteger lineNum;

	private final File traceFile;

	IStatusLineManager statusMgr;
	IWorkbenchWindow window;
	
	final private ProcessTimelineService ptlService;

	/*public static void dumpAllCounts() {
		String[] MethodNames = { "Constructor", "getNextTrace",
				"getNextDepthTrace", "addNextTrace", "getProcess",
				"launchDetailViewThreads", "lineToPaint",
				"prepareDepthViewportPainting", "prepareViewportPainting",
				"getPainter" };

		for (int i = 0; i < MethodNames.length; i++) {
			System.out.println(MethodNames[i] + ": " + MethodCounts[i]);
		}
		System.out.println();
	}*/

	public SpaceTimeDataControllerLocal(

	IWorkbenchWindow _window, IStatusLineManager _statusMgr, File expFile,
			File _traceFile) {

		lineNum = new AtomicInteger(0);

		statusMgr = _statusMgr;

		attributes = new ImageTraceAttributes();
		oldAtributes = new ImageTraceAttributes();
		
		traceFile = _traceFile;
		
		window = _window;

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
		
		
		buildScopeMapAndColorTable(_window, exp);

		trAttribute = exp.getTraceAttribute();
		minBegTime = trAttribute.dbTimeMin;
		maxEndTime = trAttribute.dbTimeMax;
		headerSize = trAttribute.dbHeaderSize;
		
		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
				ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.
				getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 

		dbName = exp.getName();
		try {
			dataTrace = new BaseData(traceFile.getAbsolutePath(), trAttribute.dbHeaderSize, TraceAttribute.DEFAULT_RECORD_SIZE);
		} catch (IOException e) {
			System.err.println("Master buffer could not be created");
		}
		height = dataTrace.getNumberOfRanks();
		
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
	@Override
	public synchronized ProcessTimeline getNextTrace(boolean changedBounds) {

		if (lineNum.get() < Math.min(attributes.numPixelsV,
				attributes.endProcess - attributes.begProcess)) {
			int oldLineNum = lineNum.getAndIncrement();
			if (changedBounds)
				return new ProcessTimeline(oldLineNum, scopeMap,
						dataTrace, lineToPaint(oldLineNum),
						attributes.numPixelsH, attributes.endTime
								- attributes.begTime, minBegTime
								+ attributes.begTime);
			else {
				if (traces.length > oldLineNum) {
					if (traces[oldLineNum] == null)// Why is it
																// sometimes
																// null????
					{
						System.out.println("Was null, auto-fixing");
						traces[oldLineNum] = new ProcessTimeline(
								oldLineNum, scopeMap, dataTrace,
								lineToPaint(oldLineNum),
								attributes.numPixelsH, attributes.endTime
										- attributes.begTime, minBegTime
										+ attributes.begTime);
					}
					return traces[oldLineNum];
				} else
					System.err.println("STD error: trace paints "
							+ traces.length + " < line number "
							+ oldLineNum);
			}
		}
		return null;
	}

	/***********************************************************************
	 * Gets the next available trace to be filled/painted from the DepthTimeView
	 * 
	 * @return The next trace.
	 **********************************************************************/
	//TODO: Make this actually synchronized and parallel-safe
	public synchronized ProcessTimeline getNextDepthTrace() {

		if (lineNum.get() < Math.min(attributes.numPixelsDepthV, maxDepth)) {
			if (lineNum.compareAndSet(0, 1)) {
				return depthTrace;
			}
			ProcessTimeline toDonate = new ProcessTimeline(lineNum.get(),
					scopeMap, dataTrace, getCurrentlySelectedProcess(), attributes.numPixelsH,
					attributes.endTime - attributes.begTime, minBegTime
							+ attributes.begTime);
			toDonate.copyDataFrom(depthTrace);

			lineNum.incrementAndGet();
			return toDonate;
		} else
			return null;
	}

	/** Adds a filled ProcessTimeline to traces - used by TimelineThreads. */

	// Does this need to be part of the superclass? I think the remote way would
	// not need this.

	public synchronized void addNextTrace(ProcessTimeline nextPtl) {
		//if (nextPtl == null)
		//	System.out.println("Saving a null PTL?");
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
	
	
	
	/***
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param baseData
	 */
	public void setBaseData(IBaseData baseData) 
	{
		dataTrace = baseData;
		
		// we have to change the range of displayed processes
		attributes.begProcess = 0;
		
		// hack: for unknown reason, "endProcess" is exclusive.
		// TODO: we should change to inclusive just like begProcess
		attributes.endProcess = baseData.getNumberOfRanks();
		
		painter.resetPosition();
	}


	@Override
	//The only part of TraceData that is needed externally is the list of processes, so we do not need to expose the whole TraceData
	public String[] getTraceDataValuesX() {
		return this.dataTrace.getListOfRanks();
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
		return dataTrace;
	}
	
	public String getTraceFileAbsolutePath(){
		return traceFile.getAbsolutePath();
	}

	public TraceAttribute getTraceAttribute() {
		return trAttribute;
	}
	@Override
	public void dispose() {
		dataTrace.dispose();
		super.dispose();
	}

}
