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

import com.onpositive.richtexteditor.viewer.RichTextViewer;


/**
 * @author laksonoadhianto
 *
 */
public class HTMLEditor extends EditorPart {
	public static String ID = "edu.rice.cs.hpc.viewer.util.HTMLEditor";

	
	private RichTextViewer richTextViewer;
	
	/**
	 * SWT Browser 
	 */
	protected Browser browser;
	/**
	 * The address of URL document ot display
	 */
	private String sURI;
	private String sFilePath;
	
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
					this.sFilePath = objAbsPath.removeLastSegments(1)+objPath.toString();
					sURI = "file:///"+sFilePath;
				}
//				browser.setUrl(sURI);
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
			// attempt to instantiate browser widget
			browser = new Browser(parent, SWT.BORDER);
			// set the property
			browser.setData(HTMLEditor.ID, this);
			// display the URL
			GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);
			// Laksono 2009.02.16: on windows the following layout will shrink the browser :-(
			//GridLayoutFactory.fillDefaults().numColumns(1).applyTo(browser);
			browser.setUrl(sURI);

		} catch (SWTError e) {
			// if the platform doesn't support pluggable browser,
			//   then we launch an internal SWT browser
			richTextViewer = new SimpleViewerHTML(parent, SWT.BORDER);
			this.richTextViewer.setData(HTMLEditor.ID, this);
			this.richTextViewer.setEditable(false);
			this.richTextViewer.getLayerManager().openHTMLFile(sFilePath);

			/*
			org.eclipse.swt.widgets.Shell objShell = this.getSite().getShell();
			// find the default external browser
			String sBrowser = System.getenv(this.sDEFAULT_BROWSER);

			if(sBrowser != null) {
				// launch the external browser
				org.eclipse.swt.program.Program.launch(sBrowser+" "+sURI);
			} else {
				// the variable is not set or there is no external browser
				org.eclipse.jface.dialogs.MessageDialog.openWarning(objShell,
					"Unset default browser", "The environment variable "+this.sDEFAULT_BROWSER+
					" has not been set.\n"+
					"The help file can be displayed on "+sURI+
					" with your browser.");
			}
			this.getEditorSite().getPage().closeEditor(this, false);
			return;
			*/
		}
		// display the original source of the HTML file (debugging purpose)
		this.setContentDescription(sURI);
		// set the title of the editor
		this.setPartName(this.getEditorInput().getName());
		
		// set the layout: the browser has to be expanded as much as possible
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		//if(browser != null)
		//	browser.setFocus();
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
