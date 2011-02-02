package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class TraceDatabase {
	
	private ArrayList<File> traceFiles = null;
	private File experimentFile = null;
	

	public boolean open( Shell shell) {
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
