package edu.rice.cs.hpc.viewer.experiment;

import java.util.HashMap;
import org.eclipse.jface.dialogs.MessageDialog;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.*; 
import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.scope.DynamicViewListener;
import edu.rice.cs.hpc.viewer.scope.bottomup.CallerScopeView;
import edu.rice.cs.hpc.viewer.scope.flat.FlatScopeView;
import edu.rice.cs.hpc.viewer.scope.topdown.ScopeView;
import edu.rice.cs.hpc.viewer.util.PreferenceConstants;
import edu.rice.cs.hpc.viewer.util.WindowTitle;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
/**
 * Class to be used as an interface between the GUI and the data experiment
 * This class should be called from an eclipse view !
 * @author laksono
 *
 */
public class ExperimentView {

	static final private int VIEW_STATE_INIT = -1;
	
	private IWorkbenchPage objPage;		// workbench current page
	/**
	 * List of registered views in the current experiment
	 */
	protected BaseScopeView []arrScopeViews;
	
	/** we have to make sure that the listener is added only once for a given window **/
	static private HashMap<IWorkbenchWindow, DynamicViewListener> hashWindow;
	
	/**
	 * Constructor for Data experiment. Needed to link with the view
	 * @param objTarget: the scope view to link with
	 */
	public ExperimentView(IWorkbenchPage objTarget) {
		if(objTarget != null) {
			this.objPage = objTarget;
			
			if (hashWindow == null) {
				hashWindow = new HashMap<IWorkbenchWindow, DynamicViewListener>();
			}
			IWorkbenchWindow window = objPage.getWorkbenchWindow();
			DynamicViewListener listener = hashWindow.get(window);
			if (listener == null) {
				listener = new DynamicViewListener(window);
				window.getPartService().addPartListener(listener);
				hashWindow.put(window, listener);
			}

		} else {
			System.err.println("EV Error: active page is null !");
		}
	}
	

	
	/**
	 * A wrapper of loadExperiment() by adding some processing and generate the views
	 * @param sFilename
	 * @param bCallerView : flag to indicate if the caller view can be displayed
	 * 
	 * @return true if the experiment is loaded successfully
	 */
	public boolean loadExperimentAndProcess(String sFilename, boolean bCallerView) {
		
		Experiment experiment = this.loadExperiment(sFilename);

		if(experiment != null) {
			try {
				experiment.postprocess(bCallerView);
			} catch (java.lang.OutOfMemoryError e) 
			{
				MessageDialog.openError(this.objPage.getWorkbenchWindow().getShell(), "Out of memory", 
						"hpcviewer requires more heap memory allocation.\nJava heap size can be increased by modifying \"-Xmx\" parameter in hpcivewer.ini file.");
			} catch (java.lang.RuntimeException e) 
			{
				MessageDialog.openError(objPage.getWorkbenchWindow().getShell(), "Critical error", 
						"XML file is not in correct format: \n"+e.getMessage());
			}
	        this.generateView(experiment);
	        return true;
		}
		return false;
	}
	
	/**
	 * A wrapper of loadExperiment() by adding some processing and generate the views
	 * The routine will first look at the user preference for displaying caller view 
	 * Then call the normal loadExperimentAndProcess routine.
	 * @param sFilename
	 */
	public boolean loadExperimentAndProcess(String sFilename) {
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		boolean bCallerView = objPref.getBoolean(PreferenceConstants.P_CALLER_VIEW);
		return this.loadExperimentAndProcess(sFilename, bCallerView);
	}
	
