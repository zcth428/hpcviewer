package edu.rice.cs.hpc.viewer.util;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.scope.ScopeActions;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage, IPropertyChangeListener  {

	private DirectoryFieldEditor objDirectory;
	private StringFieldEditor objThreshold;
	private FontFieldEditor objFontMetric;
	private FontFieldEditor objFontGeneric;
	
	/**
	 * 
	 */
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		BooleanFieldEditor objCallerViewFlag = new BooleanFieldEditor(PreferenceConstants.P_CALLER_VIEW,
				"Show caller view", getFieldEditorParent());
		addField(objCallerViewFlag);
		
		objDirectory = new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Default database directory:", getFieldEditorParent()); 
		addField(objDirectory); 
		
		objThreshold = new StringFieldEditor(PreferenceConstants.P_THRESHOLD,
				"&Threshold for hot call path\n(fraction from parent metric value, between 0.0 and 1.0)", 
				this.getFieldEditorParent());
		objThreshold.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		objThreshold.setEmptyStringAllowed(true);
		objThreshold.setStringValue(String.valueOf(ScopeActions.fTHRESHOLD));
		addField(objThreshold);
		
		this.objFontMetric = new FontFieldEditor(PreferenceConstants.P_FONT_METRIC,
				"Font for metric columns", getFieldEditorParent());
		addField(this.objFontMetric);
		this.objFontGeneric = new FontFieldEditor(PreferenceConstants.P_FONT_GENERIC,
				"Font for view/editor", getFieldEditorParent());
		
		addField(this.objFontGeneric);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object o = event.getSource();
		System.out.println("PP: "+o.getClass() + " " + event.getNewValue());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}