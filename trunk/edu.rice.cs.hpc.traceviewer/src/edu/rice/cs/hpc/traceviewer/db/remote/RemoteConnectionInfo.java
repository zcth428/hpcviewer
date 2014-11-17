package edu.rice.cs.hpc.traceviewer.db.remote;

/************************************************
 * 
 * Info needed for remote connection
 * This class acts as a hub between RemoteDBOpener and OpenDatabaseDialog
 *  
 * We need to reorganize the classes to make it more modular
 *
 ************************************************/
public class RemoteConnectionInfo 
{
  // general info
  public String serverName, serverDatabasePath, serverPort;
  
  // info needed for SSH tunneling
  public String sshTunnelUsername, sshTunnelHostname, sshTunnelPassword;
  
  
  public String toString() 
  {
	  return "Hostname: " + sshTunnelHostname + ", "
			  + "Hostname user: " + sshTunnelUsername ;
  }
}
