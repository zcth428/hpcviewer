package edu.rice.cs.hpc.traceviewer.db.local;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.data.db.TraceDataByRank;
import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.data.version2.BaseData;
import edu.rice.cs.hpc.traceviewer.data.version2.FilteredBaseData;
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
	final static private int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
	final static public int RECORD_SIZE    = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;
	private String traceFilePath;
	final private IFileDB fileDB;

	/************************
	 * Constructor to setup local database
	 * 
	 * @param _window : the current active window
	 * @param databaseDirectory : database directory
	 * 
	 * @throws InvalExperimentException
	 * @throws Exception
	 */
	public SpaceTimeDataControllerLocal(IWorkbenchWindow _window, IStatusLineManager statusMgr, 
			String databaseDirectory, IFileDB fileDB) 
			throws InvalExperimentException, Exception 
	{
		super(_window, new File(databaseDirectory));
		
		final TraceAttribute trAttribute = exp.getTraceAttribute();		
		final int version = exp.getMajorVersion();
		if (version == 2)
		{	// original format
			traceFilePath = getTraceFile(exp.getDefaultDirectory().getAbsolutePath(), statusMgr);
			
		} else if (version == 3) 
		{
			// new format
			traceFilePath = exp.getDefaultDirectory() + File.separator + exp.getDbFilename(BaseExperiment.Db_File_Type.DB_TRACE);
		}
		this.fileDB = fileDB;
		this.fileDB.open(traceFilePath, trAttribute.dbHeaderSize, RECORD_SIZE);
		dataTrace 	= new BaseData(fileDB);
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
	static private String getTraceFile(String directory, final IStatusLineManager statusMgr)
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
		
		// retrieve the current processing line, and atomically increment so that 
		// other threads will not increment at the same time
		// if the current line reaches the number of traces to render, we are done
		
		int currentLineNum = lineNum.getAndIncrement();
		if (currentLineNum < tracesToRender) {
			
			if (ptlService.getNumProcessTimeline() == 0)
				ptlService.setProcessTimeline(new ProcessTimeline[tracesToRender]);
			
			if (changedBounds) {
				ProcessTimeline currentTimeline = new ProcessTimeline(currentLineNum, getScopeMap(),
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
	

	@Override
	public IFilteredData createFilteredBaseData() {
		try{
			return new FilteredBaseData(fileDB, 
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
