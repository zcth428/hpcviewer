package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
import edu.rice.cs.hpc.traceviewer.ui.HPCCallStackView;
import edu.rice.cs.hpc.traceviewer.ui.HPCDepthView;
import edu.rice.cs.hpc.traceviewer.ui.HPCTraceView;

public class TraceDatabase {
	
	private ArrayList<File> traceFiles = null;
	private File experimentFile = null;
	final private String []args;
	
	public TraceDatabase(String []_args) {
		this.args = _args;
	}
	
	public boolean openDatabase(Shell shell) {
		
		boolean hasDatabase = false;
		
		//---------------------------------------------------------------
		// processing the command line argument
		//---------------------------------------------------------------
		if (args != null && args.length>0) {
			for(String arg: args) {
				if (arg != null && arg.charAt(0)!='-') {
					// this must be the name of the database to open
					hasDatabase = this.isCorrectDatabase(arg);
				}
			}
		}
		
		if (!hasDatabase) {
			// use dialog box to find the database
			hasDatabase = this.open(shell);
		}
		
		if (hasDatabase) {
			
			//---------------------------------------------------------------------
			// Try to open the database and refresh the data
			// ---------------------------------------------------------------------
			
			File experimentFile = this.getExperimentFile();
			ArrayList<File> traceFiles = this.getTraceFiles();
			
			SpaceTimeData stData = new SpaceTimeData(shell, experimentFile, traceFiles);
			
			try {
				//---------------------------------------------------------------------
				// Tell all views that we have the data, and they need to refresh their content
				// ---------------------------------------------------------------------				

				HPCTraceView tview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
				tview.updateData(stData);
				
				HPCDepthView dview = (HPCDepthView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCDepthView.ID);
				dview.updateData(stData);
				
				HPCCallStackView cview = (HPCCallStackView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCCallStackView.ID);
				cview.updateData(stData);
				
				//---------------------------------------------------------------------
				// upate the title of the application
				//---------------------------------------------------------------------
				shell.setText("hpctraceviewer: " + stData.getName());
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
	private boolean open( Shell shell) {
		DirectoryDialog dialog;

		boolean validDatabaseFound = false;
		dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select the directory which holds the trace databases.");
		dialog.setText("Select Data Directory");
		String dir;
		while(!validDatabaseFound)
		{
			traceFiles = new ArrayList<File>();
			
			dir = dialog.open();
			
			if (dir == null) 
				// user click cancel
				return false;
			
			validDatabaseFound = this.isCorrectDatabase(dir);
						
			if (!validDatabaseFound)
				this.msgNoDatabase(dialog, dir);
		}
		
		return validDatabaseFound;
	}
	
	
	public ArrayList<File> getTraceFiles() {
		return this.traceFiles;
	}
	
	public File getExperimentFile() {
		return this.experimentFile;
	}
	
	
	private boolean isCorrectDatabase(String directory) {
		File dirFile = new File(directory);
		String[] databases = dirFile.list();
		
		if (databases != null) {
			experimentFile = new File(directory+File.separatorChar+"experiment.xml");
			
			if (experimentFile.canRead()) {
				
				ArrayList<File> listOfFiles = new ArrayList<File>();
				for(String db: databases) {
					String traceFile = directory + File.separatorChar + db;
					if (traceFile.contains(".hpctrace")) {
						listOfFiles.add(new File(traceFile));
					}
				}
				if (listOfFiles.size()>0) {
					this.traceFiles = listOfFiles;
					return true;
				}
			}
		}
		return false;
	}
	
	private void msgNoDatabase(DirectoryDialog dialog, String str) {
		
		dialog.setMessage("The directory selected contains no trace databases:\n\t" + str + 
		"\nPlease select the directory which holds the trace databases.");
		
	}
}
