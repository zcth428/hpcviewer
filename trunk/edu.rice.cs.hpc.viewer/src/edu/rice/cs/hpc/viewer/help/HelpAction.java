/**
 * 
 */
package edu.rice.cs.hpc.viewer.help;

import java.io.IOException;
import java.net.URL;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;


/**
 * @author laksonoadhianto
 *
 */
public class HelpAction extends Action {
	static public String ID="edu.rice.cs.hpc.viewer.help.HelpAction";
	final static private String HELP_FILE_PATH = "/doc/hpcviewer-users-manual.html";
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

		//String sFilePath = this.getHelpPath();
		IFile file = root.getFile(new Path(HELP_FILE_PATH));
		
		IWorkbenchPage objPage = this.objWindow.getActivePage(); 
				
		try {
			FileEditorInput objInput = new FileEditorInput(file);
			objPage.openEditor(objInput, HTMLEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	
	/****
	 * retrieve help path of the local resource
	 * @return
	 */
	private String getHelpPath() {
		// URL to the root ("/") of the plugin-path:
		URL relativeURL = Platform.getBundle("edu.rice.cs.hpc.viewer").getEntry(HELP_FILE_PATH);

		// Turn relative path to a local path with the help of Eclipse-platform:
		URL localURL;
		try {
			localURL = FileLocator.resolve(relativeURL);

			// From this you can get the path
			String pluginDirString = localURL.getPath();
			
			return pluginDirString;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
