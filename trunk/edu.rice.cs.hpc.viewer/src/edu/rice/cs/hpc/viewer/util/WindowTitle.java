package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.editor.IViewerEditor;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/***
 * 
 * class to handle window's title
 *
 */
public class WindowTitle {
	final private static String MAIN_TITLE = "hpcviewer";
	
	
	/***
	 * A simple title construction given a specified number of databases
	 * 
	 * @param window
	 * @param experiment
	 * @param sTitle
	 * @param numDB
	 * @return
	 */
	static private String getViewTitle(IWorkbenchWindow window, Experiment experiment, String sTitle, int numDB) {
		
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
	 * 
	 * @param window
	 * @param dbIndex
	 * @param sTitle
	 * @return
	 */
	static public String getTitle(IWorkbenchWindow window, int dbIndex, String sTitle) {
		int numDB =  ViewerWindowManager.getNumberOfDatabases(window);
		
		if (numDB <= 1) {
			return sTitle;
		} else {
			return dbIndex + "-" + sTitle ;
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
		// refresh the view
		refreshViewTitle(window);
		// refresh the editors
		refreshEditorTitle(window);
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
		
		final int numDB = ViewerWindowManager.getNumberOfDatabases(window); 
			
		final IViewReference viewRefs[] = window.getActivePage().getViewReferences();
		for (IViewReference viewRef: viewRefs) {
			
			final IViewPart view = viewRef.getView(false);
			if (view instanceof BaseScopeView) {
				final BaseScopeView scopeView = (BaseScopeView) view;
				final Experiment exp = scopeView.getExperiment();
				final String title = getViewTitle(window, exp, 
						((BaseScopeView) view).getRootScope().getRootName(), numDB);
				((BaseScopeView) view).setViewTitle(title);
			}
		}
	}
	
	/***
	 * reset the title of all editors
	 * 
	 * @param window
	 */
	static private void refreshEditorTitle(IWorkbenchWindow window) {
		final IEditorReference editors[] = window.getActivePage().getEditorReferences();
		for (IEditorReference editor: editors) {
			IEditorPart editorPart = editor.getEditor(false);
			if (editorPart instanceof IViewerEditor) {
				((IViewerEditor)editorPart).resetPartName();
			} else {
				System.err.println("unknown editor for " + editorPart.getTitle() + ":" + editorPart.getClass());
			}
		}
		
	}
}
