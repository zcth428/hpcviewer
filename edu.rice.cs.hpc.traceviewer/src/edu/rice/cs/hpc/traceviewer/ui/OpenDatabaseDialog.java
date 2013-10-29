package edu.rice.cs.hpc.traceviewer.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
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


public class OpenDatabaseDialog extends Dialog {

	final static int FieldServerName = 0, FieldPortKey = 1, FieldPathKey = 2;
	final static int FieldDatabasePath = 3;
	
	final Combo[] comboBoxes = new Combo[4];
	
	//This is the most convenient and flexible way to pass around the data.
	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	public String[]  args = new String[4];
	
	private static final String SERVER_NAME_KEY = "server_name";
	private static final String SERVER_PORT_KEY = "server_port";
	private static final String SERVER_PATH_KEY = "server_path";
	
	private static final String DATABASE_PATH = "database_path";
	
	private static final String PORT_KEY_DEFAULT = "21590";

	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath, objHistoryDb;
	private TabFolder tabFolder ;
	
	// the choice is either 
	private boolean useLocalDatabase;

	protected OpenDatabaseDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	protected Control createDialogArea(Composite parent) {
		Composite outerComposite = (Composite) super.createDialogArea(parent);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(outerComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(outerComposite);
		
		tabFolder = new TabFolder(outerComposite, SWT.TOP);
		Rectangle r = outerComposite.getClientArea();
		tabFolder.setLocation(r.x, r.y);

		// ----------------------------------------------------- 
		// local database
		// ----------------------------------------------------- 
		TabItem tabLocalItem = new TabItem(tabFolder, SWT.NULL);
		tabLocalItem.setText("Local database");
		
		Composite localComposite = new Composite(tabLocalItem.getParent(), SWT.BORDER_SOLID);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(localComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(localComposite);
		
		final Label lblBrowse = new Label(localComposite, SWT.LEFT | SWT.WRAP);
		lblBrowse.setText("Database:");
		
		objHistoryDb = new UserInputHistory(DATABASE_PATH);
		comboBoxes[FieldDatabasePath] = new Combo(localComposite, SWT.DROP_DOWN | SWT.SINGLE);
		setComboWithHistory(comboBoxes[FieldDatabasePath], objHistoryDb, "");

		GridDataFactory.fillDefaults().grab(true, false).hint(400, 20).
			align(SWT.END, SWT.CENTER).applyTo(comboBoxes[FieldDatabasePath]);
		
		Button btnBrowse = new Button(localComposite, SWT.PUSH);
		btnBrowse.setText("Browse");
		
		btnBrowse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog;

				dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Please select a directory containing execution traces.");
				dialog.setText("Select Data Directory");
				String directory;
		
				directory = dialog.open();

				if (directory == null)
						// user click cancel
						return;
				comboBoxes[FieldDatabasePath].setText(directory);
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
		GridDataFactory.fillDefaults().grab(true, true).applyTo(remoteComposite);
		
		//-------- server name
		Label serverAddr = new Label(remoteComposite, SWT.LEAD);
		serverAddr.setText("Server's address:");

		Combo name = new Combo(remoteComposite, SWT.SINGLE);		 
		objHistoryName = new UserInputHistory( SERVER_NAME_KEY );
		setComboWithHistory(name, objHistoryName, "");

		name.setTextLimit(50);
		name.setToolTipText("Enter the domain name or IP address of the server to use");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(name);

		comboBoxes[FieldServerName] = name;

		//-------- remote server port		
		Label serverPort = new Label(remoteComposite, SWT.LEFT);
		serverPort.setText("Port:");
		Combo port = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPort = new UserInputHistory( SERVER_PORT_KEY );
		setComboWithHistory(port, objHistoryPort, PORT_KEY_DEFAULT);

		port.setTextLimit(5);
		port.setToolTipText("Enter the port to use"); 

		comboBoxes[FieldPortKey] = port;

		Label serverPath = new Label(remoteComposite, SWT.LEAD);
		serverPath.setText("Path to database folder:");

		//-------- remote database path
		Combo path = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPath = new UserInputHistory( SERVER_PATH_KEY );
		setComboWithHistory(path, objHistoryPath, "");

		path.setTextLimit(400);
		path.setToolTipText("Enter the path to the folder containing the database on the server"); 
		GridDataFactory.fillDefaults().grab(true, false).applyTo(path);

		comboBoxes[FieldPathKey] = path;

		remoteComposite.pack();
		
		tabRemoteItem.setControl(remoteComposite);
		
		outerComposite.pack();
		
		return outerComposite;
	}
	
	
	public boolean isLocalDatabase()
	{
		return useLocalDatabase;
	}
	
	public String getLocalDatabasePath()
	{
		return args[FieldDatabasePath];
	}
	
	public String getServerName()
	{
		return args[FieldServerName];
	}
	
	public String getPortKey()
	{
		return args[FieldPortKey];
	}
	
	public String getPathKey()
	{
		return args[FieldPathKey];
	}
	
	
	private void setComboWithHistory(Combo combo, UserInputHistory history, String defaultValue)
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
	}
	
	
	@Override
	protected void okPressed() {
		
		useLocalDatabase = tabFolder.getSelectionIndex() == 0;
		args[FieldDatabasePath] = comboBoxes[FieldDatabasePath].getText() ;

		args[FieldServerName] = comboBoxes[FieldServerName].getText();

		try{
			Integer portVal = (Integer.valueOf(comboBoxes[FieldPortKey].getText()));
			args[1] = portVal.toString();
		}
		catch (NumberFormatException r)
		{
			comboBoxes[FieldPortKey].setText("");
			return;
		}
		args[FieldDatabasePath] = comboBoxes[FieldDatabasePath].getText();
		
		if (useLocalDatabase)
		{
			objHistoryDb.addLine(args[FieldDatabasePath]);
		} else
		{
			objHistoryName.addLine(args[FieldServerName]);
			objHistoryPort.addLine(args[FieldPortKey]);
			objHistoryPath.addLine(args[FieldDatabasePath]);
		}
		
		super.okPressed();
	}



	static public void main(String []argv) 
	{
		OpenDatabaseDialog dlg = new OpenDatabaseDialog(new Shell());
		dlg.open();
	}
}
