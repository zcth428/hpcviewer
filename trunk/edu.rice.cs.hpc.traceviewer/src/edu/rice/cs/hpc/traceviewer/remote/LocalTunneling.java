package edu.rice.cs.hpc.traceviewer.remote;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/***************************************************
 * 
 * Wrapper for SSH L-Tunneling with Jsch
 *
 ***************************************************/
public class LocalTunneling 
{

	final private JSch  jsch;
	final private UserInfo userInfo;
	
	private Session session;
	private int port;
	
	public LocalTunneling(UserInfo userInfo)
	{
		jsch 		  = new JSch();
		this.userInfo = userInfo;
	}
	
	/*******
	 * Build SSH port local forwarding connection 
	 * 
	 * @param login_user : username at login host
	 * @param login_host : the login host
	 * @param remote_host : the remote host where a server listens
	 * @param port : the port number (the same as the port that
	 * 				 the server listens)
	 * 
	 * @return the assigned port
	 * 
	 * @throws JSchException
	 */
	public int connect(String login_user, String login_host, 
			String remote_host, int port)
			throws JSchException
	{
		session = jsch.getSession(login_user, login_host, 22);
		session.setUserInfo(userInfo);
		session.connect();
		
		int assigned_port = session.setPortForwardingL(port, remote_host, port);
		this.port = assigned_port;

		return this.port;
	}
	
	
	public void disconnect() throws JSchException
	{
		session.delPortForwardingL(port);
		session.disconnect();
	}
	
	/*******
	 * unit test 
	 * @param args
	 */
	public static void main(final String []args)
	{
		if (args.length != 4)
		{
			System.out.println("Required arguments: user password login_host remote_host");
			return;
		}
		LocalTunneling tunnel = new LocalTunneling( new UserInfo() {

			@Override
			public boolean promptPassword(String message) {
				System.out.println(message);
				return true;
			}

			@Override
			public String getPassword() {
				return args[1];
			}

			@Override
			public boolean promptPassphrase(String message) {
				System.out.println(message);
				return true;
			}

			@Override
			public String getPassphrase() {
				return null;
			}

			@Override
			public boolean promptYesNo(String message) {
				System.out.println(message);
				return true;
			}

			@Override
			public void showMessage(String message) {
				System.out.println(message);				
			}
			
		});
		
		try {
			int port = tunnel.connect(args[0], args[2], args[3], 21590 );
			System.out.println("Assigned port: " + port);
			
			tunnel.disconnect();
			
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
}
