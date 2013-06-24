package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import edu.rice.cs.hpc.common.util.UserInputHistory;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenDatabaseDialog extends Dialog {
	
	static int defaultTab=0; //index of the tab to open by default. changes when user clicks ok

	private Combo directoryCombo;
	private final Combo[] comboBoxes = new Combo[3];
	//This is the most convenient and flexible way to pass around the data.
	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	private String[]  args = new String[3];
	private String directory; //used to pass directory to LocalDBOpener
	private TabFolder tf; //used to determine which opener to pass to - see okPressed
	private boolean passToLocal; //used to determine which opener to pass to - see okPressed
	private final IStatusLineManager status;
	private Button okButton;
	private String errorMessage;//empty string means no error
	
	private static final String SERVER_NAME_KEY = "server_name", SERVER_PORT_KEY = "server_port", SERVER_PATH_KEY = "server_path", LOCAL_DIRECTORY_KEY = "local_directory";
	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath, objHistoryDirectory;
	

	public OpenDatabaseDialog(Shell parentShell, final IStatusLineManager inStatus) { 
		super(parentShell);
		status=inStatus;
		errorMessage="";
	}
	
	public OpenDatabaseDialog(Shell parentShell, final IStatusLineManager inStatus, String _errorMessage){
		super(parentShell);
		status=inStatus;
		errorMessage = _errorMessage;
	}
	
	/**
	 * This method is used to pass data from the OpenDatabaseDialog box to 
	 * {@link TraceDatabase#openDatabase(org.eclipse.ui.IWorkbenchWindow, String[], org.eclipse.jface.action.IStatusLineManager, AbstractDBOpener)}.
	 * It must be called on an OpenDatabaseDialog that has already been constructed AFTER calling {@link #open()}.
	 * The type of opener it returns depends on which tab the user had open when he clicked ok
	 * @author Brett Gutstein
	 * @return a LocalDBOpener or RemoteDBOpener for use with TraceDatabase.openDatabase, null if user cancels 
	 */
	public AbstractDBOpener getDBOpener() {
		return (passToLocal ? new LocalDBOpener(directory) : new RemoteDBOpener(args));
	}
	
	//overridden to get ID of the ok button
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		super.createButtonsForButtonBar(parent);
		okButton=getButton(IDialogConstants.OK_ID);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		getShell().setText("Open a Database");
		
		status.setMessage("Select a local or remote directory containing traces");
		
		Composite outerComposite = (Composite) super.createDialogArea(parent);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(outerComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(outerComposite);
		
		tf = new TabFolder(outerComposite, SWT.TOP);
		Rectangle r = outerComposite.getClientArea();
		tf.setLocation(r.x, r.y);
		
		//add listener to TabFolder to see when ok button can be pressed
		tf.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}//we dont need to do anything when default selection is made

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selected = tf.getSelectionIndex();
				if (selected == 0) {
					if (!directoryCombo.getText().equals("")) {
						okButton.setEnabled(true);
					} else {
						okButton.setEnabled(false);
					}
				} else if (selected == 1) {
					if (!(comboBoxes[0].getText().equals("")) && !(comboBoxes[1].getText().equals("")) && !(comboBoxes[2].getText().equals(""))) {
						okButton.setEnabled(true);
					} else {
						okButton.setEnabled(false);
					}
				}
				
			}
		});

		// ----------------------------------------------------- local database
		TabItem tabLocalItem = new TabItem(tf, SWT.NULL);
		tabLocalItem.setText("Local database");
		
		Composite localComposite = new Composite(tabLocalItem.getParent(), SWT.BORDER_SOLID);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(localComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(localComposite);
		
		final Label lblBrowse = new Label(localComposite, SWT.LEFT | SWT.WRAP);
		lblBrowse.setText("Directory:");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(lblBrowse);
		
		
		directoryCombo = new Combo(localComposite, SWT.SINGLE);
		objHistoryDirectory = new UserInputHistory( LOCAL_DIRECTORY_KEY );
		directoryCombo.setItems(objHistoryDirectory.getHistory());
		directoryCombo.setText( (objHistoryDirectory.getHistory().length > 0) ? objHistoryDirectory.getHistory()[0] : "" ); //autofill
		directoryCombo.setToolTipText("Enter the directory containing execution traces.");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(directoryCombo);
		
		
		//directory combo listener to determine when OK can be pressed
		ModifyListener localModify = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				if (!directoryCombo.getText().equals("")) { 
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
				
			}
		};
		directoryCombo.addModifyListener(localModify);
		
			
		Button btnBrowse = new Button(localComposite, SWT.PUSH);
		btnBrowse.setText("Browse");
			
		//browse button listener
		btnBrowse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog;

				dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Please select a directory containing execution traces.");
				dialog.setText("Select Data Directory");
		
				String selectedDirectory  = dialog.open(); 
				
				//user clicked cancel - do not change combo box
				if (selectedDirectory == null) { 
					return;
				}
				
				directoryCombo.setText(selectedDirectory); //otherwise change combo box to selected directory
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		localComposite.pack();
		
		tabLocalItem.setControl(localComposite);
		
		// ----------------------------------------------------- remote database
		TabItem tabRemoteItem = new TabItem(tf, SWT.NULL);
		tabRemoteItem.setText("Remote database");
		
		Composite remoteComposite = new Composite(tabRemoteItem.getParent(), SWT.NULL);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(remoteComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(remoteComposite);
		
		Label serverAddr = new Label(remoteComposite, SWT.LEAD);
		serverAddr.setText("Server's address:");

		Combo name = new Combo(remoteComposite, SWT.SINGLE);		 
		objHistoryName = new UserInputHistory( SERVER_NAME_KEY );
		name.setItems(objHistoryName.getHistory());
		name.setText( (objHistoryName.getHistory().length > 0) ? objHistoryName.getHistory()[0] : "" ); //autofill
		name.setTextLimit(50);
		name.setToolTipText("Enter the domain name or IP address of the server to use");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(name);

		comboBoxes[0] = name;

		Label serverPort = new Label(remoteComposite, SWT.LEFT);
		serverPort.setText("Port:");
		Combo port = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPort = new UserInputHistory( SERVER_PORT_KEY );
		port.setItems(objHistoryPort.getHistory());
		port.setText( (objHistoryPort.getHistory().length > 0) ? objHistoryPort.getHistory()[0] : "21590" ); //autofill
		port.setTextLimit(5);
		port.setToolTipText("Enter the port to use");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(port);

		comboBoxes[1] = port;

		Label serverPath = new Label(remoteComposite, SWT.LEAD);
		serverPath.setText("Path to database folder:");

		Combo path = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPath = new UserInputHistory( SERVER_PATH_KEY );
		path.setItems(objHistoryPath.getHistory());
		path.setText( (objHistoryPath.getHistory().length > 0) ? objHistoryPath.getHistory()[0] : "" ); //autofill
		path.setTextLimit(400);
		path.setToolTipText("Enter the path to the folder containing the database on the server"); 
		GridDataFactory.fillDefaults().grab(true, false).applyTo(path);

		comboBoxes[2] = path;
		
		//listener used for all three text fields to determine when ok can be pressed
		ModifyListener remoteModify = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!(comboBoxes[0].getText().equals("")) && !(comboBoxes[1].getText().equals("")) && !(comboBoxes[2].getText().equals(""))) {
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
			}
		};
		//add listener to all three fields
		name.addModifyListener(remoteModify);
		port.addModifyListener(remoteModify);
		path.addModifyListener(remoteModify);
		
		remoteComposite.pack();
		
		tabRemoteItem.setControl(remoteComposite);
		
		//add error message if one exists
		if (!(errorMessage.equals(""))) {
			final Label lblError = new Label(outerComposite, SWT.CENTER | SWT.WRAP);
			lblError.setText(errorMessage);
			GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).applyTo(lblError);
			getShell().layout(true,true);
		}
		
		outerComposite.pack();
		
		tf.setSelection(defaultTab);
		
		return outerComposite;
	}
	@Override
	protected void okPressed() {
		args[0] = comboBoxes[0].getText().trim();

		try{
			Integer portVal = (Integer.parseInt(comboBoxes[1].getText()));
			args[1] = portVal.toString().trim();
		}
		catch (NumberFormatException r) //if this exception is thrown the dialog box is not submitted and the user must re-enter a port with numbers only
		{
			comboBoxes[1].setText("");
			return;
		}
		args[2] = comboBoxes[2].getText().trim();
		objHistoryName.addLine(args[0]);
		objHistoryPort.addLine(args[1]);
		objHistoryPath.addLine(args[2]);
		
		directory=directoryCombo.getText().trim();
		
		objHistoryDirectory.addLine(directory);
		
		int selection = tf.getSelectionIndex(); //returns the zero-relative index of the TabItem which is selected in the TabFolder (0 is local 1 is remote)
		defaultTab = selection; //same tab will be opened next time
		passToLocal = (selection==0);
		
		
		super.okPressed();
	}
}
