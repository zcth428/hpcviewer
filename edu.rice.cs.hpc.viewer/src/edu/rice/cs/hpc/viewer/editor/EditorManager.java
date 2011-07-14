package edu.rice.cs.hpc.viewer.editor;

import java.io.FileNotFoundException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSashContainer;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.editors.text.EditorsUI;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.viewer.util.Utilities;

/**
 * Class specifically designed to manage editor such as displaying source code editor
 * @author la5
 *
 */
public class EditorManager {
    private IWorkbenchWindow windowCurrent;


    /**
     * 
     * @param window
     */
    public EditorManager(IWorkbenchWindow window) {
    	String sLine = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
    	EditorsUI.getPreferenceStore().setValue(sLine, true);
    	this.windowCurrent = window;
    }
    
	public EditorManager(IWorkbenchSite site) {
		this(site.getWorkbenchWindow());
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
			// lets get the database number being used for this file
			Experiment experiment = scope.getExperiment();
			
			String sLongName;
			FileSystemSourceFile newFile = ((FileSystemSourceFile)scope.getSourceFile());
			sLongName = newFile.getCompleteFilename();
			int iLine = scope.getFirstLineNumber();
			openFileEditor( sLongName, newFile.getName(), iLine, experiment );
		}
	}
	
	/**
	 * Open a new editor (if necessary) into Eclipse
	 * The filename should be a complete absolute path to the local file
	 * @param sFilename
	 */
	public void openFileEditor(String sFilename, Experiment experiment) 
	throws FileNotFoundException
	{
		java.io.File objInfo = new java.io.File(sFilename);
		if(objInfo.exists())
			this.openFileEditor(sFilename, objInfo.getName(), 1, experiment);
		else
			// Laks: 12.1.2008: return the filename in case the file is not found
			throw new FileNotFoundException(sFilename);
	}
	
	/**
	 * Open Eclipse IDE editor for a given filename. 
	 * Beware: for Eclipse 3.2, we need to create a "hidden" project of the file
	 * 			this project should be cleaned in the future !
	 * @param sFilename the complete path of the file to display in IDE
	 */
	private void openFileEditor(String sLongFilename, String sFilename, int iLineNumber, Experiment experiment)
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
				openEditorOnFileStore(wbPage, objFile, experiment);
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
	private static IEditorPart openEditorOnFileStore(IWorkbenchPage page, IFileStore fileStore, Experiment experiment) throws PartInitException {
		//sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// If there is already a editor area partition for this database, give one of its editors focus 
		// so that this new editor will display in the same partition.  If not we will create a new partition
		// and put the new editor in it after we open the editor.
		IEditorReference[] editorReferences = page.getEditorReferences();
		
		// assume there are no editors open and that we will not need a new partition
		boolean needNewPartition = false;
		// if at lease one editor already open, we may need new partition (see below)
		if (editorReferences.length > 0) {
			needNewPartition = true;
		}
		
		// check if there is already an editor opened for this database
		final String expFile = experiment.getXMLExperimentFile().getPath();
		for(IEditorReference editorRef: editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);
			
			if (editor instanceof IViewerEditor) {
				Experiment expEditor = ((IViewerEditor)editor).getExperiment();
				final String expEditorFile = expEditor.getXMLExperimentFile().getPath();
				if (expFile.equals(expEditorFile)) {
					// An editor has already opened, do not split the pane
					needNewPartition = false;
					break;
				}
			}
		}

		IEditorInput input = getEditorInput(fileStore);
		//String editorId = getEditorId(fileStore);
		// forbid eclipse to use an external editor
		String editorId = edu.rice.cs.hpc.viewer.editor.SourceCodeEditor.ID;
		// open the editor on the file
		IEditorPart iep = page.openEditor(input, editorId);
		// if we want a database number prefix, add it to the editor title
		if (iep instanceof SourceCodeEditor) {
			SourceCodeEditor sce = (SourceCodeEditor)iep;
			sce.setExperiment(experiment);
			sce.resetPartName();
				// database numbers start with 0 but titles start with 1
				//sce.setPartNamePrefix((dbNum+1) + "-");
		}
		
		// if this is the first editor from this database, create new partition to put it in
		if (needNewPartition == true) {
			splitEditorArea(iep);
		}
		return iep;
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
		return new FileStoreEditorInput(fileStore);
	}

	//-------========================= END TAKEN FROM IDE ===============

	
	/**
	 * Split the editor area for this new editor.
	 */
//	@SuppressWarnings("restriction")
	private static void splitEditorArea(IEditorPart iep) {
		PartPane partPane = ((PartSite) iep.getSite()).getPane();
		// Get PartPane that correspond to the active editor
		PartPane currentEditorPartPane = ((PartSite) iep.getSite()).getPane();
		EditorSashContainer editorSashContainer = null;
		ILayoutContainer rootLayoutContainer = partPane.getPart().getContainer();
		if (rootLayoutContainer instanceof LayoutPart) {
			ILayoutContainer editorSashLayoutContainer = ((LayoutPart) rootLayoutContainer).getContainer();
			if (editorSashLayoutContainer instanceof EditorSashContainer) {
				editorSashContainer = ((EditorSashContainer) editorSashLayoutContainer);
			}
		}

		/*
		 * Create a new part stack (i.e. a workbook) to home the
		 * currentEditorPartPane which hold the active editor
		 */
		PartStack newPart = createStack(editorSashContainer);
		editorSashContainer.stack(currentEditorPartPane, newPart);
		if (rootLayoutContainer instanceof LayoutPart) {
			ILayoutContainer cont = ((LayoutPart) rootLayoutContainer).getContainer();
			if (cont instanceof PartSashContainer) {
				// "Split" the editor area by adding the new part
				((PartSashContainer) cont).add(newPart);
			}
		}
	}
	/**
	 * A method to create a part stack container (a new workbook)
	 * 
	 * @param editorSashContainer the <code>EditorSashContainer</code> to set for the returned <code>PartStack</code>
	 * @return a new part stack container
	 */
//	@SuppressWarnings("restriction")
	private static PartStack createStack(EditorSashContainer editorSashContainer) {
		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		EditorStack newWorkbook = EditorStack.newEditorWorkbook(editorSashContainer, (WorkbenchPage)workbenchPage);
		return newWorkbook;
	}

}
