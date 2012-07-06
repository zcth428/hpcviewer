package edu.rice.cs.hpc.traceviewer.filter;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;


/*****
 * 
 * Filter dialog to create/edit filter glob pattern of processes
 *
 */
public class FilterDialog extends TitleAreaDialog {

	private List list;
	private FilterTimeline filter;
	
	/****
	 * constructor for displaying filter glob pattern
	 * @param parentShell
	 */
	public FilterDialog(Shell parentShell, FilterTimeline f) {
		super(parentShell);
		filter = f;
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Group grpFilter = new Group(composite, SWT.SHADOW_ETCHED_IN);
		grpFilter.setText("Filter");
		
		Button btnAdd = new Button(grpFilter, SWT.PUSH);
		btnAdd.setText("Add");
		btnAdd.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				InputDialog dlg = new InputDialog(getShell(), "Add a pattern", 
						"Please type a glob pattern", "*", null);
				if (dlg.open() == Dialog.OK) {
					list.add(dlg.getValue());
				}
			}
		});
		
		Button btnRemove = new Button(grpFilter, SWT.PUSH);
		btnRemove.setText("remove");
		btnRemove.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = list.getSelectionCount();
				if (i > 0) {
					final String item = list.getSelection()[0];
					if (MessageDialog.openQuestion(getShell(), "Removing a pattern", 
							"Do you want to remove: " + item + " ?")) {
						list.remove(item);
					}
				}
			}
		});
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(grpFilter);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpFilter);
		
		list = new List(composite, SWT.SINGLE);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(list);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);
		
		this.setMessage("Add/remove glob patterns to filter displayed processes");
		this.setTitle("Filter pattern");
		
		// add pattern into the list
		if (filter != null && filter.getPatterns() != null) {
			for (String str : filter.getPatterns()) {
				list.add(str);
			}
		}

		return parent;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		ArrayList<String> filterList = new ArrayList<String>();
		for(int i=0; i<list.getItemCount(); i++) {
			String item = list.getItem(i);
			filterList.add(i, item);
		}
		// put the glob pattern back
		filter.setPatterns(filterList);
		
		super.okPressed();
	}
	
	/******
	 * unit test for the dialog window
	 * @param arg
	 */
	static public void main(String arg[]) {
		FilterTimeline f = new FilterTimeline();
		ArrayList<String> list = new ArrayList<String>();
		list.add("*.*");
		f.setPatterns(list);
		
		Display display = new Display();
		FilterDialog dlg = new FilterDialog(display.getActiveShell(), f);
		
		if (dlg.open() == Dialog.OK ) {
			System.out.println("list: " + f.getPatterns().size());
		} else {
			System.err.println("cancel: " + f.getPatterns().size());
		}
	}
}
