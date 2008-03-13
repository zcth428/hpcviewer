/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
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

	/**
	 * Override the AbstractDecoratedTextEditor method to force to show
	 * the line number
	 */
	protected boolean isLineNumberRulerVisible() {
		this.getPreferenceStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, true);
		return true;
	}
}
