package edu.rice.cs.hpc.traceviewer.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
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
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.LocalDBOpener;
import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;


public class OpenDatabaseDialog extends Dialog {
	
	static int defaultTab=0; //index of the tab to open by default. changes when user clicks ok

	Combo[] comboBoxes = new Combo[3];
	//This is the most convenient and flexible way to pass around the data.
	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	private String[]  args = new String[3];
	private String directory; //used to pass directory to LocalDBOpener
	private TabFolder tf; //used to determine which opener to pass to - see okPressed
	private boolean passToLocal; //used to determine which opener to pass to - see okPressed
	private boolean okClicked; //has the ok button been clicked
	private final IStatusLineManager status;
	private Button okButton;
	private String errorMessage;//empty string means no error
	
	private static final String SERVER_NAME_KEY = "server_name", SERVER_PORT_KEY = "server_port", SERVER_PATH_KEY = "server_path";
	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath;
	

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
	
		if (okClicked) {
			if (passToLocal) {
				return new LocalDBOpener(directory);
			} else {
				return new RemoteDBOpener(args);
			}
		} else {
			return null;
		}
	}
	
	//overridden to get ID of the ok button
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		super.createButtonsForButtonBar(parent);
		okButton=getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);

	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Open a Database");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		status.setMessage("Select a local or remote directory containing traces");
		
		Composite outerComposite = (Composite) super.createDialogArea(parent);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(outerComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(outerComposite);
		
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
					if (directory!=null) {
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
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(localComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(localComposite);
		
		final Label lblBrowse = new Label(localComposite, SWT.LEFT | SWT.WRAP);
		lblBrowse.setText("File:");
		GridDataFactory.fillDefaults().grab(true, false).hint(400, 20).align(SWT.BEGINNING, SWT.CENTER).applyTo(lblBrowse);
		
		Button btnBrowse = new Button(localComposite, SWT.PUSH);
		btnBrowse.setText("Browse");
		

		
		btnBrowse.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog;

				dialog = new DirectoryDialog(getShell());
				dialog.setMessage("Please select a directory containing execution traces.");
				dialog.setText("Select Data Directory");
		
				directory = dialog.open();
				
				//user clicks cancel
				if (directory == null) { 
					okButton.setEnabled(false);
					return;
				}
				
				okButton.setEnabled(true); //user chooses a directory therefore ok is okay to press
				
				lblBrowse.setText("File: " + directory);
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
		GridDataFactory.fillDefaults().grab(true, true).applyTo(remoteComposite);
		
		Label serverAddr = new Label(remoteComposite, SWT.LEAD);
		serverAddr.setText("Server's address:");

		Combo name = new Combo(remoteComposite, SWT.SINGLE);		 
		objHistoryName = new UserInputHistory( SERVER_NAME_KEY );
		name.setItems(objHistoryName.getHistory());
		name.setTextLimit(50);
		name.setToolTipText("Enter the domain name or IP address of the server to use");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(name);

		comboBoxes[0] = name;

		Label serverPort = new Label(remoteComposite, SWT.LEFT);
		serverPort.setText("Port:");
		Combo port = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPort = new UserInputHistory( SERVER_PORT_KEY );
		port.setItems(objHistoryPort.getHistory());
		port.setText("21590");
		port.setTextLimit(5);
		port.setToolTipText("Enter the port to use"); 

		comboBoxes[1] = port;

		Label serverPath = new Label(remoteComposite, SWT.LEAD);
		serverPath.setText("Path to database folder:");

		Combo path = new Combo(remoteComposite, SWT.SINGLE);  
		objHistoryPath = new UserInputHistory( SERVER_PATH_KEY );
		path.setItems(objHistoryPath.getHistory());
		path.setText("");
		path.setTextLimit(400);
		path.setToolTipText("Enter the path to the folder containing the database on the server"); 
		GridDataFactory.fillDefaults().grab(true, false).applyTo(path);

		comboBoxes[2] = path;
		
		//listener used for all three text fields to determine when ok can be pressed
		ModifyListener mL = new ModifyListener() {
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
		name.addModifyListener(mL);
		port.addModifyListener(mL);
		path.addModifyListener(mL);
		
		remoteComposite.pack();
		
		tabRemoteItem.setControl(remoteComposite);
		
		//add error message if one exists
		if (!(errorMessage.equals(""))) {
			final Label lblError = new Label(outerComposite, SWT.CENTER | SWT.WRAP);
			lblError.setText(errorMessage);
			GridDataFactory.fillDefaults().grab(true, false).hint(400, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).applyTo(lblError);
		}
		
		outerComposite.pack();
		
		tf.setSelection(defaultTab);
		
		return outerComposite;
	}
	@Override
	protected void okPressed() {
		args[0] = comboBoxes[0].getText();

		try{
			Integer portVal = (Integer.parseInt(comboBoxes[1].getText()));
			args[1] = portVal.toString();
		}
		catch (NumberFormatException r) //if this exception is thrown, clear port and highlight it so user enters a number
		{
			comboBoxes[1].setText("");
			return;
		}
		args[2] = comboBoxes[2].getText();
		objHistoryName.addLine(args[0]);
		objHistoryPort.addLine(args[1]);
		objHistoryPath.addLine(args[2]);
		
		int selection = tf.getSelectionIndex(); //returns the zero-relative index of the TabItem which is selected in the TabFolder (0 is local 1 is remote)
		defaultTab = selection; //same tab will be opened next time
		if (selection==0) {
			passToLocal=true;
		} else if (selection==1) { //leave null if selectionindex somehow returns not 0 or 1
			passToLocal=false;
		}
		
		okClicked=true;
		
		super.okPressed();
	}

	private static class UserInputHistory {
		private static final String HISTORY_NAME_BASE = "history."; //$NON-NLS-1$
		private final static String ENCODING = "UTF-8";
	    
		// temporary revert back to use deprecated code in order to keep backward compatibility
		// Original code:
		//     private static final Preferences CONFIGURATION = ConfigurationScope.INSTANCE.getNode("edu.rice.cs.hpc");   
		private static final Preferences CONFIGURATION =  ConfigurationScope.INSTANCE.getNode("edu.rice.cs.hpc");
	    
	    private String name;
	    private int depth;
	    private List<String> history;


	    public UserInputHistory(String name) {
	        this(name, 50);
	    }

	    public UserInputHistory(String name, int depth) {
	        this.name = name;
	        this.depth = depth;
	        
	        this.loadHistoryLines();
	    }
	    
	   /* public String getName() {
	        return this.name;
	    }
	    
	    public int getDepth() {
	        return this.depth;
	    }*/
	    
	    public String []getHistory() {
	        return this.history.toArray(new String[this.history.size()]);
	    }
	    
	    public void addLine(String line) {
	        if (line == null || line.trim().length() == 0) {
	            return;
	        }
	    	this.history.remove(line);
	        this.history.add(0, line);
	        if (this.history.size() > this.depth) {
	            this.history.remove(this.history.size() - 1);
	        }
	        this.saveHistoryLines();
	    }
	    
	   /* public void clear() {
	        this.history.clear();
	        this.saveHistoryLines();
	    }*/

	    /****
	     * retrieve the preference of this application
	     * @param node
	     * @return
	     */
	    static public Preferences getPreference(String node) {
	    	return CONFIGURATION.node(node);
	    }
	    
	    /****
	     * force to store a preference
	     * @param pref
	     */
	    static public void setPreference( Preferences pref ) {
			// Forces the application to save the preferences
			try {
				pref.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}

	    }
	    
	    protected void loadHistoryLines() {
	        this.history = new ArrayList<String>();
	        String historyData = getPreference(HISTORY_NAME_BASE).get(this.name, ""); 

	        if (historyData != null && historyData.length() > 0) {
	            String []historyArray = historyData.split(";"); //$NON-NLS-1$
	            for (int i = 0; i < historyArray.length; i++) {
	            	try {
	            		historyArray[i] = new String(historyArray[i].getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
	                } catch (UnsupportedEncodingException e) {
	                	historyArray[i] = new String(historyArray[i].getBytes());
	                }
	            }
	            this.history.addAll(Arrays.asList(historyArray));
	        }
	    }
	    
	    protected void saveHistoryLines() {
	        String result = ""; //$NON-NLS-1$
	        for (Iterator<String> it = this.history.iterator(); it.hasNext(); ) {
	            String str = it.next();
	            try {
					str = new String(str.getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
				} catch (UnsupportedEncodingException e) {
					str = new String(str.getBytes());
				}
	            result += result.length() == 0 ? str : (";" + str); //$NON-NLS-1$
	        }
	        Preferences pref = getPreference(HISTORY_NAME_BASE);
	        pref.put(this.name, result);
	        setPreference( pref );
	    }
	}

}
