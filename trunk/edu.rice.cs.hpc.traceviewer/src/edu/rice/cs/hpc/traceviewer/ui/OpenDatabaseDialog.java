package edu.rice.cs.hpc.traceviewer.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
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
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


public class OpenDatabaseDialog extends Dialog {

	Combo[] comboBoxes = new Combo[3];
	//This is the most convenient and flexible way to pass around the data.
	//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
	public String[]  args = new String[3];
	
	private static final String SERVER_NAME_KEY = "server_name", SERVER_PORT_KEY = "server_port", SERVER_PATH_KEY = "server_path";
	private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath;
	

	protected OpenDatabaseDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	protected Control createDialogArea(Composite parent) {
		Composite outerComposite = (Composite) super.createDialogArea(parent);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(outerComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(outerComposite);
		
		final TabFolder tabFolder = new TabFolder(outerComposite, SWT.TOP);
		Rectangle r = outerComposite.getClientArea();
		tabFolder.setLocation(r.x, r.y);

		// ----------------------------------------------------- local database
		TabItem tabLocalItem = new TabItem(tabFolder, SWT.NULL);
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
				String directory;
		
				directory = dialog.open();

				if (directory == null)
						// user click cancel
						return;
				lblBrowse.setText("File: " + directory);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		localComposite.pack();
		
		tabLocalItem.setControl(localComposite);
		
		// ----------------------------------------------------- remote database
		TabItem tabRemoteItem = new TabItem(tabFolder, SWT.NULL);
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

		remoteComposite.pack();
		
		tabRemoteItem.setControl(remoteComposite);
		
		outerComposite.pack();
		
		return outerComposite;
	}
	@Override
	protected void okPressed() {
		args[0] = comboBoxes[0].getText();

		try{
			Integer portVal = (Integer.parseInt(comboBoxes[1].getText()));
			args[1] = portVal.toString();
		}
		catch (NumberFormatException r)
		{
			comboBoxes[1].setText("");
			return;
		}
		args[2] = comboBoxes[2].getText();
		objHistoryName.addLine(args[0]);
		objHistoryPort.addLine(args[1]);
		objHistoryPath.addLine(args[2]);
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


	static public void main(String []argv) 
	{
		OpenDatabaseDialog dlg = new OpenDatabaseDialog(new Shell());
		dlg.open();
	}
}
