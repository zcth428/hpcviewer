package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.FilteredBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

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

	public SpaceTimeDataControllerLocal(

	IWorkbenchWindow _window, IStatusLineManager _statusMgr, File expFile,
			File _traceFile) {

		super(_window);
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
	public ProcessTimeline getNextTrace(boolean changedBounds) {
		
		int tracesToRender = Math.min(attributes.numPixelsV, attributes.getProcessInterval());
		
		if (lineNum.get() < tracesToRender) {
			int currentLineNum = lineNum.getAndIncrement();
			
			if (ptlService.getNumProcessTimeline() == 0)
				ptlService.setProcessTimeline(new ProcessTimeline[tracesToRender]);
			
			if (changedBounds) {
				ProcessTimeline currentTimeline = new ProcessTimeline(currentLineNum, scopeMap,
						dataTrace, lineToPaint(currentLineNum),
						attributes.numPixelsH, attributes.getTimeInterval(), 
						minBegTime + attributes.getTimeBegin());
				
				ptlService.setProcessTimeline(currentLineNum, currentTimeline);
				return currentTimeline;
			}

			return ptlService.getProcessTimeline(currentLineNum);
		}
		return null;
	}

	
	/** Returns the index of the file to which the line-th line corresponds. */

	private int lineToPaint(int line) {

		int numTimelinesToPaint = attributes.getProcessInterval();
		if (numTimelinesToPaint > attributes.numPixelsV)
			return attributes.getProcessBegin() + (line * numTimelinesToPaint)
					/ (attributes.numPixelsV);
		else
			return attributes.getProcessBegin() + line;
	}
	
	
	/***
	 * changing the trace data, caller needs to make sure to refresh the views
	 * @param baseData
	 */
	public void setBaseData(IBaseData baseData) 
	{
		dataTrace = baseData;
		
		// -------------------------------------------------------------
		// we have to change the range of displayed processes
		// -------------------------------------------------------------
		// hack: for unknown reason, "endProcess" is exclusive.
		// TODO: we should change to inclusive just like begProcess
		attributes.setProcess(0, baseData.getNumberOfRanks());
	}



	@Override
	public IFilteredData createFilteredBaseData() {
		try{
			return new FilteredBaseData(getTraceFileAbsolutePath(), 
					trAttribute.dbHeaderSize, TraceAttribute.DEFAULT_RECORD_SIZE);
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
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


	public void fillTracesWithData(boolean changedBounds, int numThreadsToLaunch) {
		//No need to do anything. The data for local is gotten from the file
		//on demand on a per-timeline basis.
	}

}
