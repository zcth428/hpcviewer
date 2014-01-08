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
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.data.experiment.extdata.TraceName;
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
	private static final int PROTOCOL_VERSION = 0x00010001;
	//For more information on message structure, see protocol documentation at the end of RemoteDataReceiver 
	DataOutputStream sender;
	DataInputStream receiver;
	final String[] data = new String[3]; //data passed from OpenDatabaseDialog
	
	public RemoteDBOpener(String[] inData) {
		for (int i=0;i<3;i++) {
			data[i]=inData[i];
		}
	}

	static Socket serverConnection = null;
	@Override
	public SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		String serverURL = data[0];
		int port = Integer.parseInt(data[1]);
		String serverPathToDB = data[2];
		

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
                    + data[2] + "\nPlease select a directory that contains traces. \nError code: " + errorCode+".";
				return null;
			}
			
			Debugger.printDebug(2, "About to connect to socket "+ xmlMessagePortNumber + " at "+ System.nanoTime());
			statusMgr.setMessage("Receiving XML stream");
			
			InputStream xmlStream = getXmlStream(serverURL, port, xmlMessagePortNumber);
			
			if (xmlStream == null) {//null if getting it failed
				errorMessage="Error communicating with server:\nCould not receive XML stream. \nPlease try again.";
				return null;
			}

			RemoteDataRetriever dataRetriever = new RemoteDataRetriever(serverConnection,
					statusMgr, window.getShell(), compressionType);
			SpaceTimeDataControllerRemote stData = new SpaceTimeDataControllerRemote(dataRetriever, window, statusMgr,
					xmlStream, serverPathToDB + " on " + serverURL, traceCount, valuesX, sender);

			sendInfoPacket(sender, stData);
			
			return stData;
		} catch (IOException e) {
			errorMessage = "Error communicating with server:\nIO Exception. Please try again.";
			//The protocol is not robust. All exceptions are fatal.
			return null;
		}
	
	}

	private TraceName[] formatTraceNames(int traceCount) throws IOException {
		TraceName[] names  = new TraceName[traceCount];
		for (int i = 0; i < names.length; i++) {
			int processID = receiver.readInt();
			int threadID = receiver.readShort();
			names[i] = new TraceName(processID, threadID);
		}
		return names;
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

}


