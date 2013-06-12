package edu.rice.cs.hpc.traceviewer.db;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerRemote;
import edu.rice.cs.hpc.traceviewer.util.Constants;
import edu.rice.cs.hpc.traceviewer.util.Debugger;
/**
 * Handles the protocol and commands to set up the session with the server.
 * @author Philip Taffet
 *
 */
public class RemoteDBOpener extends AbstractDBOpener {
	//For more information on message structure, see protocol documentation at the end of RemoteDataReceiver 
	DataOutputStream sender;
	DataInputStream receiver;

	static Socket serverConnection = null;
	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		String serverURL = args[0];
		int port = Integer.parseInt(args[1]);
		String serverPathToDB = args[2];
		

		//Socket serverConnection = null;
		boolean connectionSuccess = connectToServer(window, serverURL, port);
		if (!connectionSuccess)
			return null;
		
		try {
			sendOpenDB(serverPathToDB);

			// Check for DBOK
			int traceCount;
			int messageTag = RemoteDataRetriever.waitAndReadInt(receiver);
			int xmlMessagePortNumber;
			int compressionType;
			String[] valuesX;
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
				MessageDialog.openError(window.getShell(),"Database not found.", 
						"The server could not find that database.\nError code: " + errorCode);
				return null;
			}
			
			Debugger.printDebug(2, "About to connect to socket "+ xmlMessagePortNumber + " at "+ System.nanoTime());
			statusMgr.setMessage("Receiving XML stream");
			
			InputStream xmlStream = getXmlStream(serverURL, port, xmlMessagePortNumber);
			
			if (xmlStream == null)//null if getting it failed
				return null;

			RemoteDataRetriever dataRetriever = new RemoteDataRetriever(serverConnection,
					statusMgr, window.getShell(), compressionType);
			SpaceTimeDataControllerRemote stData = new SpaceTimeDataControllerRemote(dataRetriever, window, statusMgr,
					xmlStream, serverPathToDB + " on " + serverURL, traceCount, valuesX);

			sendInfoPacket(sender, stData);
			
			return stData;
		} catch (IOException e) {
			MessageDialog.openError(window.getShell(), "I/O Error", 
					e.getMessage());
			//The protocol is not robust. All exceptions are fatal.
			return null;
		}
	
	}

	private String[] formatTraceNames(int traceCount) throws IOException {
		String[] valuesX;
		valuesX = new String[traceCount];
		for (int i = 0; i < valuesX.length; i++) {
			int processID = receiver.readInt();
			int threadID = receiver.readShort();
			if (threadID == -1) {
				valuesX[i] = Integer.toString(processID);
			} else {
				valuesX[i] = processID + "." + threadID;
			}
		}
		return valuesX;
	}

	private GZIPInputStream getXmlStream(String serverURL, int port, int xmlMessagePortNumber)
			throws IOException {
		
		byte[] compressedXMLMessage;
		if (xmlMessagePortNumber == port)
		{
			int exml = receiver.readInt();
			if (exml != Constants.XML_HEADER) 
			{
				System.out.println("Expected XML Message (" + Constants.XML_HEADER
						+ ")  on data socket, got " + exml);
				return null;
			}
			int size = receiver.readInt();
			compressedXMLMessage = new byte[size];
			int numRead = 0;
			while (numRead < size)
			{
				numRead += receiver.read(compressedXMLMessage, numRead, size- numRead);
			}
		}
		else
		{
			Socket xmlConnection = new Socket();
			SocketAddress xmlAddress = new InetSocketAddress(serverURL, xmlMessagePortNumber);
			xmlConnection.connect(xmlAddress, 1000);
			BufferedInputStream buf = new BufferedInputStream(xmlConnection.getInputStream());
			DataInputStream dxmlreader = new DataInputStream(buf);
			int size = dxmlreader.readInt();
			compressedXMLMessage = new byte[size];
			int numRead = 0;
			while (numRead < size)
			{
				numRead += buf.read(compressedXMLMessage, numRead, size- numRead);
			}
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

					TraceDatabase.removeInstance(window);

					serverConnection = new Socket(serverURL, port);
				}
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
			MessageDialog.openError(window.getShell(), "Error connecting to remote server",
					"Could not connect. Make sure the server is running.");
			return false;
		}
		catch (IOException e) {
			MessageDialog.openError(window.getShell(), "Error connecting to remote server",
					e.getMessage());
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
		sender.writeInt(stData.HEADER_SIZE);
		sender.flush();

	}

	private void sendOpenDB(String serverPathToDB) throws IOException {
		sender.writeInt(Constants.OPEN);
		int len = serverPathToDB.length();
		sender.writeShort(len);
		for (int i = 0; i < len; i++) {
			int charVal = serverPathToDB.charAt(i);
			if (charVal > 0xFF)
				System.out.println("Path to databse cannot contain special characters");
			sender.writeByte(charVal);
		}
		
		sender.flush();

		System.out.println("Open databse message sent");
	}

}
