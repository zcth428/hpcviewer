package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	ImageTraceAttributes oldAtributes;

	private TraceAttribute trAttribute;

	private final File traceFile;

	IStatusLineManager statusMgr;
	IWorkbenchWindow window;

	public SpaceTimeDataControllerLocal(IWorkbenchWindow _window, IStatusLineManager _statusMgr, 
			File expFile, File _traceFile) {

		statusMgr = _statusMgr;

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
		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
				ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.
				getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 

		dbName = exp.getName();
		try {
			dataTrace = new BaseData(traceFile.getAbsolutePath(), trAttribute.dbHeaderSize, 24);
		} catch (IOException e) {
			System.err.println("Master buffer could not be created");
		}
		totalTraceCountInDB = dataTrace.getNumberOfRanks();
		
		super.painter = new PaintManager(attributes, colorTable, maxDepth);
		
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
		int tracesToRender = Math.min(attributes.numPixelsV, attributes.endProcess - attributes.begProcess);
		
		
		if (lineNum.get() < tracesToRender) {
			int currentLineNum = lineNum.getAndIncrement();
			
			if (ptlService.getNumProcessTimeline() == 0)
				ptlService.setProcessTimeline(new ProcessTimeline[tracesToRender]);
			
			if (changedBounds) {
				ProcessTimeline currentTimeline = new ProcessTimeline(currentLineNum, scopeMap,
						dataTrace, lineToPaint(currentLineNum),
						attributes.numPixelsH, attributes.endTime - attributes.begTime, minBegTime + attributes.begTime);
				
				ptlService.setProcessTimeline(currentLineNum, currentTimeline);
				return currentTimeline;
			}
			return ptlService.getProcessTimeline(currentLineNum);
		}
		return null;
	}

	
	/** Returns the index of the file to which the line-th line corresponds. */

	private int lineToPaint(int line) {
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
	public String[] getTraceNames() {
		return this.dataTrace.getListOfRanks();
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
	public void closeDB() {
		dataTrace.dispose();
	}
	
	@Override
	public void dispose() {
		closeDB();
		super.dispose();
	}

}