	/**
	 * Load an XML experiment file based on the filename (uncheck for its inexistence)
	 * This method will display errors whenever encountered.
	 * This method does not include post-processing and generating scope views
	 * @param sFilename: the xml experiment file
	 */
	public Experiment loadExperiment(String sFilename) {
		Experiment experiment = null;
		// first view: usually already created by default by the perspective
		org.eclipse.swt.widgets.Shell objShell = this.objPage.getWorkbenchWindow().getShell();
		try
		{
			experiment = new Experiment();
			experiment.open( new java.io.File(sFilename), new ProcedureAliasMap() );

		} catch(java.io.FileNotFoundException fnf)
		{
			System.err.println("File not found:" + sFilename + "\tException:"+fnf.getMessage());
			MessageDialog.openError(objShell, "Error:File not found", "Cannot find the file "+sFilename);
			experiment = null;
		}
		catch(java.io.IOException io)
		{
			System.err.println("IO error:" +  sFilename + "\tIO msg: " + io.getMessage());
			MessageDialog.openError(objShell, "Error: Unable to read", "Cannot read the file "+sFilename);
			experiment = null;
		}
		catch(InvalExperimentException ex)
		{
			String where = sFilename + " " + " " + ex.getLineNumber();
			System.err.println("$" +  where);
			MessageDialog.openError(objShell, "Incorrect Experiment File", "File "+sFilename 
					+ " has incorrect tag at line:"+ex.getLineNumber());
			experiment = null;
		} 
		catch(NullPointerException npe)
		{
			System.err.println("$" + npe.getMessage() + sFilename);
			MessageDialog.openError(objShell, "File is invalid", "File has null pointer:"
					+sFilename + ":"+npe.getMessage());
			experiment = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return experiment;
	}
	
	/**
	 * Retrieve the list of all used views
	 * @return list of views
	 */
	public BaseScopeView[] getViews() {
		return this.arrScopeViews;
	}
	
	/***
	 * set the list of views of this experiment
	 * 
	 * @param views
	 */
	public void setViews(BaseScopeView[] views) {
		arrScopeViews = views;
	}
	
	/**
	 * Generate multiple views for an experiment depending on the number of root scopes
	 * @param experiment Experiment data
	 */
	public void generateView(Experiment experiment) {
		
        IWorkbenchWindow window = this.objPage.getWorkbenchWindow();
		// register this new database with our viewer window
		ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		if (vWin == null) {
			System.out.printf("ExperimentManager.setExperiment: ViewerWindow class not found\n");
		}

		// Create a database object to record information about this particular database 
		// being opened.  This information is needed to be able to close and clean up 
		// resources from this database.
		Database db = new Database();
		db.setExperimentView(this);
		// add the database to this viewer window
		if (vWin.addDatabase(db) < 0) {
			return;     // we already issued a dialog message to notify user the open failed.
		}

		db.setExperiment(experiment);		// set the experiment class used for the database
        
		// the view index has values from 0-4 and is used to index arrays (layout folders and possibly others)
		final String viewIdx = Integer.toString(vWin.reserveDatabaseNumber());

		// next, we retrieve all children of the scope and display them in separate views
		TreeNode []rootChildren = experiment.getRootScopeChildren();
		int nbChildren = rootChildren.length;
		arrScopeViews = new BaseScopeView[nbChildren];
		
		for(int k=0;nbChildren>k;k++)
		{
			RootScope child = (RootScope) rootChildren[k];
			try {
				BaseScopeView objView; 
				objView = openView(objPage, child, viewIdx, db, VIEW_STATE_INIT);
				// every root scope type has its own view
				arrScopeViews[k] = objView;
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
		// update the window title if necessary
		WindowTitle wt = new WindowTitle();
		wt.refreshAllTitles();
	}
	
	
	
	/***
	 * Standard method to open a scope view (cct, caller tree or flat tree)
	 * 
	 * @param page : current page where the view has to be hosted
	 * @param root : the root scope
	 * @param secondaryID : aux id for the view
	 * @param db : database
	 * @param viewState : state of the view (VIEW_ACTIVATE, VIEW_VISIBLE, ... ) 
	 * 						OR VIEW_STATE_INIT for the default
	 * 
	 * @return	the view
	 * @throws PartInitException
	 */
	static public BaseScopeView openView(IWorkbenchPage page, RootScope root, String secondaryID, 
			Database db, int viewState ) 
			throws PartInitException {
		
		BaseScopeView objView = null;
		
		if (root.getType() == RootScopeType.CallingContextTree) {
			int state = (viewState<=0? IWorkbenchPage.VIEW_ACTIVATE : viewState);
			// using VIEW_ACTIVATE will cause this one to end up with focus (on top).
			objView = (BaseScopeView) page.showView(ScopeView.ID , secondaryID, state); 
			
			if (viewState == VIEW_STATE_INIT) {
				objView.setInput(db, root, false);
			}

		} else if (root.getType() == RootScopeType.CallerTree) {
			if (viewState != VIEW_STATE_INIT) {
				objView = (BaseScopeView) page.showView(CallerScopeView.ID , secondaryID, IWorkbenchPage.VIEW_ACTIVATE);
				// the view has been closed. Need to set the input again
				objView.setInput(db, root, false);
			} else {
				// default situation (or first creation)
				objView = (BaseScopeView) page.showView(CallerScopeView.ID , secondaryID, IWorkbenchPage.VIEW_VISIBLE); 
				
				if (viewState == VIEW_STATE_INIT) {
					// we need to initialize the view since hpcviewer requires every view to have database and rootscope 
					objView.initDatabase(db, root, false);
					
					if (hashWindow == null) {
						hashWindow = new HashMap<IWorkbenchWindow, DynamicViewListener>();
					}
					final IWorkbenchWindow window = page.getWorkbenchWindow();
					
					DynamicViewListener dynamicViewListener = hashWindow.get(window);
					if (dynamicViewListener == null) {
						dynamicViewListener = new DynamicViewListener(window);
						window.getPartService().addPartListener(dynamicViewListener);
						hashWindow.put(window, dynamicViewListener);
					}
					dynamicViewListener.addView(objView, db, root);
				}
			}

		} else if (root.getType() == RootScopeType.Flat) {
			int state = (viewState<=0? IWorkbenchPage.VIEW_VISIBLE : viewState);
			objView = (BaseScopeView) page.showView(FlatScopeView.ID, secondaryID, state); 
			if (viewState == VIEW_STATE_INIT) {
				objView.setInput(db, root, false);
			}
		}
		return objView;
	}

}