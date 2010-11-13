/**
 * 
 */
package edu.rice.cs.hpc.viewer.help;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.PartInitException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

//import edu.rice.cs.hpc.viewer.util.EditorManager;

/**
 * @author laksonoadhianto
 *
 */
public class HelpAction extends Action {
	static public String ID="edu.rice.cs.hpc.viewer.help.HelpAction";
	protected IWorkbenchWindow objWindow;
	
	public HelpAction(IWorkbenchWindow window) {
		this.objWindow = window;
		this.setId(HelpAction.ID);
		this.setText("hpcviewer help");
		this.setDescription("Show the help document of hpcviewer");
	}
	
	public void run() {
		
		IWorkspace objWorkspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = objWorkspace.getRoot();
		//java.net.URL objURL = this.getClass().getResource("hpcviewer.html");
		
		//Path location = new Path(objURL.getFile());
		Path location = new Path("doc/hpcviewer.html");
		IFile file = root.getFile(location);
		
		IWorkbenchPage objPage = this.objWindow.getActivePage(); 
		FileEditorInput objInput = new FileEditorInput((IFile)file);
		try {
			objPage.openEditor(objInput, HTMLEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
