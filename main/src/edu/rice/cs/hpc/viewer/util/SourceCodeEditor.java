/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.editors.text.TextEditor;
/**
 * @author laksono
 *
 */
public class SourceCodeEditor extends TextEditor {
	static public String ID = "edu.rice.cs.hpc.viewer.util.SourceCodeEditor";  
	/*
	public SourceCodeEditor() {
		super();
	}*/
	/*
	 * @see org.eclipse.ui.texteditor.StatusTextEditor#validateEditorInputState()
	 * @since 3.3
	 */
	public boolean validateEditorInputState() {
		//return super.validateEditorInputState();
		return (new org.eclipse.ui.texteditor.StatusTextEditor()).validateEditorInputState();
	}

}
