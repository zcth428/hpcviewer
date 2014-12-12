package edu.rice.cs.hpc.traceviewer.db;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.traceviewer.actions.OptionMidpoint;
import edu.rice.cs.hpc.traceviewer.db.local.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.remote.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.depth.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.main.HPCTraceView;
import edu.rice.cs.hpc.traceviewer.misc.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.summary.HPCSummaryView;
import edu.rice.cs.hpc.traceviewer.ui.OpenDatabaseDialog;


/*************************************************************************************
 * 
 * Class to manage trace database: opening and detecting the *.hpctrace files
 * 
 *************************************************************************************/
public class TraceDatabase 
{
	static private HashMap<IWorkbenchWindow, TraceDatabase> listOfDatabases = null;

	private SpaceTimeDataController dataTraces = null;
	private AbstractDBOpener opener = null;

	/***
	 * get the instance of this class
	 * 
	 * @param _window
	 * @return
	 */
	static public TraceDatabase getInstance(IWorkbenchWindow _window) {
		if (listOfDatabases == null) {
			listOfDatabases = new HashMap<IWorkbenchWindow, TraceDatabase>();
			TraceDatabase data = new TraceDatabase();
			listOfDatabases.put(_window, data);
			return data;
		} else {
			TraceDatabase data = listOfDatabases.get(_window);
			if (data == null) {
				data = new TraceDatabase();
				listOfDatabases.put(_window, data);
			}
			return data;
		}
	}

	/**
	 * remove instance and its resources
	 */
	static public void removeInstance(IWorkbenchWindow _window) {

		if (listOfDatabases != null) {
			final TraceDatabase data = listOfDatabases.get(_window);
			if (data == null) return;
			if (data.dataTraces != null) {
				data.dataTraces.closeDB();
				data.dataTraces.dispose();
			}
			listOfDatabases.remove(_window);
		}
	}

	
	
	/******
	 * get a new database opener
	 * 
	 * @param info
	 * @return
	 */
	private AbstractDBOpener getDBOpener(DatabaseAccessInfo info)
	{
		if (info.isLocal())
		{
			opener = new LocalDBOpener(info.databasePath);
		} else 
		{
			opener = new RemoteDBOpener(info);
		}
		return opener;
	}
	
	/******
	 * Opening a local database
	 * 
	 * @param window
	 * @param database : path to the local database
	 * @param statusMgr
	 * @return
	 */
	static public boolean openDatabase(IWorkbenchWindow window,
			final String database, IStatusLineManager statusMgr) 
	{
		DatabaseAccessInfo info = new DatabaseAccessInfo();
		info.databasePath = database;
		
		return openDatabase(window, statusMgr, info);
	}
	
	
	/***
	 * general static function to load a database by showing open dialog box
	 * and and display the views (if everything goes fine)
	 * 
	 * @param window
	 * @param statusMgr
	 * 
	 * @return true if the opening is successful. False otherwise
	 */
	static public boolean openDatabase(IWorkbenchWindow window, IStatusLineManager statusMgr) 
	{	
		OpenDatabaseDialog dlg = new OpenDatabaseDialog(window.getShell(), statusMgr, null);
		if (dlg.open() == Window.CANCEL)
			return false;
		
		DatabaseAccessInfo info = dlg.getDatabaseAccessInfo();
		return openDatabase(window, statusMgr, info);
	}


	/*******
	 * Opening a database with a specific database access info {@link DatabaseAccessInfo}.
	 * If the opening is not successful, it tries to ask again to the user the info 
	 * 
	 * @param window
	 * @param statusMgr
	 * @param info
	 * @return
	 */
	static private boolean openDatabase(IWorkbenchWindow window, IStatusLineManager statusMgr, 
			DatabaseAccessInfo info)
	{
		SpaceTimeDataController stdc = null;
		TraceDatabase database = TraceDatabase.getInstance(window);
		String message = null;
		DatabaseAccessInfo database_info = info;
		
		do {
			database.opener = database.getDBOpener(database_info);
			
			try {
				stdc = database.opener.openDBAndCreateSTDC(window, statusMgr);
			} catch (Exception e) 
			{
				stdc    = null;
				message = e.getMessage();
				
				OpenDatabaseDialog dlg = new OpenDatabaseDialog(window.getShell(), statusMgr, message);
				if (dlg.open() == Window.CANCEL)
					return false;
				
				database_info = dlg.getDatabaseAccessInfo();
			}

		} while (stdc == null);
		
		// remove old resources
		if (database.dataTraces != null)
			database.dataTraces.dispose();
		 
		database.dataTraces = stdc;
		
		// ---------------------------------------------------------------------
		// initialize whether using midpoint or not
		// ---------------------------------------------------------------------
		final Command command = Util.getCommand(window, OptionMidpoint.commandId);
		boolean enableMidpoint = Util.isOptionEnabled(command);
		database.dataTraces.setEnableMidpoint(enableMidpoint);
		
		statusMgr.setMessage("Rendering trace data ...");
		
		final Shell shell = window.getShell();
		shell.update();
		
		// get a window service to store the new database
		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);

		// keep the current data in "shared" variable
		DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);
		dataService.setData(database.dataTraces);

		// reset the operation history
		TraceOperation.clear();

		boolean result = false;
		
		try {
			// ---------------------------------------------------------------------
			// Update the title of the application
			// ---------------------------------------------------------------------
			shell.setText("hpctraceviewer: " + database.dataTraces.getName());

			// ---------------------------------------------------------------------
			// Tell all views that we have the data, and they need to refresh
			// their content
			// Due to tightly coupled relationship between views,
			// we need to be extremely careful of the order of view activation
			// if the order is "incorrect", it can crash the program
			//
			// TODO: we need to use Eclipse's ISourceProvider to handle the
			// existence of data
			// this should avoid a tightly-coupled views
			// ---------------------------------------------------------------------

			IWorkbenchPage page = window.getActivePage();

			HPCSummaryView sview = (HPCSummaryView) page.showView(HPCSummaryView.ID);
			sview.updateView(database.dataTraces);

			HPCDepthView dview = (HPCDepthView) page.showView(HPCDepthView.ID);
			dview.updateView(database.dataTraces);

			HPCTraceView tview = (HPCTraceView) page.showView(HPCTraceView.ID);
			tview.updateView(database.dataTraces);

			HPCCallStackView cview = (HPCCallStackView) page.showView(HPCCallStackView.ID);
			cview.updateView(database.dataTraces);

			result = true;

		} catch (PartInitException e) {
			e.printStackTrace();
		}

		statusMgr.setMessage("");
		return result;
	}
}
