package edu.rice.cs.hpc.viewer.util;

import java.util.ArrayList;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
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
	 * @param node
	 */
	public void displayFileEditor(Scope.Node node) {
		// get the complete file name
		if(Utilities.isFileReadable(node.getScope())) {
			String sLongName;
			FileSystemSourceFile newFile = ((FileSystemSourceFile)node.getScope().getSourceFile());
			sLongName = newFile.getCompleteFilename();
			int iLine = node.getScope().getFirstLineNumber();
			openFileEditor( sLongName, newFile.getName(), iLine );
		} else
			System.out.println("Source file not available"+ ":"+ "("+node.getScope().getName()+")");
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
	    		System.err.println(sFilename+": File not found.");
	    		 return; // do we need this ?
	    	}
	    	try {
	    		IEditorPart objEditor = openEditorOnFileStore(wbPage, objFile); 
	    		this.setEditorMarker(wbPage, iLineNumber);
	    	} catch (PartInitException e) {
	    		System.err.println("Error opening the file !");
	    		System.err.println(e.getMessage());
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
	public static IEditorPart openEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore) throws PartInitException {
        //sanity checks
        if (page == null) {
			throw new IllegalArgumentException();
		}

        IEditorInput input = getEditorInput(fileStore);
        String editorId = getEditorId(fileStore);
        // forbid eclipse to use an external editor
        // for unknown reason, sometimes eclipse linux version call this external editor
        if(editorId.compareTo(org.eclipse.ui.IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID) == 0) {
        	// use CDT if installed
        	//editorId = "org.eclipse.cdt.ui.editor.CEditor";
        	editorId = EditorsUI.DEFAULT_TEXT_EDITOR_ID;
        }
        editorId = edu.rice.cs.hpc.viewer.util.SourceCodeEditor.ID;
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
