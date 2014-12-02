package edu.rice.cs.hpc.traceviewer.db.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
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
 * 
 * For more information on message structure, see protocol documentation at the end of RemoteDataReceiver
 * 
 * @author Philip Taffet
 *
 */
public class RemoteDBOpener extends AbstractDBOpener 
{
	// -----------------
	// constants
	// -----------------
	
	private static final int PROTOCOL_VERSION = 0x00010001;
	private static final String LOCALHOST = "localhost";

	// -----------------
	// static variables
	// -----------------
	// TODO: static variables are discouraged in Eclipse since
	// 		 it isn't suitable for multiple instance of applications
	// -----------------

	static private Socket serverConnection = null;

	// -----------------
	// object variables
	// -----------------
	
	private final RemoteConnectionInfo connectionInfo;

	private DataOutputStream sender;
	private DataInputStream receiver;

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
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener#openDBAndCreateSTDC(org.eclipse.ui.IWorkbenchWindow, 
	 * java.lang.String[], org.eclipse.jface.action.IStatusLineManager)
	 */
	public SpaceTimeDataController openDBAndCreateSTDC(
			IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) 
			throws InvalExperimentException, Exception 
	{

		// --------------------------------------------------------------
		// step 1 : create a SSH tunnel for the main port if necessary
		// --------------------------------------------------------------
		
		int port = Integer.parseInt(connectionInfo.serverPort);
		boolean use_tunnel = connectionInfo.isTunnelEnabled();
		String host = connectionInfo.serverName;
		
		if  (use_tunnel) {
			// we need to setup the SSH tunnel
			createSSHTunnel(window, port);
			host = LOCALHOST;
		}
		
		// --------------------------------------------------------------
		// step 2 : initial contact to the server.
		//			if there's no reply or I/O error, we quit
		// --------------------------------------------------------------
		
	    connectToServer(window, host, port);

		sendOpenDB(connectionInfo.serverDatabasePath);

 		// Check for DBOK
		String errorMessage;
		
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
                + connectionInfo.serverDatabasePath + "\nPlease select a directory that contains traces.\nError code: " + errorCode ;
			throw new IOException(errorMessage);
		}
		
		// --------------------------------------------------------------
		// step 3 : create a SSH tunnel for XML port if necessary
		// --------------------------------------------------------------
		
		Debugger.printDebug(2, "About to connect to socket "+ xmlMessagePortNumber + " at "+ System.nanoTime());
		
		if (use_tunnel &&  (port != xmlMessagePortNumber)) {
			// only create SSH tunnel if the XML socket has different port number
			createSSHTunnel(window, xmlMessagePortNumber);			
		}
		
		statusMgr.setMessage("Receiving XML stream");
		
		InputStream xmlStream = getXmlStream(host, port, xmlMessagePortNumber);
		
		if (xmlStream == null) {//null if getting it failed
			errorMessage="Error communicating with server:\nCould not receive XML stream. \nPlease try again.";
			throw new IOException(errorMessage);
		}

		// --------------------------------------------------------------
		// step 4 : prepare communication channel
		// --------------------------------------------------------------
		
		RemoteDataRetriever dataRetriever = new RemoteDataRetriever(serverConnection,
				statusMgr, window.getShell(), compressionType);
		
		SpaceTimeDataControllerRemote stData = new SpaceTimeDataControllerRemote(dataRetriever, window, statusMgr,
				xmlStream, connectionInfo.serverDatabasePath + " on " + host, traceCount, valuesX, sender);

		sendInfoPacket(sender, stData);
		
		return stData;	
	}
	

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.db.AbstractDBOpener#end()
	 */
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
	
	/*****
	 * a wrapper for tunneling() function to throw an IOException
	 * 
	 * @param window
	 * @param port
	 * @throws IOException
	 */
	private void createSSHTunnel(IWorkbenchWindow window, int port) 
			throws IOException
	{
		// we need to setup the SSH tunnel
		try {
			tunneling(window, port);
		} catch (JSchException e) {
			throw new IOException(e.getMessage()+": Unable to create SSH tunnel to " + connectionInfo + 
					"\nPort: " + port);
		}
	}
	
	
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
	 * @throws JSchException 
	 */
	private void tunneling(final IWorkbenchWindow window, int port) 
			throws JSchException
	{
		tunnel = new LocalTunneling(new RemoteUserInfo(window.getShell(), connectionInfo.sshTunnelUsername,
									connectionInfo.sshTunnelHostname));
		
		tunnel.connect(connectionInfo.sshTunnelUsername, connectionInfo.sshTunnelHostname, 
				connectionInfo.serverName, port);
	}
	
	/***************
	 * Get XML data from the server
	 * 
	 * @param serverURL
	 * @param port
	 * @param xmlMessagePortNumber
	 * 
	 * @return XML data in zipped format
	 * 
	 * @throws IOException
	 */
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

	
	/*****************
	 * Try to connect to a remote server
	 * 
	 * @param window
	 * @param serverURL
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void connectToServer(IWorkbenchWindow window, String serverURL, int port) 
			throws UnknownHostException, IOException 
	{
		if (serverConnection != null && !serverConnection.isClosed()) 
		{
			InetSocketAddress addr = new InetSocketAddress(serverURL, port);
			SocketAddress sockAddr = serverConnection.getRemoteSocketAddress();
			
			if (sockAddr.equals(addr)) 
			{
				//Connecting to same server, don't do anything.
				initDataIOStream();
				return;
			} else {
				//Connecting to a different server
				TraceDatabase.removeInstance(window);
			}
		}
		serverConnection = new Socket(serverURL, port);
		initDataIOStream();
	}
	
	
	private void initDataIOStream() 
			throws IOException
	{
		sender = new DataOutputStream(new BufferedOutputStream(
				serverConnection.getOutputStream()));
		receiver = new DataInputStream(new BufferedInputStream(
				serverConnection.getInputStream()));
	}

	/*******
	 * sending info message to the server
	 * 
	 * @param _sender
	 * @param stData
	 * @throws IOException
	 *******/
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

	
	/*******
	 * sending a message to the server to open a database
	 * 
	 * @param serverPathToDB
	 * @throws IOException
	 *******/
	private void sendOpenDB(String serverPathToDB) 
			throws IOException 
			{
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
		final private String user, hostname;
		
		private RemoteUserInfo(Shell shell, String user, String hostname)
		{
			this.shell = shell;
			this.user  = user;
			this.hostname = hostname;
		}
		
		@Override
		public boolean promptPassword(String message) {
			PasswordDialog dialog = new PasswordDialog(shell, "Input password for " + hostname,
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
			boolean ret = MessageDialog.openQuestion(shell, "Connection", message);
			return ret;
		}

		@Override
		public void showMessage(String message) {
			MessageDialog.openInformation(shell, "Information", message);
		}			
	}
}


