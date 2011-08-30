package edu.rice.cs.hpc.viewer.actions;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.editor.IViewerEditor;
import edu.rice.cs.hpc.viewer.scope.AbstractBaseScopeView;
import edu.rice.cs.hpc.viewer.util.WindowTitle;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

public class CloseExperiment implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public void run(IAction action) {
		// get an array of open databases for this window
		final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		if ( vWin == null) {
			return;		// get method already issued error dialog
		}
		
		final String[] dbArray = vWin.getDatabasePaths();
		if (dbArray.length == 0) {
			MessageDialog.openError(window.getShell(), 
					"Error: No Open Database's Found.", 
					"There are no databases in this window which can be closed.");
			return;		// set method already issued error dialog
		}

		List<String> dbList = Arrays.asList(dbArray);

		// put up a dialog with the open databases in the current window in a drop down selection box
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbList, 
			new ArrayContentProvider(), new LabelProvider(), "Select the databases to close:");
		dlg.setTitle("Select Databases");
		dlg.open();
		Object[] selectedDatabases = dlg.getResult();

		if ((selectedDatabases == null) || (selectedDatabases.length <= 0)) {
			return;
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
			final org.eclipse.ui.IEditorReference editors[] = curPage.getEditorReferences();
			for (IEditorReference editor: editors) {
				IEditorPart edPart = editor.getEditor(false);
				
				// ----------------------------------------------------------
				// if the editor is an instance of hpcviewer's editor, then we close it
				// 		if the database associated with it is the same as the database we
				//		want to close
				// ----------------------------------------------------------
				if (edPart instanceof IViewerEditor) {
					final IViewerEditor viewerEditor = (IViewerEditor) edPart;
					final Experiment exp = viewerEditor.getExperiment();
					final File dir = exp.getDefaultDirectory();
					
					// ----------------------------------------------------------
					// at the moment we don't have mechanism to compare database
					// thus, we just compare the path 
					// ----------------------------------------------------------
					if (dir.getAbsolutePath().equals(selectedStrings[i])) {
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
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
