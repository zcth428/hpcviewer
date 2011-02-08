package edu.rice.cs.hpc.viewer.framework;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchListener;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.util.Utilities;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private ExperimentData dataEx ;
	private IWorkbench workbench;
	/**
	 * Creates a new workbench window advisor for configuring a workbench window via the given workbench window configurer
	 * Retrieve the RCP's arguments and verify if it contains database to open
	 * 
	 * @param configurer
	 * @param args
	 */
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, String []args) {
		super(configurer);
		this.workbench = configurer.getWindow().getWorkbench();
		if(args != null && args.length > 0) {
			dataEx = ExperimentData.getInstance(this.workbench.getActiveWorkbenchWindow());
			dataEx.setArguments(args);
		}
	}

	/**
	 * Creates a new action bar advisor to configure the action bars of the window via 
	 * the given action bar configurer. The default implementation returns a new instance of ActionBarAdvisor
	 */
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/**
	 * Performs arbitrary actions before the window is opened.
	 */
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(false);	// remove toolbar/coolbar
		configurer.setShowStatusLine(true);	// show status bar
        configurer.setShowProgressIndicator(true);
		configurer.setTitle("hpcviewer");	// default title (to be updated)
	}

	/**
	 * Action when the window is already opened
	 */
	public void postWindowOpen() {
    	boolean withCallerView = true;

		// -------------------
    	// set the status bar
		// -------------------
		IWorkbenchWindow windowCurrent = workbench.getActiveWorkbenchWindow(); 
		org.eclipse.jface.action.IStatusLineManager statusline = getWindowConfigurer()
		.getActionBarConfigurer().getStatusLineManager();

		assert (windowCurrent != null);

		// -------------------
		// set the default metric
		// -------------------
		Utilities.setFontMetric(windowCurrent.getShell().getDisplay());

		// -------------------
		// see if the argument provides the database to load
		// -------------------
		if(this.dataEx != null) {
			// possibly we have express the experiment file in the command line
			IWorkbenchPage pageCurrent = windowCurrent.getActivePage();
			assert (pageCurrent != null);
			
			ExperimentView expViewer = new ExperimentView(pageCurrent);
		    assert (expViewer != null); 

		    String []sArgs = this.dataEx.getArguments();
		    String sPath = null;
		    // find the file in the list of arguments
		    for(int i=0;i<sArgs.length;i++) {

		    	if(sArgs[i].charAt(0) == '-') {
		    		String sOption = sArgs[i].substring(1);
		    		if (sArgs[i].charAt(1) == 'n' || (sArgs[i].charAt(2)=='-' && sOption.equalsIgnoreCase("nocallerview")) ){
		    			// user has set the option not to display caller view
		    			withCallerView = false;
		    		}
		    	} else {
		    		sPath = sArgs[i];
		    	}
		    }
		    
			// -------------------
		    // if a filename exist, try to open it
			// -------------------
		    if(sPath != null) {
		    	IFileStore fileStore;
				try {
					fileStore = EFS.getLocalFileSystem().getStore(new URI(sPath));
				} catch (URISyntaxException e) {
					// somehow, URI may throw an exception for certain schemes. 
					// in this case, let's do it traditional way
					fileStore = EFS.getLocalFileSystem().getStore(new Path(sPath));
					e.printStackTrace();
				}
		    	IFileInfo objFileInfo = fileStore.fetchInfo();
		    	
		    	if( objFileInfo.exists() ) {
		    		if ( objFileInfo.isDirectory() ) {
		    			System.out.println("Opening database: " + fileStore.fetchInfo().getName() + " " + withCallerView);
		    			// bug fix: needs to treat a folder/directory
		    			// it is a directory
		    			this.dataEx = //new ExperimentData(this.workbench.getActiveWorkbenchWindow());
		    				ExperimentData.getInstance(this.workbench.getActiveWorkbenchWindow());
		    			ExperimentManager objManager = this.dataEx.getExperimentManager();
		    			objManager.openDatabaseFromDirectory(sPath, this.getFlag(withCallerView));
		    		} else {
		    			File objFile = new File(sPath);
		    			EFS.getLocalFileSystem().fromLocalFile(new File(sPath));
		    			System.out.println("Opening file: " + objFile.getAbsolutePath() + " " +  fileStore.fetchInfo().getName());
		    			expViewer.loadExperimentAndProcess( objFile.getAbsolutePath(), withCallerView);
		    		}
		    	} else {
		    		System.err.println("File doesn't exist: " + fileStore.getName() );
					this.hideViews();

		    	}

				this.shutdownEvent(this.workbench, windowCurrent.getActivePage());
				return;
		    } 
		}
		
		// there is no information about the database
		statusline.setMessage(null, "Load a database to start.");
		// we need load the file ASAP
		this.openDatabase(withCallerView);

		this.shutdownEvent(this.workbench, windowCurrent.getActivePage());
	}
	
	/**
	 * Open an experiment database. A database is a folder that contains XML experiment files 
	 * (only the first one will be taken into account)
	 */
	private void openDatabase( boolean withCallerView ) {
		this.dataEx = 
			ExperimentData.getInstance(this.workbench.getActiveWorkbenchWindow());
		ExperimentManager expFile = this.dataEx.getExperimentManager();
		boolean has_database = expFile.openFileExperiment( this.getFlag(withCallerView));

		if (!has_database) {
			this.hideViews();
		}
	}


	/****
	 * when there is no database opened, we have to hide views to avoid users to click buttons
	 * since most action buttons assume the database is already opened, then an action of this
	 * button while there is no database will cause chaos
	 */
	private void hideViews() {
		// close views
		IWorkbenchPage page = this.workbench.getActiveWorkbenchWindow().getActivePage();
		IViewReference refs[] = page.getViewReferences();
		for (IViewReference ref_view: refs) {
			IViewPart view = ref_view.getView(false);
			if (view != null)
				page.hideView(view);
		}
	}
	
	
	/**
	 * return the flag to indicate if a caller view needs to be displayed or not
	 * @param withCallerView
	 * @return
	 */
	private int getFlag (boolean withCallerView) {
		if (withCallerView) return ExperimentManager.FLAG_DEFAULT;
		else return ExperimentManager.FLAG_WITHOUT_CALLER_VIEW;
	}
	/**
	 * Performs arbitrary actions as the window's shell is being closed directly, and possibly veto the close.
	 */
	public boolean preWindowShellClose() {
		this.workbench.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		return true; // we allow app to shut down
	}
	
	/**
	 * add a shutdown event to the workbench
	 * @param IWorkbench
	 * @param IWorkbenchPage
	 */
	private void shutdownEvent(IWorkbench workbench, final IWorkbenchPage pageCurrent) {
		// attach a new listener to the workbench
		workbench.addWorkbenchListener(new IWorkbenchListener(){
			public boolean preShutdown(IWorkbench workbench,
                    boolean forced) {
				// bug on Mac OS: Mac will allow user to close via menu system while hpcviewer is still displaying 
				// 	a modal dialog box (such as  open file dialog). This will create infinite loop in the SWT events
				//  and has to be killed 
				if (pageCurrent != null)
					// somehow, closeEditors method works better than closeAllEditors.
					pageCurrent.closeEditors(pageCurrent.getEditorReferences(), false);
				return true;
			}
			public void postShutdown(IWorkbench workbench) {
				// do nothing
			}
		});
	}
	
	/**
	 * Laksono 2009.02.11: removing unwanted menus
	 */
	public void postWindowCreate()
	{
	  IContributionItem[] mItems, mSubItems;
	  IMenuManager mm = getWindowConfigurer ().getActionBarConfigurer ().getMenuManager ();
	  mItems = mm.getItems ();
	  for (int i = 0; i < mItems.length; i++)
	  {
	    if (mItems[i] instanceof MenuManager)
	    {
	      mSubItems = ((MenuManager) mItems[i]).getItems ();
	      for (int j = 0; j < mSubItems.length; j++)
	      {
	    	  if (mItems[i].getId ().equals ("help"))
	        {
	    		  // John doesn't like some of the key assist tooltips. Let's remove all for once
	          ((MenuManager) mItems[i]).remove ("org.eclipse.ui.actions.showKeyAssistHandler");
	        }
	      }
	    }
	  }
	}
}
