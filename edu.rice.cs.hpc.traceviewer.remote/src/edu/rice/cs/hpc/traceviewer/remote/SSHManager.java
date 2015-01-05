package edu.rice.cs.hpc.traceviewer.remote;


import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/*****************************************************************
 * 
 * wrapper class for JSCH library
 *
 *****************************************************************/
public class SSHManager 
{
	private static final Logger LOGGER = 
			Logger.getLogger(SSHManager.class.getName());
	private JSch jschSSHChannel;
	private String strUserName;
	private String strConnectionIP;
	private int intConnectionPort;
	private String strPassword;
	private Session sesConnection;
	private int intTimeOut;

	private void doCommonConstructorActions(String userName, 
			String password, String connectionIP, String knownHostsFileName)
	{
		jschSSHChannel = new JSch();

		try
		{
			jschSSHChannel.setKnownHosts(knownHostsFileName);
		}
		catch(JSchException jschX)
		{
			logError(jschX.getMessage());
		}

		strUserName = userName;
		strPassword = password;
		strConnectionIP = connectionIP;
	}

	public SSHManager(String userName, String password, 
			String connectionIP, String knownHostsFileName)
	{
		doCommonConstructorActions(userName, password, 
				connectionIP, knownHostsFileName);
		intConnectionPort = 22;
		intTimeOut = 60000;
	}

	public SSHManager(String userName, String password, String connectionIP, 
			String knownHostsFileName, int connectionPort)
	{
		doCommonConstructorActions(userName, password, connectionIP, 
				knownHostsFileName);
		intConnectionPort = connectionPort;
		intTimeOut = 60000;
	}

	public SSHManager(String userName, String password, String connectionIP, 
			String knownHostsFileName, int connectionPort, int timeOutMilliseconds)
	{
		doCommonConstructorActions(userName, password, connectionIP, 
				knownHostsFileName);
		intConnectionPort = connectionPort;
		intTimeOut = timeOutMilliseconds;
	}

	public String connect()
	{
		String errorMessage = null;

		try
		{
			sesConnection = jschSSHChannel.getSession(strUserName, 
					strConnectionIP, intConnectionPort);
			sesConnection.setPassword(strPassword);
			sesConnection.connect(intTimeOut);
		}
		catch(JSchException jschX)
		{
			errorMessage = jschX.getMessage();
		}

		return errorMessage;
	}

	private String logError(String errorMessage)
	{
		if(errorMessage != null)
		{
			LOGGER.log(Level.SEVERE, "{0}:{1} - {2}", 
					new Object[]{strConnectionIP, intConnectionPort, errorMessage});
		}

		return errorMessage;
	}

	private String logWarning(String warnMessage)
	{
		if(warnMessage != null)
		{
			LOGGER.log(Level.WARNING, "{0}:{1} - {2}", 
					new Object[]{strConnectionIP, intConnectionPort, warnMessage});
		}

		return warnMessage;
	}

	public String sendCommand(String command)
	{
		StringBuilder outputBuffer = new StringBuilder();

		try
		{
			Channel channel = sesConnection.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			channel.connect();
			InputStream commandOutput = channel.getInputStream();
			int readByte = commandOutput.read();

			while(readByte != 0xffffffff)
			{
				outputBuffer.append((char)readByte);
				readByte = commandOutput.read();
			}

			channel.disconnect();
		}
		catch(IOException ioX)
		{
			logWarning(ioX.getMessage());
			return null;
		}
		catch(JSchException jschX)
		{
			logWarning(jschX.getMessage());
			return null;
		}

		return outputBuffer.toString();
	}

	public void close()
	{
		sesConnection.disconnect();
	}

	static public void main(String []args)
	{
		System.out.println("sendCommand");
		
	     /**
	      * YOU MUST CHANGE THE FOLLOWING
	      * FILE_NAME: A FILE IN THE DIRECTORY
	      * USER: LOGIN USER NAME
	      * PASSWORD: PASSWORD FOR THAT USER
	      * HOST: IP ADDRESS OF THE SSH SERVER
	     **/
	     String command = "ls";
	     String userName = "";
	     String password = "";
	     String connectionIP = "";
	     SSHManager instance = new SSHManager(userName, password, connectionIP, "");
	     String errorMessage = instance.connect();

	     if(errorMessage != null)
	     {
	        System.out.println(errorMessage);
	     }

	     String expResult = "FILE_NAME\n";
	     // call sendCommand for each command and the output 
	     //(without prompts) is returned
	     String result = instance.sendCommand(command);
	     // close only after all commands are sent
	     instance.close();
	     assert(expResult.compareTo(result)==0);
	}

}
