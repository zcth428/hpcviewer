package edu.rice.cs.hpc.traceviewer.db.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.zip.GZIPInputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import edu.rice.cs.hpc.data.experiment.extdata.TraceName;
import edu.rice.cs.hpc.traceviewer.remote.LocalTunneling;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.ui.PasswordDialog;
import edu.rice.cs.hpc.traceviewer.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;
import edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener;
import edu.rice.cs.hpc.traceviewer.db.TraceDatabase;
/**
 * Handles the protocol and commands to set up the session with the server.
 * @author Philip Taffet
 *
 */
public class RemoteDBOpener extends AbstractDBOpener 
{
	private static final int PROTOCOL_VERSION = 0x00010001;
	private static final String LOCALHOST = "localhost";
	
	//For more information on message structure, see protocol documentation at the end of RemoteDataReceiver 
	
	private DataOutputStream sender;
	private DataInputStream receiver;

	private final RemoteConnectionInfo connectionInfo;

	private Socket serverConnection = null;
	private LocalTunneling tunnel;

	/**************
	 * constructor
	 * 
	 * @param connectionInfo
	 */
	public RemoteDBOpener(RemoteConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	// --------------------------------------------------------------------------------------
	// override methods
	// --------------------------------------------------------------------------------------
	
	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		int port = Integer.parseInt(connectionInfo.serverPort);
		boolean use_tunnel = (connectionInfo.sshTunnelHostname != null);
		String host = connectionInfo.serverName;
		
		if  (use_tunnel) {
			// we need to setup the SSH tunnel
			if (! tunneling(window, port))
			{
				return null;
			}
			host = LOCALHOST;
		}
		
		
	    boolean connectionSuccess = connectToServer(window, host, port);
		if (!connectionSuccess)
			return null;
		
		try {
			sendOpenDB(connectionInfo.serverDatabasePath);

			// Check for DBOK
			int traceCount;
			int messageTag = RemoteDataRetriever.waitAndReadInt(receiver);
			int xmlMessagePortNumber;
			int compressionType;
			TraceName[] valuesX;
			if (messageTag == Constants.DB_OK)// DBOK
			{
				xmlMessagePortNumber  = receiver.readInt();
				traceCount = receiver.readInt();
				compressionType = receiver.readInt();
				valuesX = formatTraceNames(traceCount);
				
			} else 
			{
				//If the message is not a DBOK, it must be a NODB 
				//Right now, the error code isn't used, but it is there for the future
				int errorCode = receiver.readInt();
				errorMessage="The server could not find traces in the directory:\n"
                    + connectionInfo.serverDatabasePath + "\nPlease select a directory that contains traces. \nError code: " + errorCode+".";
				return null;
			}
			
			Debugger.printDebug(2, "About to connect to socket "+ xmlMessagePortNumber + " at "+ System.nanoTime());
			statusMgr.setMessage("Receiving XML stream");
			
			InputStream xmlStream = getXmlStream(host, port, xmlMessagePortNumber);
			
			if (xmlStream == null) {//null if getting it failed
				errorMessage="Error communicating with server:\nCould not receive XML stream. \nPlease try again.";
				return null;
			}

			RemoteDataRetriever dataRetriever = new RemoteDataRetriever(serverConnection,
					statusMgr, window.getShell(), compressionType);
			SpaceTimeDataControllerRemote stData = new SpaceTimeDataControllerRemote(dataRetriever, window, statusMgr,
					xmlStream, connectionInfo.serverDatabasePath + " on " + host, traceCount, valuesX, sender);

			sendInfoPacket(sender, stData);
			
			return stData;
		} catch (IOException e) {
			errorMessage = "Error communicating with server:\nIO Exception. Please try again.";
			//The protocol is not robust. All exceptions are fatal.
			return null;
		}
	
	}

