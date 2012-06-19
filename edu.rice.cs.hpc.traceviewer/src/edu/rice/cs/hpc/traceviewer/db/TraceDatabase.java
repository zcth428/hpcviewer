package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpc.traceviewer.ui.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.ui.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.ui.HPCSummaryView;
import edu.rice.cs.hpc.traceviewer.ui.HPCTraceView;
import edu.rice.cs.hpc.traceviewer.util.TraceProgressReport;

/*************
 * 
 * Class to manage trace database: opening and detecting the *.hpctrace files
 * 
 */
public class TraceDatabase {

	/**
	 * heuristically, the minimum size a trace file must be in order to be
	 * correctly formatted
	 */
	

	static private HashMap<IWorkbenchWindow, TraceDatabase> listOfDatabases = null;
	private SpaceTimeDataController dataTraces = null;

	/***
	 * get the instance of this class
	 * If an instance does not exist, this creates an instance and a from the window to that instance and returns the instance
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
			data.dataTraces.getPainter().dispose();
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
			final String[] args, final IStatusLineManager statusMgr, AbstractDBOpener opener) {
		

		final Shell shell = window.getShell();

		TraceDatabase database = TraceDatabase.getInstance(window);
		
		SpaceTimeDataController STDC = opener.openDBAndCreateSTDC(window, args,
				statusMgr);
		
		if (STDC==null)
			return false;
		
		database.dataTraces = STDC;
		statusMgr.setMessage("Rendering trace data ...");
		shell.update();

		try {
			// ---------------------------------------------------------------------
			// Update the title of the application
			// ---------------------------------------------------------------------
			shell.setText("hpctraceviewer: " + database.dataTraces.getName());

			// ---------------------------------------------------------------------
			// Tell all views that we have the data, and they need to refresh
			// their content
			// ---------------------------------------------------------------------

			HPCSummaryView sview = (HPCSummaryView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(HPCSummaryView.ID);
			sview.updateData(database.dataTraces);

			HPCTraceView tview = (HPCTraceView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(HPCTraceView.ID);
			tview.updateData(database.dataTraces);

			HPCDepthView dview = (HPCDepthView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(HPCDepthView.ID);
			dview.updateData(database.dataTraces);

			HPCCallStackView cview = (HPCCallStackView) PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(HPCCallStackView.ID);
			cview.updateData(database.dataTraces);

			ISourceProviderService sourceProviderService = (ISourceProviderService) window
					.getService(ISourceProviderService.class);

			// keep the current data in "shared" variable
			DataService dataService = (DataService) sourceProviderService
					.getSourceProvider(DataService.DATA_PROVIDER);
			dataService.setData(database.dataTraces);

			return true;

		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return false;// This will only execute if an Exception is thrown and
						// caught

	}

	

}
