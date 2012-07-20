package edu.rice.cs.hpc.viewer.window;

import java.io.File;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.actions.DebugShowCCT;
import edu.rice.cs.hpc.viewer.actions.DebugShowFlatID;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.provider.DatabaseState;

/**
 * This class is used to record information about one hpcviewer window.  It contains information 
 * about each of the databases that have been opened in this window and an instance of the workbench 
 * window with which it is associated.
 * 
 * Each database opened within a window creates a Database object which is kept in dbObj array.  The index 
 * into this array will be used as a database number.  This number plus 1 (to avoid showing a 0) will be 
 * displayed in the view titles for all metrics views created when the database is opened.  The view titles 
 * will also contain the experiment name (which was moved from the window title to the view titles).  This 
 * allows the user to see which database was the source for the data in each metric view.
 * 
 * @author mohrg
 *
 */
public class ViewerWindow {
	/**
	 * The maximum number of performance databases that can be 
	 * opened in one window.
	 */
	public static int maxDbNum = 5;
	/**
	 *  The workbench window with which this class is associated. 
	 */
	private IWorkbenchWindow winObj;
	/**
	 * An array of performance databases (indexed by database number) that have 
	 * been opened in this window.
	 */
	Database[] dbObj = new Database[maxDbNum];

	private Command cmdDebugCCT;
	private Command cmdDebugFlat;
	

	public IWorkbenchWindow getWinObj() {
		return winObj;
	}
	public void setWinObj(IWorkbenchWindow window) {
		winObj = window;
		ICommandService commandService = (ICommandService) winObj.getService(ICommandService.class);
		cmdDebugCCT = commandService.getCommand( DebugShowCCT.commandId );
		cmdDebugFlat = commandService.getCommand( DebugShowFlatID.commandId );
	}

	/**
	 * Get the next database number to be used in this window.
	 * @return
	 */
	public int getNextDbNum() {
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				return i;
			}
		}
		return -1;
	}

	public int getDbNum(Experiment experiment) {
		return this.getDbNum(experiment.getXMLExperimentFile().getPath());
	}
	
	public int getDbNum(String dbPath) {
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			String path = dbObj[i].getExperiment().getXMLExperimentFile().getPath();
			if (dbPath.equals(path)) {
				return i;
			}
		}
		return -1;
	}

	public Database getDb(int index) {
		return dbObj[index];
	}

	public Database getDb(String dbPath) {
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			String path = dbObj[i].getExperiment().getXMLExperimentFile().getPath();
			if (dbPath.equals(path)) {
				return dbObj[i];
			}
		}
		return null;
	}

	public Database getDb(Experiment experiment) {
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			if (dbObj[i].getExperiment().equals(experiment)) {
				return dbObj[i];
			}
		}
		return null;
	}

	public Database getDb(ExperimentView view) {
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			if (dbObj[i].getExperimentView().equals(view)) {
				return dbObj[i];
			}
		}
		return null;
	}

	/**
	 * Returns the number of open databases in this window.
	 */
	public int getOpenDatabases () {
		int opened=0;
		for (int i=0 ; i<maxDbNum ; i++) {
			if (dbObj[i] != null) {
				opened++;
			}
		}
		return opened;
	}

	/**
	 * Adds a new database to to the array of open databases in this window.
	 * 
	 * @param database
	 * @return
	 */
	public int addDatabase(Database database) {
		// find an available spot in the array
		for (int i=0 ; i<dbObj.length ; i++) {
			if (dbObj[i] == null) {
				dbObj[i] = database;

				// refresh the menu state
				checkService();
				
				return i;
			}
		}
		// no empty slots, put out a dialog box to report we are unable to open another database
		MessageDialog.openError(winObj.getShell(), 
				"Error: Maximum Performance Database's already open.", 
				"There are already " + maxDbNum + " performance databases opened in this window, can not open any more.");
		return -1;
	}

	/**
	 * Removes a database from the list of open databases in this window.
	 * 
	 * @param databasePath
	 * @return
	 */
	public int removeDatabase (String databasePath) {
		// look for the pathname of the database we want to remove
		for (int i=0 ; i<maxDbNum ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			Experiment experiment = dbObj[i].getExperiment();
			String xmlFileName = experiment.getXMLExperimentFile().getPath();
			int dbDir = xmlFileName.lastIndexOf(File.separator);
			// if this is the guy we want to go away, take him out now
			if (xmlFileName.substring(0,dbDir).equals(databasePath)) {
				dbObj[i].dispose();
				// Database numbers are visible to the user (in titles).  So we do not
				// compact the list, we only make this entry empty (set pointer to its 
				// database class to null) so it can be reused if another open is done.
				dbObj[i] = null;
				
				// refresh the menu state
				checkService();
				
				experiment.dispose();
				
				return i;
			}
		}
		return -1;
	}

	/*****
	 * get the opened experiment databases
	 * @return null if there is no opened database
	 */
	public Experiment[] getExperiments() {
		int numDb = getOpenDatabases();
		if (numDb == 0)
			return null;
		Experiment []experiments = new Experiment[numDb];
		
		for (int i=0, j=0; i<maxDbNum; i++) {
			if (dbObj[i] == null)
				continue;
			experiments[j] = dbObj[i].getExperiment();
			j++;
		}
		return experiments;
	}
	
	/**
	 *  get the path names for each database currently opened in this window.
	 * @param window
	 * @return
	 */
	public String[] getDatabasePaths () {
		String[] dbArray = new String[getOpenDatabases()];
		
		for (int i=0, j=0 ; i<maxDbNum ; i++) {
			if (dbObj[i] == null) {
				continue;
			}
			Experiment experiment = dbObj[i].getExperiment();
			String xmlFileName = experiment.getXMLExperimentFile().getPath();
			int dbDir = xmlFileName.lastIndexOf(File.separator);
			dbArray[j++] = xmlFileName.substring(0,dbDir);
		}

		return dbArray;
	}
	
	
	/***
	 * update the service provided by DatabaseState to ensure that the menus's state are refreshed
	 * 
	 */
	public void checkService()
	{
		ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
						ISourceProviderService.class);
		// Now get my service
		DatabaseState commandStateService = (DatabaseState) sourceProviderService
				.getSourceProvider(DatabaseState.DATABASE_ACTIVE_STATE);
		commandStateService.toogleEnabled(winObj);
		
		commandStateService = (DatabaseState) sourceProviderService
				.getSourceProvider(DatabaseState.DATABASE_MERGE_STATE);
		commandStateService.toogleEnabled(winObj);
	}
	
	// --------------------------------------------------------------
	// Debug mode for this window
	// --------------------------------------------------------------
	
	/**
	 * check if we are in debug mode (showing CCT label) or not 
	 * @return true if we need to show CCT label
	 */
	public boolean showCCTLabel() 
	{
		return isDebugMode(cmdDebugCCT);
	}
	
	/**
	 * check if we are in debug mode (showing Flat label) or not 
	 * @return true if we need to show Flat label
	 */
	public boolean showFlatLabel() 
	{
		return isDebugMode(cmdDebugFlat);
	}
	
	/**
	 * check if we are in debug mode for a given command
	 * 
	 * @param command
	 * @return
	 */
	private boolean isDebugMode(Command command) 
	{
		boolean isDebug = false;
		final State state = command.getState(RegistryToggleState.STATE_ID);
		if (state != null)
		{
			final Boolean b = (Boolean) state.getValue();
			isDebug = b.booleanValue();
		}
		return isDebug;
	}
}
