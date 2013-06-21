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
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.ui.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.ui.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.ui.HPCSummaryView;
import edu.rice.cs.hpc.traceviewer.ui.HPCTraceView;
import edu.rice.cs.hpc.traceviewer.ui.OpenDatabaseDialog;


/*************
 * 
 * Class to manage trace database: opening and detecting the *.hpctrace files
 * 
 */
public class TraceDatabase {

	static private HashMap<IWorkbenchWindow, TraceDatabase> listOfDatabases = null;
	private SpaceTimeDataController dataTraces = null;

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
			if (data.dataTraces != null)
				data.dataTraces.dispose();
			listOfDatabases.remove(_window);
		}
	}

	/***
	 * static function to load a database and display the views
	 * 
	 * @param window
	 * @param args
	 * @param statusMgr
	 * @return
	 */
	static public boolean openDatabase(IWorkbenchWindow window,
			final String[] args, IStatusLineManager statusMgr,
			AbstractDBOpener opener) {
		
		
		AbstractDBOpener openThis = opener;
		final Shell shell = window.getShell(); 
		TraceDatabase database = TraceDatabase.getInstance(window);
		boolean validDatabaseFound = false;
		SpaceTimeDataController stdc;
		
		 do {
			
			stdc = openThis.openDBAndCreateSTDC(window, args,
					statusMgr);
			
			if (stdc == null) { //if STDC is null, directory, port, or server was incorrect
				//open new dialog for user to choose new directory, port, or server
				OpenDatabaseDialog dlg = new OpenDatabaseDialog(new Shell(), statusMgr, openThis.getErrorMessage());
				if (dlg.open() == Window.CANCEL)
					return false;
				openThis = dlg.getDBOpener();
			} else {
				validDatabaseFound=true;
			}
		} while (!validDatabaseFound); //until user enters a valid database or cancels keep popping up dialogs

		database.dataTraces = stdc;
		
		// ---------------------------------------------------------------------
		// initialize whether using midpoint or not
		// ---------------------------------------------------------------------
		final Command command = Util.getCommand(window, OptionMidpoint.commandId);
		boolean enableMidpoint = Util.isOptionEnabled(command);
		database.dataTraces.setEnableMidpoint(enableMidpoint);
		
		statusMgr.setMessage("Rendering trace data ...");
		shell.update();
		
		// get a window service to store the new database
		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);

		// keep the current data in "shared" variable
		DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);
		dataService.setData(database.dataTraces);

		// reset the operation history
		TraceOperation.clear();

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

			return true;

		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return false;

	}
}
