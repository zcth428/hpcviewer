package edu.rice.cs.hpc.traceviewer.db;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/**
 * An abstract class whose only role is to create a SpaceTimeDataController by opening the database.
 * This is extended by LocalDBOpener and RemoteDBOpener
 * @author Philip Taffet
 *
 */
public abstract class AbstractDBOpener {
	
	protected final static int MIN_TRACE_SIZE = 32 + 8 + 24
			+ TraceDataByRankLocal.SIZE_OF_TRACE_RECORD * 2;
	
	abstract SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window, String[] args,
		    IStatusLineManager statusMgr);
}
