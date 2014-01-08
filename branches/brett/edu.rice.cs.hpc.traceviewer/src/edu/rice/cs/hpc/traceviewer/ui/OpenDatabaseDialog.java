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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import edu.rice.cs.hpc.common.util.UserInputHistory;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
import edu.rice.cs.hpc.traceviewer.framework.Activator;

/*******************************************************
 * 
 * Generic dialog window to open database both locally and remotely
 *
 */
public class OpenDatabaseDialog extends Dialog {

	// ----------------------------------------------------------------
	// constants
	// ----------------------------------------------------------------
	
	private static final String SERVER_NAME_KEY = "server_name";
	private static final String SERVER_PORT_KEY = "server_port";
	private static final String SERVER_PATH_KEY = "server_path";
	
	private static final String DATABASE_PATH = "database_path";
	
	private static final String PORT_KEY_DEFAULT = "21590";
	private static final String HISTORY_SELECTION = "traceviewer.data.select";
	
	private final Combo[] comboBoxes = new Combo[4];

	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	final static int FieldServerName = 0, FieldPortKey = 1, FieldPathKey = 2;
	final static int FieldDatabasePath = 3;

	// ----------------------------------------------------------------
	// variables
	// ----------------------------------------------------------------

	//This is the most convenient and flexible way to pass around the data.
	private String[]  args = new String[4];

	private final IStatusLineManager status;
	private Button okButton;
	private String errorMessage;//empty string means no error
	
	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath,
							 objHistoryDb;
	private TabFolder tabFolder ;
	
	// the choice is either 
	private boolean useLocalDatabase = true;

	
	/*****
	 * constructor with the default error message
	 * 
	 * @param parentShell
	 * @param inStatus
	 */
	public OpenDatabaseDialog(Shell parentShell, final IStatusLineManager inStatus) { 
		super(parentShell);
		status=inStatus;
		errorMessage="";
	}
	
	/*****
	 * constructor with a customized error message
	 * 
	 * @param parentShell
	 * @param inStatus
	 * @param error message
	 */
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
		return (isLocalDatabase() ? new LocalDBOpener(args[FieldDatabasePath]) : new RemoteDBOpener(args));
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
		
		if (status != null)
			status.setMessage("Select a local or remote directory containing traces");
		
		Composite outerComposite = (Composite) super.createDialogArea(parent);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(outerComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(outerComposite);
		
		tabFolder = new TabFolder(outerComposite, SWT.TOP);
		
		//add listener to TabFolder to see when ok button can be pressed
		tabFolder.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}//we dont need to do anything when default selection is made

