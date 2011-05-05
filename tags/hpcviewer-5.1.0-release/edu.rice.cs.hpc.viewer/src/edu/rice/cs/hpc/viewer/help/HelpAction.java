/**
 * 
 */
package edu.rice.cs.hpc.viewer.help;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;


/**
 * @author laksonoadhianto
 *
 */
public class HelpAction extends Action {
	final static public String ID="edu.rice.cs.hpc.viewer.help.HelpAction";
	final static private String HELP_FILE_PATH = "/doc/hpcviewer-users-manual.html";
	final private IWorkbenchWindow objWindow;
	
	public HelpAction(IWorkbenchWindow window) {
		this.objWindow = window;
		this.setId(HelpAction.ID);
		this.setText("hpcviewer help");
		this.setDescription("Show the help document of hpcviewer");
	}
	
	public void run() {
		
		IWorkspace objWorkspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = objWorkspace.getRoot();

		IFile file = root.getFile(new Path(HELP_FILE_PATH));
		
		IWorkbenchPage objPage = this.objWindow.getActivePage(); 
				
		try {
			FileEditorInput objInput = new FileEditorInput(file);
			objPage.openEditor(objInput, HTMLEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
