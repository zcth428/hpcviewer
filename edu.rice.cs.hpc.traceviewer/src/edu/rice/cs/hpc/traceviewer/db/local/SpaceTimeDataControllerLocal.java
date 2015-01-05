package edu.rice.cs.hpc.traceviewer.db.local;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseData;
import edu.rice.cs.hpc.data.experiment.extdata.FilteredBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.data.db.TraceDataByRank;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.util.TraceProgressReport;

/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	protected final static int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
	private String traceFilePath;

	public SpaceTimeDataControllerLocal(IWorkbenchWindow _window, String databaseDirectory) 
			throws InvalExperimentException, Exception 
	{
		super(_window, new File(databaseDirectory));
	}


	/*********************
	 * Start reading and initializing trace file
	 * 
	 * @param _window : current window instant
	 * @param _statusMgr : the window's status manager
	 * 
	 * @return true if the trace file exists, false otherwise
	 *********************/
	public boolean setupTrace(IWorkbenchWindow _window, IStatusLineManager _statusMgr)
	{
		final TraceAttribute trAttribute = exp.getTraceAttribute();

		if (trAttribute.dbGlob.charAt(0) == '*')
		{	// original format
			traceFilePath = getTraceFile(exp.getDefaultDirectory().getAbsolutePath(), _statusMgr);
			
		} else 
		{
			// new format
			traceFilePath = exp.getDefaultDirectory() + File.separator + trAttribute.dbGlob;
		}
		if (traceFilePath != null)
		{
			try {
				dataTrace = new BaseData(traceFilePath, trAttribute.dbHeaderSize, 24);
				return true;
				
			} catch (IOException e) {
				MessageDialog.openError(_window.getShell(), "I/O Error", e.getMessage());
				System.err.println("Master buffer could not be created");
			}
		}
		return false;
	}
	
	/*********************
	 * get the absolute path of the trace file (experiment.mt).
	 * If the file doesn't exist, it is possible it is not merged yet 
	 *  (in this case we'll merge them automatically)
	 * 
	 * @param directory
	 * @param statusMgr
	 * @return
	 *********************/
	private String getTraceFile(String directory,	final IStatusLineManager statusMgr)
	{
		try {
			statusMgr.setMessage("Merging traces ...");

			final TraceProgressReport traceReport = new TraceProgressReport(
					statusMgr);
			final String outputFile = directory
					+ File.separatorChar + "experiment.mt";
			
			File dirFile = new File(directory);
			final MergeDataFiles.MergeDataAttribute att = MergeDataFiles
					.merge(dirFile, "*.hpctrace", outputFile,
							traceReport);
			
			if (att != MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
				File fileTrace = new File(outputFile);
				if (fileTrace.length() > MIN_TRACE_SIZE) {
					return fileTrace.getAbsolutePath();
				}
				
				System.err.println("Warning! Trace file "
						+ fileTrace.getName()
						+ " is too small: "
						+ fileTrace.length() + "bytes .");
			}
			System.err
					.println("Error: trace file(s) does not exist or fail to open "
							+ outputFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
					exp.getTraceAttribute().dbHeaderSize, TraceAttribute.DEFAULT_RECORD_SIZE);
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public String getTraceFileAbsolutePath(){
		return traceFilePath;
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


	@Override
	public String getName() {
		return exp.getDefaultDirectory().getPath();
	}

}
