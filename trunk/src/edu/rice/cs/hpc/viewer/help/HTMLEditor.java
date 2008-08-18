/**
 * 
 */
package edu.rice.cs.hpc.viewer.help;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.SWT;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author laksonoadhianto
 *
 */
public class HTMLEditor extends EditorPart {
	public static String ID = "edu.rice.cs.hpc.viewer.util.HTMLEditor";
	/**
	 * SWT Browser 
	 */
	protected Browser browser;
	/**
	 * The address of URL document ot display
	 */
	protected String sURI;
	/**
	 * Default constructor
	 */
	public HTMLEditor() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// TODO Auto-generated method stub
		this.setSite(site);
		this.setInput(input);
		if(input instanceof FileEditorInput) {
			//if(browser != null){
				FileEditorInput objInput = (FileEditorInput) input;
				IFile file = objInput.getFile();
				if(file != null) {
					IPath objPath = file.getFullPath();
					Path objAbsPath = new Path(getAbsolutePath(this));
					//IFileStore objFileStore = EFS.getLocalFileSystem().getStore(objPath);
					sURI = "file:///"+objAbsPath.removeLastSegments(1)+"/"+objPath.toString();
				}
				//browser.setUrl(sURI);
			//}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		try {
			browser = new Browser(parent, SWT.BORDER);
		} catch (SWTError e) {
			e.printStackTrace();
			return;
		}
		// set the property
		browser.setData(HTMLEditor.ID, this);
		// display the URL
		browser.setUrl(sURI);
		// display the original source of the HTML file (debugging purpose)
		this.setContentDescription(sURI);
		// set the title of the editor
		this.setPartName(this.getEditorInput().getName());
		
		// set the layout: the browser has to be expanded as much as possible
		GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(browser);
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		if(browser != null)
			browser.setFocus();
	}
	
	/**
	 * Finding the absolute path of the object/class.
	 * This method is general enough to be in static utilities
	 * @param o
	 * @return
	 */
	public String getAbsolutePath(Object o){
		java.security.ProtectionDomain pd = o.getClass().getProtectionDomain();
		if ( pd == null ) return null;
		java.security.CodeSource cs = pd.getCodeSource();
		if ( cs == null ) return null;
		java.net.URL url = cs.getLocation();
		if ( url == null ) return null;
		java.io.File f = new java.io.File( url.getFile() );
		if (f == null) return null;

		return f.getAbsolutePath();
		}
}
