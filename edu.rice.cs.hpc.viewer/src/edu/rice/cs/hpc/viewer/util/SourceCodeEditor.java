package edu.rice.cs.hpc.viewer.util;

import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
/**
 * @author laksono
 *
 */
public class SourceCodeEditor extends TextEditor {
	static public String ID = "edu.rice.cs.hpc.viewer.util.SourceCodeEditor";  
	
	public SourceCodeEditor() {
		super();
	}
	
	/**
	 * Disable editing the editor 
	 * 	 
	 * */
	public boolean validateEditorInputState() {
		return ((AbstractTextEditor)(this)).validateEditorInputState();
	}

	/**
	 * Override the AbstractDecoratedTextEditor method to force to show
	 * the line number
	 */
	protected boolean isLineNumberRulerVisible() {
		this.getPreferenceStore().setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, true);
		return true;
	}
	
	public void setPartNamePrefix(String partNamePrefix) {
		String partName = super.getPartName();
		if (partName.startsWith(partNamePrefix)) {
			// this method gets called each time you click the program scope but if the source file editor is 
			// already open we have already put the database number in the title and we do not want to do it again.
			return;
		}
		super.setPartName(partNamePrefix + partName);
		return;
	}
	
	/**
	 * Description copied from interface: ITextEditor
	 * Returns whether the text in this text editor can be changed by the user.
	 */
	public boolean isEditable() {
		return false;
	}
}
