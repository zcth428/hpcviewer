package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/****
 * 
 * display procedure and its class
 * can be used for either adding or editing the map
 *
 */
public class ProcedureMapDetailDialog extends Dialog {

	final private String title;
	private String proc;
	private String procClass;
	
	private Text txtProc;
	private Text txtClass;

	/***
	 * retrieve the new procedure name
	 * @return
	 */
	public String getProcedure() {
		return proc;
	}
	
	/**
	 * retrieve the new class
	 * @return
	 */
	public String getProcedureClass() {
		return procClass;
	}
	
	/***
	 * constructor
	 * 
	 * @param parentShell : parent shell
	 * @param title : title of the dialog
	 * @param proc : default name of the procedure
	 * @param procClass : default name of the class
	 */
	protected ProcedureMapDetailDialog(Shell parentShell, String title, String proc, String procClass) {
		super(parentShell);
		
		this.proc = proc;
		this.procClass = procClass;
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
		
		final Label lblProc = new Label(composite, SWT.LEFT);
		lblProc.setText("Procedure: ");
		txtProc = new Text(composite, SWT.LEFT | SWT.SINGLE);
		txtProc.setText(proc);
		GridDataFactory.swtDefaults().hint(
				this.convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.grab(true, false).applyTo(txtProc);
		
		final Label lblClass = new Label(composite, SWT.LEFT);
		lblClass.setText("Class: ");
		txtClass = new Text(composite, SWT.LEFT | SWT.SINGLE);
		txtClass.setText(procClass);
		GridDataFactory.swtDefaults().hint(
				this.convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.grab(true, false).applyTo(txtClass);
		
		return composite;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		proc = txtProc.getText();
		procClass = txtClass.getText();
		super.okPressed();
	}
	
	/***
	 * unit test
	 * 
	 * @param argv
	 */
	static public void main(String argv[]) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		shell.open();
		
		ProcedureMapDetailDialog dlg = new ProcedureMapDetailDialog(shell, "edit", "procedure", "procedure-class");

		dlg.open();
		
		System.out.println("proc: " + dlg.proc + ", class: " + dlg.procClass);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		display.dispose();
	}

}
