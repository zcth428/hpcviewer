package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpc.traceviewer.util.TraceProgressReport;

public class LocalDBOpener extends AbstractDBOpener {
	
	private String directory;
	
	
	public LocalDBOpener(String inDirectory){
		directory=inDirectory;
	}
	
	
	


	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, final IStatusLineManager statusMgr) {
		
		final Shell shell = window.getShell();
		FileData location = new FileData();
		
		//if database is incorrect, return null so openDatabase prompts user for a new directory
		if (!isCorrectDatabase(directory, statusMgr, location)) {
			errorMessage= "The directory selected contains no traces:\n"
                    + directory + "\nPlease select a directory that contains traces.";
			return null;
		}
		

		
		TraceDatabase.removeInstance(window);

		// ---------------------------------------------------------------------
		// Try to open the database and refresh the data
		// ---------------------------------------------------------------------
		
		
		statusMgr.setMessage("Opening trace data...");
		shell.update();
		
		
		//

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
		
		return new SpaceTimeDataControllerLocal(
				window, statusMgr, location.fileXML, location.fileTrace);
		
		
	}
	
	/****
	 * Check if the directory is correct or not. If it is correct, it returns
	 * the XML file and the trace file
	 * 
	 * @param directory
	 *            (in): the input directory
	 * @param statusMgr
	 *            (in): status bar
	 * @param experimentFile
	 *            (out): XML file
	 * @param traceFile
	 *            (out): trace file
	 * @return true if the directory is valid, false otherwise
	 * 
	 */
	static private boolean isCorrectDatabase(String directory,
			final IStatusLineManager statusMgr, FileData location) {
		File dirFile = new File(directory);

		if (dirFile.exists() && dirFile.isDirectory()) {
			location.fileXML = new File(directory + File.separatorChar
					+ Constants.DATABASE_FILENAME);

			if (location.fileXML.canRead()) {
				try {
					statusMgr.setMessage("Merging traces ...");

					final TraceProgressReport traceReport = new TraceProgressReport(
							statusMgr);
					final String outputFile = dirFile.getAbsolutePath()
							+ File.separatorChar + "experiment.mt";
					final MergeDataFiles.MergeDataAttribute att = MergeDataFiles
							.merge(dirFile, "*.hpctrace", outputFile,
									traceReport);
					if (att != MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
						location.fileTrace = new File(outputFile);

						if (location.fileTrace.length() > MIN_TRACE_SIZE) {
							return true;
						}
						System.err.println("Warning! Trace file "
								+ location.fileTrace.getName()
								+ " is too small: "
								+ location.fileTrace.length() + "bytes .");
						return false;
					}
					System.err
							.println("Error: trace file(s) does not exist or fail to open "
									+ outputFile);

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return false;
	}
	         
	
}


