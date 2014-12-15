package edu.rice.cs.hpc.traceviewer.db;

/************************************************
 * 
 * Info needed for remote connection
 * This class acts as a hub between RemoteDBOpener and OpenDatabaseDialog
 *  
 * We need to reorganize the classes to make it more modular
 *
 ************************************************/
public class DatabaseAccessInfo 
{
  // general info
  public String serverName = null, databasePath = null, serverPort = null;
  
  // info needed for SSH tunneling
  public String sshTunnelUsername = null, sshTunnelHostname = null, sshTunnelPassword = null;
  
  
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
  
  public boolean isLocal() 
  {
	  if (serverName == null) {
		  if (databasePath != null)
			  return true;
		  
		  // both local database and remote information cannot be null
		  // if this is the case, it should be error in code design ! 
		  
		  throw new RuntimeException("Path to the local database is null");
	  }
	  return false;
  }
}
