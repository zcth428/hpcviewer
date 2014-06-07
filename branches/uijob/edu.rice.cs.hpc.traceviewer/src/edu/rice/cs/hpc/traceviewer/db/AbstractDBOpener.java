package edu.rice.cs.hpc.traceviewer.db;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.data.db.TraceDataByRank;

/**
 * An interface for the DBOpeners. Specifically, it is implemented by
 * {@link RemoteDBOpener} and {@link LocalDBOpener}. Its main purpose is to
 * create a {@link SpaceTimeDataController} from the connection to the database
 * (be it local or remote), but it also partially handles closing that connection.
 * 
 * @author Philip Taffet
 * 
 */
public abstract class AbstractDBOpener {

	final static int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
	protected String errorMessage="";
	
	public String getErrorMessage(){
		return errorMessage;
	}

	/**
	 * This prepares the database for retrieving data and creates a
	 * SpaceTimeDataController from that data. The local implementation
	 * (LocalDBOpener) should return a SpaceTimeDataControllerLocal while the
	 * remote implementation (RemoteDBOpener) should return a
	 * SpaceTimeDataControllerRemote.
	 * 
	 * @param window
	 * @param args
	 *            The command line arguments used to start the application
	 * @param statusMgr
	 * @return
	 */
	abstract SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window, String[] args,
			IStatusLineManager statusMgr);

	// Our current policy on closing: Except for back-to-back connections to the
	// same server, we should close the server when we are making a new
	// connection, local or remote.

}
