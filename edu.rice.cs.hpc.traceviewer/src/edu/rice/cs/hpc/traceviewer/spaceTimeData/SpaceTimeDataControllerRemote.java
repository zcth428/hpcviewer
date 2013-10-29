package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.db.DecompressionThread;
import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController 
{	
	final RemoteDataRetriever dataRetriever;

	public final int HEADER_SIZE;
	
	private final String[] valuesX;
	

	public SpaceTimeDataControllerRemote(RemoteDataRetriever _dataRet, IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, InputStream expStream, String Name, int _numTraces, String[] _valuesX) {


		BaseExperiment exp = new ExperimentWithoutMetrics();
		try {
			// Without metrics, so param 3 is false
			exp.open(expStream, new ProcedureAliasMap(), Name);
		}
		catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line " + e.getLineNumber());
			e.printStackTrace();
			// return;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		buildScopeMapAndColorTable(_window, exp);

		TraceAttribute attribute = exp.getTraceAttribute();
		minBegTime = attribute.dbTimeMin;
		maxEndTime = attribute.dbTimeMax;
		HEADER_SIZE = attribute.dbHeaderSize;

		dbName = exp.getName();
		
		ISourceProviderService sourceProviderService = (ISourceProviderService) _window.getService(ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 
		
		dataRetriever = _dataRet;
		totalTraceCountInDB = _numTraces;
		valuesX = _valuesX;

		super.painter = new PaintManager(attributes, colorTable, maxDepth);

	}

	@Override
	public String[] getTraceNames() {
		return valuesX;
	}
	/**
	 * This performs the network request and does a small amount of processing on the reply. Namely, it does 
	 * not decompress the traces. Instead, it returns threads that will do that work when executed.
	 */
	public Thread[] fillTracesWithData (boolean changedBounds, int numThreadsToLaunch) {
		if (changedBounds) {
			
			DecompressionThread[] workThreads = new DecompressionThread[numThreadsToLaunch];
			int RanksExpected = Math.min(attributes.endProcess-attributes.begProcess, attributes.numPixelsV);
			
			ptlService.setProcessTimeline(new ProcessTimeline[RanksExpected]);

			for (int i = 0; i < workThreads.length; i++) {

				workThreads[i] = new DecompressionThread(ptlService, scopeMap, RanksExpected, attributes.begTime, attributes.endTime);
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

	
	
	@Override
	public void dispose() {
		closeDB();
		super.dispose();

	}
	
	@Override
	public void closeDB() {
		try {
			dataRetriever.closeConnection();
		} catch (IOException e) {
			System.out.println("Could not close the connection.");
		}
	}


	/** 
	 * There is no {@link IBaseData} for the remote version. The closest we have is {@link RemoteDataRetriever}, but it
	 * doesn't make sense for <code>RemoteDataRetriever</code> to <code>implement IBaseData</code>
	 */
	@Override
	public IBaseData getBaseData() {
		return null;
	}


	@Override
	public ProcessTimeline getNextTrace(boolean changedBounds) {
		int index = lineNum.getAndIncrement();
		if (index >= ptlService.getNumProcessTimeline()) return null;
		return ptlService.getProcessTimeline(index);
	}
}
