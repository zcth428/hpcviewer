package edu.rice.cs.hpc.traceviewer.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.traceviewer.db.RemoteDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;

public class OpenRemoteDatabse extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//InputDialog inputDlg = new InputDialog(HandlerUtil.getActiveShell(event), "Remote server information", "Please enter the information to connect to a database on a remote server", null, null);
		//inputDlg.open();
		RemoteServerInfoDialogBox inputDlg = new RemoteServerInfoDialogBox(HandlerUtil.getActiveShell(event));
		inputDlg.open();
		
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IViewSite vSite = ( IViewSite ) HandlerUtil.getActiveSite(event);
		RemoteDBOpener dbopener = new RemoteDBOpener();
		
		TraceDatabase.openDatabase(window, inputDlg.args, vSite.getActionBars().getStatusLineManager(), dbopener);
		
		return null;
	}
	private class RemoteServerInfoDialogBox extends Dialog
	{
		Text[] textBoxes = new Text[3];
		
		public String[]  args = new String[3];
		
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
			 
			 Text name = new Text(composite, SWT.SINGLE);  
			 name.setText("");
			 name.setTextLimit(50);
			 name.setToolTipText("Enter the domain name or IP address of the server to use");
			 GridData gdAddr = new GridData(150, 20);
			 name.setLayoutData(gdAddr);
			 textBoxes[0] = name;
			 
			 
			 Label serverPort = new Label(composite, SWT.LEAD);
			 serverPort.setText("Port:");
			 
			 Text port = new Text(composite, SWT.SINGLE);  
			 port.setText("21590");
			 port.setTextLimit(5);
			 port.setToolTipText("Enter the port to use"); 
			 GridData gdPort = new GridData(50, 20);
			 port.setLayoutData(gdPort);
			 textBoxes[1] = port;
			 
			 Label serverPath = new Label(composite, SWT.LEAD);
			 serverPath.setText("Path to database folder:");
			 
			 Text path = new Text(composite, SWT.SINGLE);  
			 path.setText("");
			 path.setTextLimit(400);
			 path.setToolTipText("Enter the path to the folder containing the database on the server"); 
			 GridData gdPath = new GridData(220, 20);
			 path.setLayoutData(gdPath);
			 textBoxes[2] = path;
			 
			 composite.pack();
			 
			 return composite;
		}
		@Override
		protected void okPressed() {
			args[0] = textBoxes[0].getText();
			
			try{
			 Integer portVal = (Integer.parseInt(textBoxes[1].getText()));
			 args[1] = portVal.toString();
			}
			catch (NumberFormatException r)
			{
				textBoxes[1].setText("");
				return;
			}
			args[2] = textBoxes[2].getText();
			super.okPressed();
			
		}
		
	}

}
