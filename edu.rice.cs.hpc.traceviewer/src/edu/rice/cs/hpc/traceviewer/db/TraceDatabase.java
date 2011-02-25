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
	
	
	public boolean openDatabase(Shell shell) {
		
		if (this.open(shell)) {
			
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
				return false;
			
			File dirFile = new File(dir);
			String[] databases = dirFile.list();
			
			if ((databases == null)) 
				this.msgNoDatabase(dialog, dir);
			
			experimentFile = new File(dir+File.separatorChar+"experiment.xml");
			
			for (int databaseId = 0; databaseId < databases.length; databaseId++)
			{
				String cstName = dir+File.separatorChar+databases[databaseId];
				if (cstName.contains(".hpctrace"))
				{
					traceFiles.add(new File(dir+File.separatorChar+databases[databaseId]));
					validDatabaseFound = true;
				}
			}
			if (!experimentFile.exists())
				validDatabaseFound = false;
			
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
	
	
	private void msgNoDatabase(DirectoryDialog dialog, String str) {
		
		dialog.setMessage("The directory selected contains no trace databases:\n\t" + str + 
		"\nPlease select the directory which holds the trace databases.");
		
	}
}
