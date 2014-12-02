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
  
  
  /************
   * check if ssh tunnel is enabled.
   * Currently we define an SSH tunnel is enabled if the hostname is defined.
   * This is not a perfect definition, but we can avoid redundant variables.
   * 
   * @return true if ssh tunnel should be enabled
   */
  public boolean isTunnelEnabled()
  {
	  return sshTunnelHostname != null;
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() 
  {
	  return "Hostname: " + sshTunnelHostname + ", "
			  + "Hostname user: " + sshTunnelUsername ;
  }
}
