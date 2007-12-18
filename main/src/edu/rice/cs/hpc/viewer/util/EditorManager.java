package edu.rice.cs.hpc.viewer.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchSite;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import org.eclipse.ui.IWorkbenchWindow;

public class EditorManager {
    private IWorkbenchWindow windowCurrent;

    /**
     * 
     * @param window
     */
    public EditorManager(IWorkbenchWindow window) {
    	this.windowCurrent = window;
    }
    
	public EditorManager(IWorkbenchSite site) {
		this.windowCurrent = site.getWorkbenchWindow();
	}
	/**
	 * Open and Display editor
	 * @param node
	 */
	public void displayFileEditor(Scope.Node node) {
		// get the complete file name
		FileSystemSourceFile newFile = ((FileSystemSourceFile)node.getScope().getSourceFile());
		if(newFile!=null) {
			if(newFile.isAvailable()) {
				String sLongName;
				sLongName = newFile.getCompleteFilename();
				int iLine = node.getScope().getFirstLineNumber();
				openFileEditor( sLongName, newFile.getName(), iLine );
			} else
				System.out.println("Source file not available"+ ":"+ "("+newFile.getName()+")");
			// laks: try to show the editor
		} else
			System.err.println("ScopeView-displayFileEditor:"+node.getScope().getShortName());
	}
	
	/**
	 * Open a new editor (if necessary) into Eclipse
	 * The filename should be a complete absolute path to the local file
	 * @param sFilename
	 */
	public void openFileEditor(String sFilename) {
		java.io.File objInfo = new java.io.File(sFilename);
		if(objInfo.exists())
			this.openFileEditor(sFilename, objInfo.getName(), 1);
		else
			org.eclipse.jface.dialogs.MessageDialog.openError(this.windowCurrent.getShell(), 
					"Error Opening File",
					"File:" +sFilename + "("+objInfo.getName()+") does not exist");
	}
	
	/**
	 * Open Eclipse IDE editor for a given filename. 
	 * Beware: for Eclipse 3.2, we need to create a "hidden" project of the file
	 * 			this project should be cleaned in the future !
	 * @param sFilename the complete path of the file to display in IDE
	 */
	private void openFileEditor(String sLongFilename, String sFilename, int iLineNumber) {
		// get the complete path of the file
		org.eclipse.core.filesystem.IFileStore objFile = 
			org.eclipse.core.filesystem.EFS.getLocalFileSystem().getStore(new 
					org.eclipse.core.runtime.Path(sLongFilename).removeLastSegments(1));
		// get the active page for the editor
		org.eclipse.ui.IWorkbenchPage wbPage = this.windowCurrent.getActivePage();
		if(wbPage != null ){
			//objFile=objFile.getChild(objFile.fetchInfo().getName());
			objFile=objFile.getChild(sFilename);
	    	if(!objFile.fetchInfo().exists()) {
	    		 MessageDialog.openInformation(this.windowCurrent.getShell(), "File not found", 
	    		 	sFilename+": File cannot be opened or does not exist in " + objFile.getName());
	    		 return; // do we need this ?
	    	}
	    	try {
	    		IEditorPart objEditor = org.eclipse.ui.ide.IDE.openEditorOnFileStore(wbPage, objFile);
	    		/*IContentOutlinePage outlinePage = (IContentOutlinePage) objEditor.getAdapter(IContentOutlinePage.class);
	    		 if (outlinePage != null) {
	    		    // editor wishes to contribute outlinePage to content outline view
	    			 
	 	    		IViewPart objOutlineView = wbPage.showView("org.eclipse.ui.views.ContentOutline");
	 	    		wbPage.showView(this.ID);
	 	    		this.setFocus();	 	    		
	 	    		this.treeViewer.getTree().setFocus();
	    		 }
		    	System.out.println(" ScopeView: " + objEditor.getClass() + " outline: "+ outlinePage.getClass());
		    	*/
	    		this.setEditorMarker(wbPage, iLineNumber);
	    	} catch (PartInitException e) {
	    		e.printStackTrace();
	    		MessageDialog.openError(this.windowCurrent.getShell(), "Error opening the file", e.getMessage());
	       /* some code */
	     }
		}
	}

	/**
	 * Set the marker into the active editor
	 * @param wbPage
	 * @param iLineNumber
	 */
	private void setEditorMarker(org.eclipse.ui.IWorkbenchPage wbPage, int iLineNumber) {
	       //IFile file;
	       try{
	    	   IResource resource = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
	    	   IMarker marker=resource.createMarker("HPCViewer"); 
			   marker.setAttribute(IMarker.LINE_NUMBER, iLineNumber+1);
			   marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
			   org.eclipse.ui.ide.IDE.gotoMarker(wbPage.getActiveEditor(), marker);
	    	   
	       } catch (org.eclipse.core.runtime.CoreException e) {
	    	   e.printStackTrace();
	       }

	}

}
