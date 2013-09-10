package edu.rice.cs.hpc.viewer.window;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.actions.DebugShowCCT;
import edu.rice.cs.hpc.viewer.actions.DebugShowFlatID;
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
	ArrayList<Database> dbObj = new ArrayList<Database>(maxDbNum);

	private Command cmdDebugCCT;
	private Command cmdDebugFlat;
	
	/** number of databases (whether has been closed or not) 
	 *  this number is useful to make sure the view is always unique */
	private AtomicInteger numAggregateDatabase = new AtomicInteger(1);

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
		return dbObj.size();
	}


	public Database getDb(String dbPath) {
		for (Database db: dbObj) {
			if (db == null) {
				continue;
			}
			String path = db.getExperiment().getDefaultDirectory().getAbsolutePath(); //.getXMLExperimentFile().getPath();
			if (dbPath.equals(path)) {
				return db;
			}
		}
		return null;
	}


	/**
	 * Returns the number of open databases in this window.
	 */
	public int getOpenDatabases () {
		return dbObj.size();
	}

	/**
	 * Adds a new database to to the array of open databases in this window.
	 * 
	 * @param database
	 * @return
	 */
	public int addDatabase(Database database) {
		dbObj.add(database);
		checkService();
		return dbObj.size()-1;
	}

	/**
	 * Removes a database from the list of open databases in this window.
	 * 
	 * @param databasePath
	 * @return 	the current number of database (usually db.size - 1 if no error occurs)
	 * 			<0 if there's an error
	 */
	public int removeDatabase (String databasePath) {
		Database db = getDb(databasePath);
		assert(db != null);
		
		if (dbObj.remove(db)) {
			checkService();

			return dbObj.size();
		}
		return -1;
	}

	public int getDbNum(Experiment experiment) {
		int i=0;
		for (Database db: dbObj) {
			if (db == null) {
				continue;
			}
			Experiment current = db.getExperiment();
			if (experiment.equals(current)) {
				break;
			}
			i++;
		}
		return i;
	}

	
	/****
	 * Remove (nullify) all the allocated resources
	 */
	public void dispose() {
		for (Database db : dbObj) {
			if (db != null)
				db.dispose();
		}
		dbObj = null;
	}
	
	public Database[] getDatabases() {
		Database []db = new Database[dbObj.size()];
		dbObj.toArray(db);
		return db;
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
		
		int i=0;
		for (Database db: dbObj) {
			if (db == null)
				continue;
			experiments[i] = db.getExperiment();
			i++;
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
		
		int i=0;
		for (Database db : dbObj) {
			if (dbObj == null) {
				continue;
			}
			Experiment experiment = db.getExperiment();
			String xmlFileName = experiment.getXMLExperimentFile().getPath();
			int dbDir = xmlFileName.lastIndexOf(File.separator);
			dbArray[i++] = xmlFileName.substring(0,dbDir);
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
	
	/****
	 * get the ID of the database. This ID has to be unique for each window, and always incremented
	 * 
	 * Every time we open a database, we only increment by 1
	 * 
	 * @return
	 */
	public int reserveDatabaseNumber() {
		return numAggregateDatabase.incrementAndGet();
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
