package edu.rice.cs.hpc.traceviewer.db.local;

import java.io.File;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.util.Util;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*******************************************************************
 * 
 * Class to open a local database
 * 
 * @author Philip Taffet
 * 
 *******************************************************************/
public class LocalDBOpener extends AbstractDBOpener {

	private String directory;
	
	/*******
	 * prepare opening a database 
	 * 
	 * @param directory : the directory of the database
	 * @throws Exception 
	 */
	public LocalDBOpener(String directory) throws Exception
	{
		if (LocalDBOpener.directoryHasTraceData(directory)>0) {
			this.directory = directory;
		} else {
			throw new Exception("The direcoty does not contain hpctoolkit database with trace data:"
					+ directory);
		}
	}
	
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener#openDBAndCreateSTDC
	 * (org.eclipse.ui.IWorkbenchWindow, org.eclipse.jface.action.IStatusLineManager)
	 */
	public SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			final IStatusLineManager statusMgr) throws InvalExperimentException, Exception {
		
		final Shell shell = window.getShell();
		
		// Laks 2014.03.10: needs to comment the call to removeInstance
		// this call causes the data to be deleted but the GC+Color instances still exist
		// the allocated GC+Color can be disposed later in SpaceTimeDataController class
		
		//	TraceDatabase.removeInstance(window);

		// ---------------------------------------------------------------------
		// Try to open the database and refresh the data
		// ---------------------------------------------------------------------
		
		
		statusMgr.setMessage("Opening trace data...");
		shell.update();

		// ---------------------------------------------------------------------
		// dispose resources if the data has been allocated
		// unfortunately, some colors are allocated from window handle,
		// some are allocated dynamically. At the moment we can't dispose
		// all colors
		// ---------------------------------------------------------------------
		// if (database.dataTraces != null)
		// database.dataTraces.dispose();

		// database.dataTraces = new SpaceTimeData(window, location.fileXML,
		// location.fileTrace, statusMgr);
		
		SpaceTimeDataControllerLocal stdc = new SpaceTimeDataControllerLocal(
				window, directory);
		
		if (stdc.setupTrace(window, statusMgr)) {
			return stdc;
		}
		return null;
	}

	/**********************
	 * static method to check if a directory contains hpctoolkit's trace data
	 * 
	 * @param directory : a database directory
	 * @return int version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 */
	static public int directoryHasTraceData(String directory)
	{
		File file = new File(directory);
		String database_directory;
		if (file.isFile()) {
			// if the argument is a file, then we'll look for its parent directory
			file = file.getParentFile();
			database_directory = file.getAbsolutePath();
		} else {
			database_directory = directory;
		}
		// checking for version 3.0
		String file_path = database_directory + File.separatorChar + "trace.db";
		File tmp_file 	 = new File(file_path);
		if (tmp_file.canRead()) {
			return 3;
		}
		
		// checking for version 2.0
		file_path = database_directory + File.separatorChar + "experiment.mt";
		tmp_file  = new File(file_path);
		if (tmp_file.canRead()) {
			return 2;
		}
		
		// checking for version 2.0 with old format files
		tmp_file  = new File(database_directory);
		File[] file_hpctraces = tmp_file.listFiles( new Util.FileThreadsMetricFilter("*.hpctrace") );
		if (file_hpctraces != null && file_hpctraces.length>0) {
			return 2;
		}
		return -1;
	}



	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}	
}


