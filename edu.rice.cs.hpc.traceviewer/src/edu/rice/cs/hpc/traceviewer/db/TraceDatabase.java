package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.rice.cs.hpc.data.util.IProgressReport;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.ui.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.ui.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.ui.HPCSummaryView;
import edu.rice.cs.hpc.traceviewer.ui.HPCTraceView;


/*************
 * 
 * Class to manage trace database: opening and detecting the *.hpctrace files
 *
 */
public class TraceDatabase
{
	
	/**the minimum size a trace file must be in order to be correctly formatted*/
	final private static int MIN_TRACE_SIZE = 32+8+TraceDataByRank.SIZE_OF_HEADER+TraceDataByRank.SIZE_OF_TRACE_RECORD*2;
	
	/**a file holding an concatenated collection of all the trace files*/
	private File traceFile = null;
	
	/**a file holding the experiment data - currently .xml format*/
	private File experimentFile = null;
	
	final private String []args;
	
	private IStatusLineManager _statusMgr;
	
	public TraceDatabase(String []_args)
	{
		args = _args;
	}
	
	public boolean openDatabase(Shell shell, final IStatusLineManager statusMgr)
	{
		boolean hasDatabase = false;
		
		_statusMgr = statusMgr;
		statusMgr.setMessage("Select a directory containing traces");
		
		//---------------------------------------------------------------
		// processing the command line argument
		//---------------------------------------------------------------
		if (args != null && args.length>0) {
			for(String arg: args) {
				if (arg != null && arg.charAt(0)!='-') {
					// this must be the name of the database to open
					hasDatabase = this.isCorrectDatabase(arg, statusMgr, shell);
				}
			}
		}
		
		if (!hasDatabase) {
			// use dialog box to find the database
			hasDatabase = this.open(shell, statusMgr);
		}
		
		if (hasDatabase) {
			//---------------------------------------------------------------------
			// Try to open the database and refresh the data
			// ---------------------------------------------------------------------
			File experimentFile = this.getExperimentFile();
			File traceFile = this.getTraceFile();
			
			statusMgr.setMessage("Opening trace data ...");
			shell.update();
			
			SpaceTimeData stData = new SpaceTimeData(shell, experimentFile, traceFile, statusMgr);
			
			statusMgr.setMessage("Rendering trace data ...");
			shell.update();

			try {
				//---------------------------------------------------------------------
				// Update the title of the application
				//---------------------------------------------------------------------
				shell.setText("hpctraceviewer: " + stData.getName());
				
				//---------------------------------------------------------------------
				// Tell all views that we have the data, and they need to refresh their content
				// ---------------------------------------------------------------------				

				HPCSummaryView sview = (HPCSummaryView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCSummaryView.ID);
				sview.updateData(stData);

				HPCTraceView tview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
				tview.updateData(stData);
				
				HPCDepthView dview = (HPCDepthView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCDepthView.ID);
				dview.updateData(stData);
				
				HPCCallStackView cview = (HPCCallStackView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCCallStackView.ID);
				cview.updateData(stData);

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
	private boolean open(Shell shell, final IStatusLineManager statusMgr)
	{
		DirectoryDialog dialog;

		boolean validDatabaseFound = false;
		dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select a directory containing execution traces.");
		dialog.setText("Select Data Directory");
		String directory;
		while(!validDatabaseFound) {
			
			directory = dialog.open();
			
			if (directory == null) 
				// user click cancel
				return false;
			
			validDatabaseFound = this.isCorrectDatabase(directory, statusMgr, shell);
						
			if (!validDatabaseFound)
				this.msgNoDatabase(dialog, directory);
		}
		
		return validDatabaseFound;
	}
	
	
	public File getTraceFile()
	{
		return this.traceFile;
	}
	
	public File getExperimentFile()
	{
		return this.experimentFile;
	}
	
	
	private boolean isCorrectDatabase(String directory, final IStatusLineManager statusMgr, Shell shell)
	{
		File dirFile = new File(directory);
		
		if (dirFile.exists() && dirFile.isDirectory()) {
			experimentFile = new File(directory + File.separatorChar + "experiment.xml");
			
			if (experimentFile.canRead()) {
				try {
					statusMgr.setMessage("Merging traces ...");
					final String traceFilename = MergeDataFiles.merge(dirFile, "*.hpctrace", "mt", 
							new TraceProgressReport(_statusMgr));
					final File traceFile = new File(traceFilename);

					if (traceFile.length() > MIN_TRACE_SIZE) {
						this.traceFile = traceFile;
						return true;
					} else {
						System.err.println("Warning! Trace file " + traceFile.getName() + " is too small: " 
								+ traceFile.length() + "bytes .");
						return false;
					}

				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		return false;
	}
	
	private void msgNoDatabase(DirectoryDialog dialog, String str) {
		
		dialog.setMessage("The directory selected contains no traces:\n\t" + str + 
				"\nPlease select a directory that contains traces.");
	}
	
	private class TraceProgressReport implements IProgressReport 
	{
		final private IStatusLineManager _statusMgr;
		
		public TraceProgressReport(IStatusLineManager statusMgr )
		{
			this._statusMgr = statusMgr;
		}
		
		public void begin(String title, int num_tasks) {
			_statusMgr.setMessage(title);
			_statusMgr.getProgressMonitor().beginTask("Starting: "+title, num_tasks);
		}

		public void advance() {
			_statusMgr.getProgressMonitor().worked(1);
		}

		public void end() {
			_statusMgr.getProgressMonitor().done();
		}
		
	}
}
