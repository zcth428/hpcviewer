package edu.rice.cs.hpc.traceviewer.db.local;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
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

	final private String directory;
	
	/*******
	 * prepare opening a database 
	 * 
	 * @param directory : the directory of the database
	 */
	public LocalDBOpener(String directory)
	{
		this.directory = directory;
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


	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}	
}