	@Override
	public void end() {
		try {
			// closing I/O and network connection
			sender.close();
			receiver.close();
			
			serverConnection.close();
			if (tunnel != null) {
				try {
					tunnel.disconnect();
				} catch (JSchException e) {
					System.err.println("Warning: Cannot close the SSH tunnel !");
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------------
	
	/******
	 * 
	 * @param traceCount
	 * @return
	 * @throws IOException
	 */
	private TraceName[] formatTraceNames(int traceCount) throws IOException {
		TraceName[] names  = new TraceName[traceCount];
		for (int i = 0; i < names.length; i++) {
			int processID = receiver.readInt();
			int threadID = receiver.readShort();
			names[i] = new TraceName(processID, threadID);
		}
		return names;
	}


	/**************
	 * open SSH local tunnel
	 * 
	 * @param window
	 * @param port
	 * 
	 * @return true if the connection is successful
	 * 			flase otherwise
	 */
	private boolean tunneling(final IWorkbenchWindow window, int port)
	{
		tunnel = new LocalTunneling(new RemoteUserInfo(window.getShell()));
		
		try {
			tunnel.connect(connectionInfo.sshTunnelUsername, connectionInfo.sshTunnelHostname, 
					connectionInfo.serverName, port);
			return true;
			
		} catch (JSchException e) {
			e.printStackTrace();
			errorMessage = e.getMessage();
			
			MessageDialog.openError(window.getShell(), "Error: cannot create SSH tunnel", e.getMessage());
		}
		return false;
	}
	
	private GZIPInputStream getXmlStream(String serverURL, int port, int xmlMessagePortNumber)
			throws IOException {

		byte[] compressedXMLMessage;
		DataInputStream dxmlReader;
		if (xmlMessagePortNumber == port)
		{
			dxmlReader = receiver;

		}
		else
		{
			Socket xmlConnection = new Socket();
			SocketAddress xmlAddress = new InetSocketAddress(serverURL, xmlMessagePortNumber);
			xmlConnection.connect(xmlAddress, 1000);
			BufferedInputStream buf = new BufferedInputStream(xmlConnection.getInputStream());
			dxmlReader = new DataInputStream(buf);
		}

		int exml = dxmlReader.readInt();
		if (exml != Constants.XML_HEADER) 
		{
			Debugger.printDebug(0,"Expected XML Message (" + Constants.XML_HEADER
					+ ")  on data socket, got " + exml);
			return null;
		}
		int size = dxmlReader.readInt();
		
		compressedXMLMessage = new byte[size];
		int numRead = 0;
		while (numRead < size)
		{
			numRead += dxmlReader.read(compressedXMLMessage, numRead, size- numRead);
		}
		GZIPInputStream xmlStream = new GZIPInputStream(new 
				ByteArrayInputStream(compressedXMLMessage));
		return xmlStream;
	}

	private boolean connectToServer(IWorkbenchWindow window, String serverURL, int port) {
		try {
			if (serverConnection != null && !serverConnection.isClosed()) {
				if (!serverConnection.getRemoteSocketAddress().equals(
						new InetSocketAddress(serverURL, port))) {
					//Connecting to a different server
					TraceDatabase.removeInstance(window);

					serverConnection = new Socket(serverURL, port);
				}
				//Connecting to same server, don't do anything.
			}
			else {// First connection 
				serverConnection = new Socket(serverURL, port);
			}
			sender = new DataOutputStream(new BufferedOutputStream(
					serverConnection.getOutputStream()));
			receiver = new DataInputStream(new BufferedInputStream(
					serverConnection.getInputStream()));
		}
		catch (ConnectException e1) {
			// This is a legitimate catch that we need to expect. The rest
			// should be very rare (ex. the internet goes down in the middle of
			// a transmission)
			errorMessage = "Error connecting to remote server:\nCould not connect. \nMake sure the server is running.";
			return false;
		}
		catch (IOException e) {
			errorMessage = "Error connecting to remote server:\nIO exception. Please try again.";
			return false;
		}
		return true;
	}

	private void sendInfoPacket(DataOutputStream _sender,
			SpaceTimeDataControllerRemote stData) throws IOException {

		// The server
		// needs information (min & max time, etc.) that can only be gotten (as
		// far as I know) from the
		// XML processing that happens in the SpaceTimeDataController
		// constructor, so we construct it, get what we need, then pass in the
		// RemoteDataRetriever as soon as possible.

		/*
		 * Then: overallMinTime (long) overallMaxTime (long) headerSize (int)
		 */
		sender.writeInt(Constants.INFO);
		sender.writeLong(stData.getMinBegTime());
		sender.writeLong(stData.getMaxEndTime());
		sender.writeInt(stData.getHeaderSize());
		sender.flush();

	}

	private void sendOpenDB(String serverPathToDB) throws IOException {
		sender.writeInt(Constants.OPEN);
		sender.writeInt(PROTOCOL_VERSION);
		int len = serverPathToDB.length();
		sender.writeShort(len);
		for (int i = 0; i < len; i++) {
			int charVal = serverPathToDB.charAt(i);
			if (charVal > 0xFF)
				System.out.println("Path to databse cannot contain special characters");
			sender.writeByte(charVal);
		}
		
		sender.flush();

		Debugger.printDebug(0,"Open database message sent");
	}
	
	
	/*********************************
	 * 
	 * private class to prompt user information (if needed)
	 *
	 *********************************/
	static private class RemoteUserInfo implements UserInfo
	{
		private String password;
		final private Shell shell;
		
		private RemoteUserInfo(Shell shell)
		{
			this.shell = shell;
		}
		
		@Override
		public boolean promptPassword(String message) {
			PasswordDialog dialog = new PasswordDialog(shell, "Input password", "Your password", null, null);
			
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
			boolean ret = MessageDialog.openQuestion(shell, "Connection", message);
			return ret;
		}

		@Override
		public void showMessage(String message) {
			MessageDialog.openInformation(shell, "Information", message);
		}			
	}
}


