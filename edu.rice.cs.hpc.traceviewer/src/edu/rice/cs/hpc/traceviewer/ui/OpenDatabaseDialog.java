package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import edu.rice.cs.hpc.common.util.UserInputHistory;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.DatabaseAccessInfo;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
import edu.rice.cs.hpc.traceviewer.db.local.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.remote.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.framework.Activator;

/*******************************************************
 * 
 * Generic dialog window to open database both locally and remotely
 * 
 * @author Brett Gutstein, Philip Taffet (original)
 * @author Laksono (maintainer)
 *******************************************************/
public class OpenDatabaseDialog extends Dialog 
{
	// ----------------------------------------------------------------
	// constants
	// ----------------------------------------------------------------

	private static final String SERVER_NAME_KEY = "server_name";
	private static final String SERVER_PORT_KEY = "server_port";
	private static final String SERVER_PATH_KEY = "server_path";

	private static final String SERVER_LOGIN = "server_login";
	private static final String SERVER_TUNNEL = "server_tunnel";

	private static final String DATABASE_PATH = "database_path";

	private static final String PORT_KEY_DEFAULT = "21590";
	private static final String HISTORY_SELECTION = "traceviewer.data.select";

	private static final int NUM_COMBO_FIELDS = 5;
	
	private final Combo[] comboBoxes = new Combo[NUM_COMBO_FIELDS];

	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	final static int FieldServerName = 0, FieldPortKey = 1, FieldPathKey = 2, FieldServerLogin = 4;
	public final static int FieldDatabasePath = 3;

	// ----------------------------------------------------------------
	// variables
	// ----------------------------------------------------------------

	//This is the most convenient and flexible way to pass around the data.
	private String[]  args  = null;
	
