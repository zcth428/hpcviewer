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
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.extdata.Filter;
import edu.rice.cs.hpc.data.experiment.extdata.FilteredBaseData;


/*****
 * 
 * Filter dialog to create/edit filter glob pattern of processes
 *
 */
public class FilterDialog extends TitleAreaDialog {

	private List list;
	private FilteredBaseData filterData;
	private Button btnRemove;
	private Button btnShow;
	
	/****
	 * constructor for displaying filter glob pattern
	 * @param parentShell
	 */
	public FilterDialog(Shell parentShell, FilteredBaseData f) {
		super(parentShell);
		filterData = f;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Group grpMode = new Group(composite, SWT.NONE);
		grpMode.setText("Mode of filter");
		
		btnShow = new Button(grpMode, SWT.RADIO);
		btnShow.setText("To show");
		btnShow.setToolTipText("An option to show matching patterns");

		Button btnHide = new Button(grpMode, SWT.RADIO);
		btnHide.setText("To hide");
		btnHide.setToolTipText("An option to hide matching patterns");
		
		Filter filter = filterData.getFilter();
		if (filter.isShownMode())
			btnShow.setSelection(true);
		else
			btnHide.setSelection(true);
		
		Label lblMode = new Label(grpMode, SWT.LEFT | SWT.WRAP);
		lblMode.setText("Selecting 'To show' radio button will show matching processes, " +
						 "while selecting 'To hide'button will hide them.");
		
		GridDataFactory.swtDefaults().span(2, 1).grab(true, false).applyTo(lblMode);
		
		GridDataFactory.swtDefaults().grab(true, false).applyTo(grpMode);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpMode);

		Group grpFilter = new Group(composite, SWT.NONE);
		grpFilter.setText("Filter");

		GridDataFactory.fillDefaults().grab(true, true).applyTo(grpFilter);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpFilter);

		Composite coButtons = new Composite(grpFilter, SWT.NONE);

		RowLayout rl = new RowLayout();
		rl.pack = true;
		rl.center = true;
		rl.type = SWT.VERTICAL;
		
		coButtons.setLayout(rl);
		
		Button btnAdd = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnAdd.setText("Add");
		btnAdd.setToolTipText("Add a new glob pattern");
		btnAdd.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				InputDialog dlg = new InputDialog(getShell(), "Add a pattern", 
						"Please type a glob pattern\n" + 
						"Symbol * matches all characters, while symbol ? matches only one character.\n" +
						"For instance, *.0 will match 12.0 and 13.0", "", null);
				if (dlg.open() == Dialog.OK) {
					list.add(dlg.getValue());
					checkButtons();
				}
			}
		});
		btnAdd.setLayoutData(new RowData(80,20));
		
		btnRemove = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnRemove.setText("remove");
		btnRemove.setToolTipText("Remove a selected glob pattern");
		btnRemove.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = list.getSelectionCount();
				if (i > 0) {
					final String item = list.getSelection()[0];
					list.remove(item);
					checkButtons();
				}
			}
		});
		btnRemove.setLayoutData(new RowData(80,20));
		
		final Button btnRemoveAll = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnRemoveAll.setText("Remove all");
		btnRemoveAll.setToolTipText("Remove all glob patterns");
		btnRemoveAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = list.getItemCount();
				if (count>0) {
					if (MessageDialog.openQuestion(getShell(), "Remove all patterns",
							"Are you sure to remove all " + count + " patterns ?")) {
						list.removeAll();
						checkButtons();
					}
				}
			}
		}) ;
		btnRemoveAll.setLayoutData(new RowData(80,20));
		
		list = new List(grpFilter, SWT.SINGLE | SWT.V_SCROLL);
		list.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				checkButtons();
			}			
		});
		GridDataFactory.fillDefaults().grab(true, true).hint(40, 80).applyTo(list);
		
		this.setMessage("Add/remove glob patterns to filter displayed processes");
		this.setTitle("Filter patterns");
		
		// add pattern into the list
		if (filterData != null && filter.getPatterns() != null) {
			for (String str : filter.getPatterns()) {
				list.add(str);
			}
		}

		checkButtons();
		
		return parent;
	}
	
	
	private void checkButtons() {
		boolean selected = (list.getSelectionCount()>0);
		btnRemove.setEnabled(selected);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle | SWT.RESIZE);
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
		Filter filter = filterData.getFilter();
		
		// put the glob pattern back
		filter.setPatterns(filterList);
		// set the show mode (to show or to hide)
		filter.setShowMode( btnShow.getSelection() );
		
		// check if the filter is correct
		filterData.setFilter(filter);
		if (filterData.getNumberOfRanks()>0)
			super.okPressed();
		else {
			// it is not allowed to filter everything
			MessageDialog.openError(getShell(), "Error", "The result of filter is empty ranks.\nIt isn't allowed to filter all the ranks.");
		}
	}	
}
