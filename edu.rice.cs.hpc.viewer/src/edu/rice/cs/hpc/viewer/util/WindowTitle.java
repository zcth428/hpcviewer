package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.window.Database;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/***
 * 
 * class to handle window's title
 *
 */
public class WindowTitle {
	final private static String MAIN_TITLE = "hpcviewer";
	
	/*****
	 * retrieve the title of a view
	 * if the number of opened databases is more than one, we will add prefix
	 * 
	 * @param window
	 * @param experiment
	 * @param sTitle
	 * @return
	 */
	static public String getViewTitle(IWorkbenchWindow window, Experiment experiment, String sTitle) {
		int numDB =  ViewerWindowManager.getNumberOfDatabases(window);
		
		if (numDB <= 1) {
			return sTitle;
		} else {
			ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
			numDB = 1 + vw.getDbNum(experiment);
			return numDB + "-" + sTitle + "("+experiment.getName()+")";
		}
	}
	
	/***
	 * Get the title of the main window
	 * 
	 * @param window
	 * @param experiment
	 * @return
	 */
	static public String getWindowTitle(IWorkbenchWindow window, Experiment experiment) {
		int numDB =  ViewerWindowManager.getNumberOfDatabases(window);
		
		if (numDB > 1) {
			return (ViewerWindowManager.getWindowNumber(window)+1) + "-" + MAIN_TITLE;
		} else if (experiment != null) {
			return MAIN_TITLE + ": "+experiment.getName();
		} else {
			if (numDB == 1) {
				ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
				Experiment exp[] = vw.getExperiments();
				if (exp != null) {
					return getWindowTitle(window, exp[0]);
				}
			}
			return MAIN_TITLE;			
		}
	}
	
	/***
	 * Get generic title
	 * 
	 * @param window
	 * @param experiment
	 * @param sTitle
	 * @return
	 */
	static public String getTitle(IWorkbenchWindow window, Experiment experiment, String sTitle) {
		int numDB =  ViewerWindowManager.getNumberOfDatabases(window);
		
		if (numDB <= 1) {
			return sTitle;
		} else {
			ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
			numDB = 1 + vw.getDbNum(experiment);
			return numDB + "-" + sTitle ;
		}
	}

	/***
	 * Reset the title of the main window, views and editors
	 * 
	 * @param window
	 * @param experiment: current database
	 */
	static public void refreshAllTitle(IWorkbenchWindow window, Experiment experiment) {
		// refresh the main title
		window.getShell().setText(getWindowTitle(window, experiment));
		refreshViewTitle(window);
	}
	

	/****
	 * Reset the title of the main window
	 * @param window
	 */
	static public void refreshAllTitle(IWorkbenchWindow window) {
		// refresh the main title
		refreshAllTitle(window, null);
	}
	
	
	/****
	 * Reset the title of all views
	 * @param window
	 */
	static private void refreshViewTitle(IWorkbenchWindow window) {
		final ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
		final int numDB = vw.getOpenDatabases();
		
		for (int i=0; i<ViewerWindow.maxDbNum; i++) {
			final Database db = vw.getDb(i);
			if (db != null) {
				final ExperimentView ev = vw.getDb(i).getExperimentView();
				final BaseScopeView views[] = ev.getViews();
				
				for (BaseScopeView view: views) {
					final Experiment exp = view.getExperiment();
					final String sOriginalTitle = view.getTitle();
					char prefix[] = new char[2];
					sOriginalTitle.getChars(0, 1, prefix, 0);
					
					if (numDB>1 && prefix[0] >= '0' && prefix[0]<='9') {
						// it's already prefixed with database number. 
						// do we need to refresh or just no-op ?
					} else {
						// the title is still in the original form
						// need to refresh the title if necessary
						final RootScope root = (RootScope) view.getRootScope();
						view.setViewTitle(getViewTitle(window, exp, root.getRootName()));
					}
				}
			}
		}
	}
}
