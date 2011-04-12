package edu.rice.cs.hpc.viewer.util;

import java.util.ArrayList;
import java.io.FileNotFoundException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.editors.text.EditorsUI;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;

/**
 * Class specifically designed to manage editor such as displaying source code editor
 * @author la5
 *
 */
public class EditorManager {
    private IWorkbenchWindow windowCurrent;

    /*
    private void setDefaultEditor() {
    	IEditorRegistry objRegistry;
    	if(this.windowCurrent != null)
    		objRegistry = this.windowCurrent.getWorkbench().getEditorRegistry();
    	else
    		objRegistry = PlatformUI.getWorkbench().getEditorRegistry();
    	String sEditor = org.eclipse.ui.editors.text.TextEditor.class.toString();
    	objRegistry.setDefaultEditor("*", sEditor);
    	objRegistry.setDefaultEditor("*.f*", sEditor);
    } */
    /**
     * 
     * @param window
     */
    public EditorManager(IWorkbenchWindow window) {
    	String sLine = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
    	EditorsUI.getPreferenceStore().setValue(sLine, true);
    	this.windowCurrent = window;
    	//this.setDefaultEditor();
    }
    
	public EditorManager(IWorkbenchSite site) {
		this.windowCurrent = site.getWorkbenchWindow();
    	//this.setDefaultEditor();
	}
	
	/**
	 * Open and Display editor
	 * @param scope of the node
	 */
	public void displayFileEditor(Scope scope) 
	throws FileNotFoundException
	{
		// get the complete file name
		if(Utilities.isFileReadable(scope)) {
			String sLongName;
			FileSystemSourceFile newFile = ((FileSystemSourceFile)scope.getSourceFile());
			sLongName = newFile.getCompleteFilename();
			int iLine = scope.getFirstLineNumber();
			openFileEditor( sLongName, newFile.getName(), iLine );
		}
		//} else
		//	System.out.println("Source file not available"+ ":"+ "("+node.getScope().getName()+")");
	}
	
	/**
	 * Open a new editor (if necessary) into Eclipse
	 * The filename should be a complete absolute path to the local file
	 * @param sFilename
	 */
	public void openFileEditor(String sFilename) 
	throws FileNotFoundException
	{
		java.io.File objInfo = new java.io.File(sFilename);
		if(objInfo.exists())
			this.openFileEditor(sFilename, objInfo.getName(), 1);
		else
			// Laks: 12.1.2008: return the filename in case the file is not found
			throw new FileNotFoundException(sFilename);
			//throw new FileNotFoundException("File not found: "+sFilename);
	}
	
	/**
	 * Open Eclipse IDE editor for a given filename. 
	 * Beware: for Eclipse 3.2, we need to create a "hidden" project of the file
	 * 			this project should be cleaned in the future !
	 * @param sFilename the complete path of the file to display in IDE
	 */
	private void openFileEditor(String sLongFilename, String sFilename, int iLineNumber)
		throws FileNotFoundException
	{
		// get the complete path of the file
		org.eclipse.core.filesystem.IFileStore objFile = 
			org.eclipse.core.filesystem.EFS.getLocalFileSystem().getStore(new 
					org.eclipse.core.runtime.Path(sLongFilename).removeLastSegments(1));
		// get the active page for the editor
		org.eclipse.ui.IWorkbenchPage wbPage = this.windowCurrent.getActivePage();
		if(wbPage != null ){
			objFile=objFile.getChild(sFilename);
	    	if(!objFile.fetchInfo().exists()) {
	    		throw new FileNotFoundException(sFilename+": File not found.");
	    	}
	    	try {
				openEditorOnFileStore(wbPage, objFile);
		    	this.setEditorMarker(wbPage, iLineNumber);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	    	   IMarker marker=resource.createMarker(IMarker.MARKER); 
			   marker.setAttribute(IMarker.LINE_NUMBER, iLineNumber+1);
			   IEditorPart editor = wbPage.getActiveEditor();
			   if (editor != null)
				   org.eclipse.ui.ide.IDE.gotoMarker(wbPage.getActiveEditor(), marker);
	    	   
	       } catch (org.eclipse.core.runtime.CoreException e) {
	    	   e.printStackTrace();
	       }

	}

	//-------========================= TAKEN FROM IDE ===============
	/**
	 * This is "home-made" method of IDE.openEditorOnFileStore since the IDE function
	 * will use external editor on *nix machine for Fortran files !
	 * @param page
	 * @param fileStore
	 * @return
	 * @throws PartInitException
	 */
	private static IEditorPart openEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore) throws PartInitException {
        //sanity checks
        if (page == null) {
			throw new IllegalArgumentException();
		}

        IEditorInput input = getEditorInput(fileStore);
        //String editorId = getEditorId(fileStore);
        // forbid eclipse to use an external editor
        String editorId = edu.rice.cs.hpc.viewer.util.SourceCodeEditor.ID;
        // open the editor on the file
        return page.openEditor(input, editorId);
    }

    /**
     * Get the id of the editor associated with the given <code>IFileStore</code>.
     * 
	 * @param workbench
	 * 	         the Workbench to use to determine the appropriate editor's id 
     * @param fileStore
     *           the <code>IFileStore</code> representing the file for which the editor id is desired
	 * @return the id of the appropriate editor
	 * @since 3.3
	 */
	/*
	private static String getEditorId(IFileStore fileStore) {
		IEditorDescriptor descriptor;
		try {
			descriptor = IDE.getEditorDescriptor(fileStore.getName());
		} catch (PartInitException e) {
			return null;
		}
		if (descriptor != null)
			return descriptor.getId();
		return null;
	}
*/
	/**
	 * Create the Editor Input appropriate for the given <code>IFileStore</code>.
	 * The result is a normal file editor input if the file exists in the
	 * workspace and, if not, we create a wrapper capable of managing an
	 * 'external' file using its <code>IFileStore</code>.
	 * 
	 * @param fileStore
	 *            The file store to provide the editor input for
	 * @return The editor input associated with the given file store
	 */
	private static IEditorInput getEditorInput(IFileStore fileStore) {
		IFile workspaceFile = getWorkspaceFile(fileStore);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new FileStoreEditorInput(fileStore);
	}

	/**
	 * Determine whether or not the <code>IFileStore</code> represents a file
	 * currently in the workspace.
	 * 
	 * @param fileStore
	 *            The <code>IFileStore</code> to test
	 * @return The workspace's <code>IFile</code> if it exists or
	 *         <code>null</code> if not
	 */
	private static IFile getWorkspaceFile(IFileStore fileStore) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(fileStore.toURI());
		files = filterNonExistentFiles(files);
		if (files == null || files.length == 0)
			return null;

		// for now only return the first file
		return files[0];
	}

	/**
	 * Filter the incoming array of <code>IFile</code> elements by removing
	 * any that do not currently exist in the workspace.
	 * 
	 * @param files
	 *            The array of <code>IFile</code> elements
	 * @return The filtered array
	 */
	private static IFile[] filterNonExistentFiles(IFile[] files) {
		if (files == null)
			return null;

		int length = files.length;
		ArrayList<IFile> existentFiles = new ArrayList<IFile>(length);
		for (int i = 0; i < length; i++) {
			if (files[i].exists())
				existentFiles.add(files[i]);
		}
		return (IFile[]) existentFiles.toArray(new IFile[existentFiles.size()]);
	}
	//-------========================= END TAKEN FROM IDE ===============


}
