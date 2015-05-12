package edu.rice.cs.hpc.filter.action;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.filter.pattern.PatternValidator;

/*********************************************************************
 * 
 * Input dialog for filter, can be used for adding a filter pattern or
 * editing the values.
 *
 *********************************************************************/
public class FilterInputDialog extends InputDialog 
{
	private Button btnEnable, btnDisable ;
	private Combo cbAttribute;
	
	private FilterAttribute attribute;

	/*******
	 * Constructor for the dialog.
	 * @param parentShell : the parent shell
	 * @param dialogTitle : the title of the dialog box
	 * @param initialValue : the value of the pattern (can be null)
	 * @param attrType : the value of attribute type (can be null)
	 */
	public FilterInputDialog(Shell parentShell, String dialogTitle, String initialValue, FilterAttribute attribute) 
	{
		super(parentShell, dialogTitle, "Use a glob pattern to define a filter." + 
				" For instance, a MPI* will filter all MPI routines", initialValue,
				new PatternValidator());
		
		this.attribute = attribute;
	}
	
	/****
	 * retrieve the new filter attribute
	 * 
	 * @return FilterAttribute
	 */
	public FilterAttribute getAttribute()
	{
		return attribute;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.InputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) 
	{
		final Composite container = (Composite) super.createDialogArea(parent);	
		
		final Composite attArea   = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(attArea);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(attArea);
		
		final Label lblAttribute  = new Label(attArea, SWT.LEFT);
		lblAttribute.setText("Attribute:");
		
		cbAttribute   = new Combo(attArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		final String []names_attr   = FilterAttribute.getFilterNames();
		cbAttribute.setItems(names_attr);
		
		btnEnable = new Button(attArea, SWT.RADIO);
		btnEnable.setText("Enabled");
		btnDisable = new Button(attArea, SWT.RADIO);
		btnDisable.setText("Disabled");
		
		if (attribute != null)
		{
			cbAttribute.setText(attribute.getFilterType());
			if (attribute.enable)
			{
				btnEnable.setSelection(true);
			} else {
				btnDisable.setSelection(true);
			}
		}
		
		return container;
	}
	
	
    /*
     * (non-Javadoc) Method declared on Dialog.
     */
	@Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	attribute = new FilterAttribute();
        	attribute.enable = btnEnable.getSelection();

        	if (!attribute.enable && !btnDisable.getSelection())
        	{
        		setErrorMessage("User has to enable/disable the filter.");
            	getOkButton().setEnabled(true);
        		return;
        	}
        	int ordinal = cbAttribute.getSelectionIndex();
        	if (ordinal < 0)
        	{
        		setErrorMessage("User has to select a filter attribute.");
            	getOkButton().setEnabled(true);
        		return;
        	}
        	attribute.filterType = FilterAttribute.Type.values()[ordinal];
        }
        super.buttonPressed(buttonId);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setText("Test dialog filter");
		
		FilterInputDialog dialog = new FilterInputDialog(shell, "Toto", "initial", null);
		if (dialog.open() == Dialog.OK){
			System.out.println("pattern: " + dialog.getValue());
			System.out.println("att: " + dialog.getAttribute());
		}
	}
}
