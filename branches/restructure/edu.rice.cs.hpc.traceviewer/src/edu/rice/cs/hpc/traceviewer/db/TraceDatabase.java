package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
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
public class TraceDatabase
{
	
	/** heuristically, the minimum size a trace file must be in order to be correctly formatted*/
	final private static int MIN_TRACE_SIZE = TraceDataByRank.HeaderSzMin + TraceDataByRank.RecordSzMin * 2;
				
	static private HashMap<IWorkbenchWindow, TraceDatabase> listOfDatabases = null;
	private SpaceTimeData dataTraces = null;
	
	/***
	 * get the instance of this class
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
			data.dataTraces.dispose();
			listOfDatabases.remove(_window);
		}
	}
		
	/***
	 * static function to load a database and display the views
	 * @param window
	 * @param args
	 * @param statusMgr
	 * @return
	 */
	static public boolean openDatabase(IWorkbenchWindow window, final String []args, final IStatusLineManager statusMgr)
	{
		boolean hasDatabase = false;

		final Shell shell = window.getShell();

		statusMgr.setMessage("Select a directory containing traces");
		FileData location = new FileData();
		//---------------------------------------------------------------
		// processing the command line argument
		//---------------------------------------------------------------
		if (args != null && args.length>0) {
			for(String arg: args) {
				if (arg != null && arg.charAt(0)!='-') {
					// this must be the name of the database to open
					hasDatabase = TraceDatabase.isCorrectDatabase(arg, statusMgr, location);
				}
			}
		}
		
		if (!hasDatabase) {
			// use dialog box to find the database
			hasDatabase = open(shell, statusMgr, location);
		}
		
		if (hasDatabase) {
			//---------------------------------------------------------------------
			// Try to open the database and refresh the data
			// ---------------------------------------------------------------------
			
			statusMgr.setMessage("Opening trace data ...");
			shell.update();
			
			TraceDatabase database = TraceDatabase.getInstance(window);
			
			// ---------------------------------------------------------------------
			// dispose resources if the data has been allocated
			// 	unfortunately, some colors are allocated from window handle,
			//	some are allocated dynamically. At the moment we can't dispose
			//	all colors
			// ---------------------------------------------------------------------
			if (database.dataTraces != null)
				database.dataTraces.dispose();
			
			database.dataTraces = new SpaceTimeData(window, location.fileXML, location.fileTrace, statusMgr);
			
			statusMgr.setMessage("Rendering trace data ...");
			shell.update();

			try {
				//---------------------------------------------------------------------
				// Update the title of the application
				//---------------------------------------------------------------------
				shell.setText("hpctraceviewer: " + database.dataTraces.getName());
				
				//---------------------------------------------------------------------
				// Tell all views that we have the data, and they need to refresh their content
				//	Due to tightly coupled relationship between views, 
				//	we need to be extremely careful of the order of view activation
				//	if the order is "incorrect", it can crash the program
				//
				// TODO: we need to use Eclipse's ISourceProvider to handle the existence of data
				//		 this should avoid a tightly-coupled views
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

				ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
						ISourceProviderService.class);
				
				// keep the current data in "shared" variable
				DataService dataService = (DataService) sourceProviderService.getSourceProvider(DataService.DATA_PROVIDER);
				dataService.setData(database.dataTraces);
				
				return true;
				
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		return false;

	}
	

	/***
	 * Open a database by displaying a directory dialog box
	 * return true if the database is correct, false otherwise
	 * 
	 * @param shell
	 * @return
	 */
	static private boolean open(Shell shell, final IStatusLineManager statusMgr,
			FileData location)
	{
		DirectoryDialog dialog;

		boolean validDatabaseFound = false;
		dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select a directory containing execution traces.");
		dialog.setText("Select Data Directory");
		String directory;
		/*while(!validDatabaseFound)*/ 
		{
			directory = dialog.open();
			
			if (directory == null) 
				// user click cancel
				return false;
			
			validDatabaseFound = isCorrectDatabase(directory, statusMgr, location);
						
			if (!validDatabaseFound) {
				String sMsg = "The directory selected contains no traces:\n\t" + directory + 
						"\nPlease select a directory that contains traces.";
				MessageDialog.openError(shell, "Error opening the database", sMsg);
			}
		}
		
		return validDatabaseFound;
	}
	
	
	
	/****
	 * Check if the directory is correct or not. If it is correct, it returns the XML file and the trace file
	 * 
	 * @param directory (in): the input directory
	 * @param statusMgr (in): status bar
	 * @param experimentFile (out): XML file
	 * @param traceFile (out): trace file
	 * @return true if the directory is valid, false otherwise
	 * 
	 */
	static private boolean isCorrectDatabase(String directory, final IStatusLineManager statusMgr,
			FileData location)
	{
		File dirFile = new File(directory);
		
		if (dirFile.exists() && dirFile.isDirectory()) {
			location.fileXML = new File(directory + File.separatorChar + Constants.DATABASE_FILENAME);
			
			if (location.fileXML.canRead()) {
				try {
					statusMgr.setMessage("Merging traces ...");
					
					final TraceProgressReport traceReport = new TraceProgressReport(statusMgr);
					final String outputFile = dirFile.getAbsolutePath() + File.separatorChar + "experiment.mt";
					final MergeDataFiles.MergeDataAttribute att = MergeDataFiles.merge(dirFile, "*.hpctrace", outputFile, traceReport);
					
					if (att != MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
						location.fileTrace = new File(outputFile);

						if (location.fileTrace.length() > MIN_TRACE_SIZE) {
							return true;
						} else {
							System.err.println("Warning! Trace file " + location.fileTrace.getName() + " is too small: " 
									+ location.fileTrace.length() + "bytes .");
							return false;
						}
					} else {
						System.err.println("Error: trace file(s) does not exist or fail to open " + outputFile);
					}

				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		return false;
	}	
}
