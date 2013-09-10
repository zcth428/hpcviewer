package edu.rice.cs.hpc.viewer.actions;

import java.io.FileNotFoundException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.viewer.editor.EditorManager;
import edu.rice.cs.hpc.viewer.editor.SourceCodeEditor;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;


/********************************************************************************
 * 
 * menu handler to show the XML code based on the current active view or editor
 * if the current active view or editor has information about the database,
 * 	then we display its XML file.
 * otherwise this will do nothing
 *
 ********************************************************************************/
public class showXML extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ViewerWindow vw = ViewerWindowManager.getViewerWindow(window);
		
		if (vw == null)
			return null;
		
		final int numDB = vw.getOpenDatabases();
		Experiment experiment = null;
		
		// try to find the current database
		if (numDB == 1)
		{
			// only one opened database
			experiment = vw.getExperiments()[0];
		}else
		{
			// multiple databases are opened:
			// need to select an experiment to show
			IWorkbenchPart part = window.getActivePage().getActivePart();
			if (part instanceof BaseScopeView)
			{
				experiment = ((BaseScopeView)part).getExperiment();
				
			} else if (part instanceof SourceCodeEditor)
			{
				experiment = ((SourceCodeEditor)part).getExperiment();
			}
		}
		
		if (experiment != null)
			showXMLEditor( window, experiment);
		else
		{
			// display an error message here (but not a dialog box, it's annoying)
		}
		
		return null;
	}

	
	/***
	 * show the read-only editor for xml file
	 * 
	 * @param window
	 * @param experiment
	 */
	private void showXMLEditor( IWorkbenchWindow window, Experiment experiment )
	{
		// get the the experiment XML file for the database this program scope is part of
		String filePath = experiment.getXMLExperimentFile().getPath();

		// prepare the editor
		EditorManager editor = new EditorManager(window);
		try {
			// database numbers start with 0 but titles start with 1
			editor.openFileEditor(filePath, experiment);
		} catch (FileNotFoundException e) {
			// can not find the file (or something goes wrong)
			MessageDialog.openError( window.getShell(), "Error: File not found", e.getMessage() );
		}
	}
}