			@Override
			public void widgetSelected(SelectionEvent e) {
				int selected = tabFolder.getSelectionIndex();
				
				boolean bStatus = false;
				
				if (selected == 0) {
					bStatus = !comboBoxes[FieldPathKey].getText().equals("");
					
				} else if (selected == 1) {
					bStatus = (!(comboBoxes[FieldServerName].getText().equals("")) && 
							!(comboBoxes[FieldPortKey].getText().equals("")) && 
							!(comboBoxes[FieldPathKey].getText().equals("")));
				}
				okButton.setEnabled( bStatus );
			}
		});

		// ----------------------------------------------------- 
		// local database
		// ----------------------------------------------------- 
		TabItem tabLocalItem = new TabItem(tabFolder, SWT.NULL);
		tabLocalItem.setText("Local database");
		
		Composite localComposite = new Composite(tabLocalItem.getParent(), SWT.BORDER_SOLID);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(localComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(localComposite);
		
		final Label lblBrowse = new Label(localComposite, SWT.LEFT | SWT.WRAP);
		lblBrowse.setText("Database:");
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(lblBrowse);
		
		objHistoryDb = new UserInputHistory(DATABASE_PATH);
		comboBoxes[FieldDatabasePath] = new Combo(localComposite, SWT.DROP_DOWN | SWT.SINGLE);
		setComboWithHistory(comboBoxes[FieldDatabasePath], objHistoryDb, "", 400,
				"Enter the path to the folder containing the database");

		GridDataFactory.fillDefaults().grab(true, false).hint(400, 40).
			align(SWT.FILL, SWT.CENTER).applyTo(comboBoxes[FieldDatabasePath]);
		
		
		//directory combo listener to determine when OK can be pressed
		ModifyListener localModify = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				if (!comboBoxes[FieldDatabasePath].getText().equals("")) { 
					okButton.setEnabled(true);
				} else {
					okButton.setEnabled(false);
				}
				
			}
		};
		comboBoxes[FieldDatabasePath].addModifyListener(localModify);
		
			
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
		
				args[FieldDatabasePath] = dialog.open();

				if (args[FieldDatabasePath] == null)
						// user click cancel
						return;
				comboBoxes[FieldDatabasePath].setText(args[FieldDatabasePath]);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		localComposite.pack();
		
		tabLocalItem.setControl(localComposite);
		
		// ----------------------------------------------------- 
		// remote database
		// ----------------------------------------------------- 
		TabItem tabRemoteItem = new TabItem(tabFolder, SWT.NULL);
		tabRemoteItem.setText("Remote database");
		
		Composite remoteComposite = new Composite(tabRemoteItem.getParent(), SWT.NULL);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(remoteComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(remoteComposite);
		
		//-------- server name
		Label serverAddr = new Label(remoteComposite, SWT.LEAD);
		serverAddr.setText("Server's address:");

		Combo name = new Combo(remoteComposite, SWT.SINGLE);		 
		objHistoryName = new UserInputHistory( SERVER_NAME_KEY );
		setComboWithHistory(name, objHistoryName, "", 50,
				"Enter the domain name or IP address of the server to use");

		GridDataFactory.fillDefaults().grab(true, false).applyTo(name);

		comboBoxes[FieldServerName] = name;

		//-------- remote server port		
		Label serverPort = new Label(remoteComposite, SWT.LEFT);
		serverPort.setText("Port:");
		Combo port = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPort = new UserInputHistory( SERVER_PORT_KEY );

		setComboWithHistory(port, objHistoryPort, PORT_KEY_DEFAULT, 5, 
				"Enter the port to use");

		GridDataFactory.fillDefaults().grab(true, false).applyTo(port);

		comboBoxes[FieldPortKey] = port;

		//-------- remote database path

		Label serverPath = new Label(remoteComposite, SWT.LEAD);
		serverPath.setText("Path to database folder:");

		Combo path = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPath = new UserInputHistory( SERVER_PATH_KEY );
		setComboWithHistory(path, objHistoryPath, "", 400, 
				"Enter the path to the folder containing the database on the server");

		GridDataFactory.fillDefaults().grab(true, false).applyTo(path);

		comboBoxes[FieldPathKey] = path;
		
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
		
		// default selection ?
		Activator activator = Activator.getDefault();
		if (activator != null) {
			ScopedPreferenceStore objPref = (ScopedPreferenceStore) activator.getPreferenceStore();
			int select = objPref.getInt(HISTORY_SELECTION);
			tabFolder.setSelection(select);
		}
		
		return outerComposite;
	}
	
	
	public boolean isLocalDatabase()
	{
		return useLocalDatabase;
	}
	
	
	private void setComboWithHistory(Combo combo, UserInputHistory history, String defaultValue,
			int limit, String tooltipText)
	{
		final String []logs = history.getHistory();
		if (logs != null && logs.length > 0)
		{
			combo.setItems(logs);
			combo.select(0);
		}
		else 
		{
			combo.setText(defaultValue);
		}
		combo.setTextLimit(limit);
		combo.setToolTipText(tooltipText); 
	}
	
	
	@Override
	protected void okPressed() {
		
		useLocalDatabase = tabFolder.getSelectionIndex() == 0;

		if (useLocalDatabase)
		{
			args[FieldDatabasePath] = comboBoxes[FieldDatabasePath].getText() ;
			objHistoryDb.addLine(args[FieldDatabasePath]);
		} else
		{
			args[FieldServerName] = comboBoxes[FieldServerName].getText();

			try{
				Integer portVal = (Integer.valueOf(comboBoxes[FieldPortKey].getText()));
				args[FieldPortKey] = portVal.toString().trim();
			}
			catch (NumberFormatException r) //if this exception is thrown the dialog box is not submitted and the user must re-enter a port with numbers only
			{
				comboBoxes[FieldPortKey].setText("");
				return;
			}

			args[FieldPathKey] = comboBoxes[FieldPathKey].getText();
			
			objHistoryName.addLine(args[FieldServerName]);
			objHistoryPort.addLine(args[FieldPortKey]);
			objHistoryPath.addLine(args[FieldPathKey]);
		}
		
		Activator activator = Activator.getDefault();
		if (activator != null) {
			ScopedPreferenceStore objPref = (ScopedPreferenceStore) activator.getPreferenceStore();
			objPref.setValue(HISTORY_SELECTION, tabFolder.getSelectionIndex());
		}
		
		super.okPressed();
	}
	
	public static void main(String []args) 
	{
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		shell.open();
		OpenDatabaseDialog dialog = new OpenDatabaseDialog(shell, null);
		
		if (dialog.open() == Dialog.OK) {
			System.out.println("ok");
		}
	}
}