	private final IStatusLineManager status;
	private Button okButton;
	private String errorMessage;//empty string means no error
	private Button checkboxTunneling;

	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath,
	objHistoryDb;
	private UserInputHistory objHistoryLoginHost, objHistoryTunnel;

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
		setErrorMessage(_errorMessage);
	}
	
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	

	/**
	 * This method is used to pass data from the OpenDatabaseDialog box to 
	 * {@link TraceDatabase#openDatabase(org.eclipse.ui.IWorkbenchWindow, String[], org.eclipse.jface.action.IStatusLineManager, AbstractDBOpener)}.
	 * It must be called on an OpenDatabaseDialog that has already been constructed AFTER calling {@link #open()}.
	 * The type of opener it returns depends on which tab the user had open when he clicked ok
	 * 
	 * @author Brett Gutstein
	 * @return a LocalDBOpener or RemoteDBOpener for use with TraceDatabase.openDatabase, null if user cancels 
	 */
	public AbstractDBOpener getDBOpener() {

		if (isLocalDatabase()) {
			return new LocalDBOpener(args[FieldDatabasePath]);
		} else {
			final DatabaseAccessInfo info = new DatabaseAccessInfo();

			info.databasePath = args[FieldPathKey];
			info.serverName 		= args[FieldServerName];
			info.serverPort			= args[FieldPortKey];
			info.sshTunnelHostname	= getLoginHost();
			info.sshTunnelUsername	= getLoginUser();

			return (isLocalDatabase() ? new LocalDBOpener(args[FieldDatabasePath]) : new RemoteDBOpener(info));
		}
	}
	

	/******
	 * Return the information to access the database (if the user clicks Ok).
	 * This information will be needed by {@link TraceDatabasee} to start loading the database
	 * whether it's local or remote
	 * 
	 * @return {@link DatabaseAccessInfo} the information to access the database if okay button is clicked,
	 * 			null otherwise
	 */
	public DatabaseAccessInfo getDatabaseAccessInfo()
	{
		// no info if the user click cancel button
		if (args == null)
			return null;
		
		DatabaseAccessInfo info = new DatabaseAccessInfo();
		
		if (isLocalDatabase())
		{
			info.databasePath = args[FieldDatabasePath];
		} else 
		{
			info.databasePath 		= args[FieldPathKey];
			info.serverName 		= args[FieldServerName];
			info.serverPort			= args[FieldPortKey];
			info.sshTunnelHostname	= getLoginHost();
			info.sshTunnelUsername	= getLoginUser();
		}
		
		return info;
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
				if (okButton != null)
					checkFields();
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
				checkFields();
			}
		};
		comboBoxes[FieldDatabasePath].addModifyListener(localModify);

		final Button btnBrowse = new Button(localComposite, SWT.PUSH);
		btnBrowse.setText("Browse");

		//browse button listener
		btnBrowse.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog;

				dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Please select a directory containing execution traces.");
				dialog.setText("Select Data Directory");

				final String database = dialog.open();

				if (database == null)
					// user click cancel
					return;
				comboBoxes[FieldDatabasePath].setText(database);

				// automatically close the dialog box
				OpenDatabaseDialog.this.okPressed();
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
		serverAddr.setText("Server's host:");

		final Combo name = new Combo(remoteComposite, SWT.SINGLE);		 
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
				checkFields();
			}
		};

		//-------- ssh tunneling

		final Group grpTunnel = new Group(remoteComposite, SWT.None);
		grpTunnel.setText("SSH Tunneling");
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(grpTunnel);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpTunnel);

		objHistoryTunnel  = new UserInputHistory(SERVER_TUNNEL, 1);
		
		checkboxTunneling = new Button(grpTunnel, SWT.CHECK);
		checkboxTunneling.setText("Use SSH tunnel (similar to ssh -L port:server_address:port login@hostname)");
		boolean check = false;

		String []tunnels = objHistoryTunnel.getHistory();
		if (tunnels != null && tunnels.length > 0) {
			check = tunnels[0].equals(Boolean.TRUE.toString());
		}
		checkboxTunneling.setSelection(check);
		
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(checkboxTunneling);
		
		final Label lblHost = new Label(grpTunnel, SWT.LEFT);
		lblHost.setText("login@hostname:");

		objHistoryLoginHost = new UserInputHistory(SERVER_LOGIN);
		final Combo login = new Combo(grpTunnel, SWT.SINGLE);
		setComboWithHistory(login, objHistoryLoginHost, "login@hostname", 400, 
				"Enter the login and the host for tunneling in format login@hostname");
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(login);
		login.setEnabled(check); 
		comboBoxes[FieldServerLogin] = login;

		checkboxTunneling.addSelectionListener( new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				login.setEnabled(checkboxTunneling.getSelection()); 
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
			
		});
		
		// -------------------------------------------
		// add listener to all three fields
		// -------------------------------------------
		name.addModifyListener(remoteModify);
		port.addModifyListener(remoteModify);
		path.addModifyListener(remoteModify);

		remoteComposite.pack();

		tabRemoteItem.setControl(remoteComposite);

		//add error message if one exists
		final Label lblError = new Label(outerComposite, SWT.CENTER | SWT.WRAP);
		lblError.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));

		if (errorMessage != null)
		{
			lblError.setText(errorMessage);
		}
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).applyTo(lblError);
		getShell().layout(true,true);

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

	/*********
	 * return the username for SSH tunneling
	 * 
	 * @return
	 */
	private String getLoginUser()
	{
		if (args[FieldServerLogin] != null)
		{
			final String field = args[FieldServerLogin]; 
			final String user = field.substring(0, field.indexOf('@'));
			return user;
		}
		return null;
	}

	/******
	 * return the hostname for SSH tunneling
	 * 
	 * @return
	 */
	private String getLoginHost()
	{
		if (args[FieldServerLogin] != null)
		{
			final String field = args[FieldServerLogin]; 
			final String host  = field.substring(field.indexOf('@')+1);
			return host;
		}
		return null;
	}


	private boolean isLocalDatabase()
	{
		return useLocalDatabase;
	}


	/*******
	 * Automatically fill the combo with the user's previous entries (based on history) 
	 * 
	 * @param combo
	 * @param history
	 * @param defaultValue
	 * @param limit
	 * @param tooltipText
	 */
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
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		args = new String[NUM_COMBO_FIELDS];
		useLocalDatabase = tabFolder.getSelectionIndex() == 0;

		if (useLocalDatabase)
		{
			args[FieldDatabasePath] = comboBoxes[FieldDatabasePath].getText() ;
			objHistoryDb.addLine(args[FieldDatabasePath]);
		} else
		{
			// ------------------------------------
			// testing the input's validity
			// ------------------------------------
			final String login = comboBoxes[FieldServerLogin].getText(); 
			if (!login.isEmpty())
			{
				if (login.indexOf( '@' ) < 0) {
					MessageDialog.openError(getShell(), "Invalid input", "The SSH tunnel login input is invalid");
					return;
				}
			}
			
			try{
				Integer portVal = (Integer.valueOf(comboBoxes[FieldPortKey].getText()));
				args[FieldPortKey] = portVal.toString().trim();
			}
			catch (NumberFormatException r) //if this exception is thrown the dialog box is not submitted and the user must re-enter a port with numbers only
			{
				comboBoxes[FieldPortKey].setText("");
				return;
			}

			args[FieldServerName]  = comboBoxes[FieldServerName].getText();
			args[FieldPathKey] 	   = comboBoxes[FieldPathKey].getText();
			
			if (checkboxTunneling.getSelection()) {
				args[FieldServerLogin] = comboBoxes[FieldServerLogin].getText();
				objHistoryLoginHost.addLine(args[FieldServerLogin]);
			} else {
				args[FieldServerLogin] = null;
			}

			// ------------------------------------
			// save the history to the user preferences
			// ------------------------------------
			objHistoryName.addLine(args[FieldServerName]);
			objHistoryPort.addLine(args[FieldPortKey]);
			objHistoryPath.addLine(args[FieldPathKey]);
			objHistoryTunnel.addLine(String.valueOf( checkboxTunneling.getSelection()) );
		}

		Activator activator = Activator.getDefault();
		if (activator != null) {
			ScopedPreferenceStore objPref = (ScopedPreferenceStore) activator.getPreferenceStore();
			objPref.setValue(HISTORY_SELECTION, tabFolder.getSelectionIndex());
		}

		super.okPressed();
	}

	/****
	 * check combo boxes fields in local and remote tab
	 * If the field is not empty, we enable OK button
	 */
	private void checkFields() 
	{
		// Prevent crash on Windows b/c of Eclipse's lazy initialization
		if (comboBoxes[FieldDatabasePath] == null)
			return;

		if (tabFolder.getSelectionIndex() == 0) {
			if (!comboBoxes[FieldDatabasePath].getText().equals("")) { 
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		} else {
			if (!(comboBoxes[0].getText().equals("")) && !(comboBoxes[1].getText().equals("")) && !(comboBoxes[2].getText().equals(""))) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
	}

	/**********
	 * test unit, called by JVM
	 * 
	 * @param args
	 */
	public static void main(String []args) 
	{
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.open();
		OpenDatabaseDialog dialog = new OpenDatabaseDialog(shell, null);

		if (dialog.open() == Dialog.OK) {
			System.out.println("ok");
			final String []arguments = dialog.args;
			for (String arg:arguments) 
			{
				System.out.println("-> " + arg);
			}
		}
	}
}
