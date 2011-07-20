/**
 * 
 */
package edu.rice.cs.hpc.viewer.actions;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.util.WindowTitle;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

/**
 * @author laksonoadhianto
 *
 */
public class CloseDatabase extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// get an array of open databases for this window
		final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		if ( vWin == null) {
			return null;		// get method already issued error dialog
		}
		
		final String[] dbArray = vWin.getDatabasePaths();
		if (dbArray.length == 0) {
			MessageDialog.openError(window.getShell(), 
					"Error: No Open Database's Found.", 
					"There are no databases in this window which can be closed.");
			return null;		// set method already issued error dialog
		}

		List<String> dbList = Arrays.asList(dbArray);

		// put up a dialog with the open databases in the current window in a drop down selection box
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbList, 
			new ArrayContentProvider(), new LabelProvider(), "Select the databases to close:");
		dlg.setTitle("Select Databases");
		dlg.open();
		Object[] selectedDatabases = dlg.getResult();

		if ((selectedDatabases == null) || (selectedDatabases.length <= 0)) {
			return null;
		}
		
		String[] selectedStrings = new String[selectedDatabases.length];
		for (int i=0 ; i<selectedDatabases.length ; i++) {
			selectedStrings[i] = selectedDatabases[i].toString();
			
			vWin.getDb(selectedStrings[i]);
			// remove the database from our database manager information
			int dbNum = vWin.removeDatabase(selectedStrings[i]);
			if (dbNum < 0) {
				// can close views for an entry we could not find
				continue;
			}
		
			IWorkbenchPage curPage = window.getActivePage();

			// close any open editor windows for this database
			org.eclipse.ui.IEditorReference editors[] = curPage.getEditorReferences();
			int nbEditors = editors.length;
			for(int j=0;j<nbEditors;j++) {
				String title = editors[j].getTitle();
				// if this is for the database being closed, remove it (hiding it actually deletes it)
				if (title.startsWith((dbNum+1) + "-")) {
					IEditorPart edPart = editors[j].getEditor(true);
					if (edPart != null) {
						curPage.closeEditor(edPart, false);
					}
				}
			}
			
			// close this databases metrics views
			org.eclipse.ui.IViewReference views[] = curPage.getViewReferences();
			int nbViews = views.length;
			for(int j=0;j<nbViews;j++) {
				IViewPart objPart = views[j].getView(false);
				if (objPart instanceof AbstractBaseScopeView) {
					final AbstractBaseScopeView objView = (AbstractBaseScopeView) objPart;
					final Experiment experiment = objView.getExperiment();
					String xmlFileName = experiment.getXMLExperimentFile().getPath();
					final int dbDir = xmlFileName.lastIndexOf(File.separator);
					xmlFileName = xmlFileName.substring(0, dbDir);
					
					if (selectedStrings[i].equals(xmlFileName)) {
						curPage.hideView(objView);
					}
				}
			}
		}
		
		WindowTitle.refreshAllTitle(window);
		return null;
	}

}
