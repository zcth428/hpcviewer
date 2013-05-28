package edu.rice.cs.hpc.traceviewer.db;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.MergeDataFiles;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerLocal;
import edu.rice.cs.hpc.traceviewer.util.TraceProgressReport;

public class LocalDBOpener extends AbstractDBOpener {

	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		statusMgr.setMessage("Select a directory containing traces");
		FileData location = new FileData();
		
		Shell shell = window.getShell();
		boolean hasDatabase = false;
		// ---------------------------------------------------------------
		// processing the command line argument
		// ---------------------------------------------------------------
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg != null && arg.charAt(0) != '-') {
					// this must be the name of the database to open
					hasDatabase = isCorrectDatabase(arg,
							statusMgr, location);
				}
			}
		}

		if (!hasDatabase) {
			// use dialog box to find the database
			hasDatabase = open(shell, statusMgr, location);
		}
		// If it still doesn't have a database, we assume that the user doesn't
		// want to open a database, so we return null, which makes the calling method return false.
		if (!hasDatabase)
			return null;

		// ---------------------------------------------------------------------
		// Try to open the database and refresh the data
		// ---------------------------------------------------------------------

		statusMgr.setMessage("Opening trace data ...");
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
	/***
	 * Open a database by displaying a directory dialog box return true if the
	 * database is correct, false otherwise
	 * 
	 * @param shell
	 * @return
	 */
	static private boolean open(Shell shell,
			final IStatusLineManager statusMgr, FileData location) {
		DirectoryDialog dialog;

		boolean validDatabaseFound = false;
		dialog = new DirectoryDialog(shell);
		dialog.setMessage("Please select a directory containing execution traces.");
		dialog.setText("Select Data Directory");
		String directory;
		while (!validDatabaseFound) {

			directory = dialog.open();

			if (directory == null)
				// user click cancel
				return false;

			validDatabaseFound = isCorrectDatabase(directory, statusMgr,
					location);

			if (!validDatabaseFound)
				msgNoDatabase(dialog, directory);
		}

		return validDatabaseFound;
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
						} else {
							System.err.println("Warning! Trace file "
									+ location.fileTrace.getName()
									+ " is too small: "
									+ location.fileTrace.length() + "bytes .");
							return false;
						}
					} else {
						System.err
								.println("Error: trace file(s) does not exist or fail to open "
										+ outputFile);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return false;
	}

	/***
	 * show message in directory dialog box
	 * 
	 * @param dialog
	 * @param str
	 */
	private static void msgNoDatabase(DirectoryDialog dialog, String str) {

		dialog.setMessage("The directory selected contains no traces:\n\t"
				+ str + "\nPlease select a directory that contains traces.");
	}
	@Override
	void closeDB() {
		//It doesn't seem like the file handles are ever released. I guess this isn't a problem. 
		
	}

}
