package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.RemoteFilteredBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.extdata.TraceName;
import edu.rice.cs.hpc.traceviewer.db.DecompressionThread;
import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController 
{	
	final RemoteDataRetriever dataRetriever;

	private final int headerSize;
	private final TraceName[]  valuesX;
	private final DataOutputStream server;

	public SpaceTimeDataControllerRemote(RemoteDataRetriever _dataRet, IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, InputStream expStream, String Name, int _numTraces, TraceName[] valuesX, DataOutputStream connectionToServer) {


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
		headerSize = attribute.dbHeaderSize;

		dbName = exp.getName();
		
		ISourceProviderService sourceProviderService = (ISourceProviderService) _window.getService(ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 
		
		dataRetriever = _dataRet;
		totalTraceCountInDB = _numTraces;
		this.valuesX = valuesX;
		server = connectionToServer;

		super.painter = new PaintManager(attributes, colorTable, maxDepth);
		super.dataTrace = createFilteredBaseData();
	}

	
	@Override
	public IFilteredData createFilteredBaseData() {
		return new RemoteFilteredBaseData(valuesX, headerSize, server);
	}

	/**
	 * This performs the network request and does a small amount of processing on the reply. Namely, it does 
	 * not decompress the traces. Instead, it returns threads that will do that work when executed.
	 */
	@Override
	public void fillTracesWithData (boolean changedBounds, int numThreadsToLaunch) {
		if (changedBounds) {
			
			DecompressionThread[] workThreads = new DecompressionThread[numThreadsToLaunch];
			int ranksExpected = Math.min(attributes.endProcess-attributes.begProcess, attributes.numPixelsV);
			
			DecompressionThread.setTotalRanksExpected(ranksExpected);
			ptlService.setProcessTimeline(new ProcessTimeline[ranksExpected]);

			for (int i = 0; i < workThreads.length; i++) {

				workThreads[i] = new DecompressionThread(ptlService, scopeMap, ranksExpected, attributes.begTime, attributes.endTime);
				workThreads[i].start();
			}
			

			try {

				dataRetriever.getData(attributes.begProcess,
						attributes.endProcess, attributes.begTime,
						attributes.endTime, attributes.numPixelsV,
						attributes.numPixelsH, scopeMap);
			} catch (IOException e) {
				// UI Notify user...
				e.printStackTrace();
			}
		}
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

	@Override
	public ProcessTimeline getNextTrace(boolean changedBounds) {
		Integer nextIndex;

		if (changedBounds) {
			// TODO: Should this be implemented with real locking?
			while ((nextIndex = DecompressionThread.getNextTimelineToRender()) == null) {
				//Make sure a different thread didn't get the last one while 
				//this thread was waiting:
				if (lineNum.get() >= ptlService.getNumProcessTimeline())
					return null;
				try {
					Thread.sleep(50);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lineNum.getAndIncrement();
		}
		else{
			nextIndex = lineNum.getAndIncrement();
			if (nextIndex >= ptlService.getNumProcessTimeline())
				return null;
		}
		return ptlService.getProcessTimeline(nextIndex.intValue());
	}


	public int getHeaderSize() {
		return headerSize;
	}
}
