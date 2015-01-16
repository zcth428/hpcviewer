package edu.rice.cs.hpc.remote.tunnel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.UserInfo;

import edu.rice.cs.hpc.remote.ui.PasswordDialog;

/********************************************
 * 
 * Remote user information to store and ask for password
 * This class is needed for SSH tunneling
 *
 ********************************************/
public class RemoteUserInfo 
implements UserInfo
{
	final private Shell shell;

	private String password;
	private String user, hostname;
	private int port;
	
	public RemoteUserInfo(Shell shell)
	{			
		this.shell = shell;
	}
	
	
	public void setInfo(String user, String hostname, int port)
	{
		this.user 	  = user;
		this.hostname = hostname;
		this.port 	  = port;
	}
	
	// --------------------------------------------------------------------------------------
	// override methods
	// --------------------------------------------------------------------------------------
	
	@Override
	public boolean promptPassword(String message) {
		PasswordDialog dialog = new PasswordDialog(shell, "Input password for " + hostname + ":" + port,
				"password for user " + user, null, null);
		
		boolean ret =  dialog.open() == Dialog.OK;
		
		if (ret)
			password = dialog.getValue();
		
		return ret;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public boolean promptYesNo(String message) {
		//boolean ret = MessageDialog.openQuestion(shell, "Connection", message);
		return true;
	}

	@Override
	public void showMessage(String message) {
		MessageDialog.openInformation(shell, "Information", message);
	}			

}
