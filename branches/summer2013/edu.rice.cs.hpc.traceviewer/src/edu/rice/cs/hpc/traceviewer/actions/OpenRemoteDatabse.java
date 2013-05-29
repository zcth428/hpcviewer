package edu.rice.cs.hpc.traceviewer.actions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenRemoteDatabse extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//InputDialog inputDlg = new InputDialog(HandlerUtil.getActiveShell(event), "Remote server information", "Please enter the information to connect to a database on a remote server", null, null);
		//inputDlg.open();
		RemoteServerInfoDialogBox inputDlg = new RemoteServerInfoDialogBox(HandlerUtil.getActiveShell(event));
		int buttonClicked = inputDlg.open();
		if (buttonClicked == Dialog.OK)
		{
			final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);
			RemoteDBOpener dbopener = new RemoteDBOpener();

			TraceDatabase.openDatabase(window, inputDlg.args, vSite.getActionBars().getStatusLineManager(), dbopener);
		}
		return null;
	}
	private class RemoteServerInfoDialogBox extends Dialog
	{
		Combo[] comboBoxes = new Combo[3];
		//This is the most convenient and flexible way to pass around the data.
		//Index 0 = Server's name/address; Index 1 = Port; Index 2 = Path to database folder on server
		public String[]  args = new String[3];
		
		private static final String SERVER_NAME_KEY = "server_name", SERVER_PORT_KEY = "server_port", SERVER_PATH_KEY = "server_path";
		private UserInputHistory objHistoryName, objHistoryPort, objHistoryPath;
		
		protected RemoteServerInfoDialogBox(Shell parentShell) {
			super(parentShell);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
						 Composite composite = (Composite) super.createDialogArea(parent);
			 //http://www.ibm.com/developerworks/opensource/library/os-jface1/
			 
			 GridLayout gridLayout = new GridLayout(2, false);
			 composite.setLayout(gridLayout);
			 
			 Label serverAddr = new Label(composite, SWT.LEAD);
			 serverAddr.setText("Server's address:");
			 
			 Combo name = new Combo(composite, SWT.SINGLE);		 
			 objHistoryName = new UserInputHistory( SERVER_NAME_KEY );
			 name.setItems(objHistoryName.getHistory());
			 name.setTextLimit(50);
			 name.setToolTipText("Enter the domain name or IP address of the server to use");
			 GridData gdAddr = new GridData(150, 20);
			 name.setLayoutData(gdAddr);
			 comboBoxes[0] = name;
			 
			 
			 Label serverPort = new Label(composite, SWT.LEAD);
			 serverPort.setText("Port:");
			 Combo port = new Combo(composite, SWT.SINGLE);  
			 objHistoryPort = new UserInputHistory( SERVER_PORT_KEY );
			 port.setItems(objHistoryPort.getHistory());
			 port.setText("21590");
			 port.setTextLimit(5);
			 port.setToolTipText("Enter the port to use"); 
			 GridData gdPort = new GridData(75, 20);
			 port.setLayoutData(gdPort);
			 comboBoxes[1] = port;
			 
			 Label serverPath = new Label(composite, SWT.LEAD);
			 serverPath.setText("Path to database folder:");
			 
			 Combo path = new Combo(composite, SWT.SINGLE);  
			 objHistoryPath = new UserInputHistory( SERVER_PATH_KEY );
			 path.setItems(objHistoryPath.getHistory());
			 path.setText("");
			 path.setTextLimit(400);
			 path.setToolTipText("Enter the path to the folder containing the database on the server"); 
			 GridData gdPath = new GridData(220, 20);
			 path.setLayoutData(gdPath);
			 comboBoxes[2] = path;
			 
			 composite.pack();
			 
			 return composite;
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
		
	}

	private static class UserInputHistory {
		private static final String HISTORY_NAME_BASE = "history."; //$NON-NLS-1$
		private final static String ENCODING = "UTF-8";
	    
		// temporary revert back to use deprecated code in order to keep backward compatibility
		// Original code:
		//     private static final Preferences CONFIGURATION = ConfigurationScope.INSTANCE.getNode("edu.rice.cs.hpc");   
	    @SuppressWarnings("deprecation")
		private static final Preferences CONFIGURATION = new ConfigurationScope().getNode("edu.rice.cs.hpc");
	    
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
	        return (String [])this.history.toArray(new String[this.history.size()]);
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
	            String str = (String)it.next();
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
