package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.server.ServerNotActiveException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerRemote;

public class RemoteDBOpener extends AbstractDBOpener {
	//For more information on message structure, see protocol documentation at the end of RemoteDataReceiver 
	private static final int DONE = 0x444F4E45;
	private static final int OPEN = 0x4F50454E;
	private static final int INFO = 0x494E464F;
	private static final int XML_HEADER = 0x45584D4C;
	private static final int DATABASE_NOT_FOUND = 0x4E4F4442;
	private static final int DB_OK = 0x44424F4B;
	DataOutputStream sender;
	DataInputStream receiver;

	static Socket serverConnection = null;
	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		String serverURL = args[0];
		int port = Integer.parseInt(args[1]);
		String serverPathToDB = args[2];
		

		//Socket serverConnection = null;
		try {
			if (serverConnection != null) {
				if (!serverConnection.getRemoteSocketAddress().equals(
						new InetSocketAddress(serverURL, port))) {
					closeDB();//Connecting to a new server
					serverConnection = new Socket(serverURL, port);
				}
			} else {//First connection
				serverConnection = new Socket(serverURL, port);
			}
			sender = new DataOutputStream(new BufferedOutputStream(
					serverConnection.getOutputStream()));
			receiver = new DataInputStream(new BufferedInputStream(
					serverConnection.getInputStream()));
		} catch (ConnectException e1) {
			// This is a legitimate catch that we need to expect. The rest
			// should be very rare (ex. the internet goes down in the middle of
			// a transmission)
			System.out.println("Could not connect. Is the server running?");
			return null;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		try {
			sendOpenDB(serverPathToDB);

			// Check for DBOK
			int traceCount;
			int Message = RemoteDataRetriever.waitAndReadInt(receiver);
			int XMLMessagePortNumber;
			int CompressionType;
			String[] ValuesX;
			if (Message == DB_OK)// DBOK
			{
				XMLMessagePortNumber  = receiver.readInt();
				traceCount = receiver.readInt();
				CompressionType = receiver.readInt();
				ValuesX = new String[traceCount];
				for (int i = 0; i < ValuesX.length; i++) {
					int processID = receiver.readInt();
					int threadID = receiver.readShort();
					if (threadID == -1) {
						ValuesX[i] = Integer.toString(processID);
					} else {
						ValuesX[i] = processID + "." + threadID;
					}
				}
				
			} else if (Message == DATABASE_NOT_FOUND)// NODB
			{
				// Tell the user
				// TODO: Implement some UI notification
				int errorCode = receiver.readInt();// Unused but there for the
													// future
				System.err.println("The server could not find that database.");
				return null;// We probably actually want it to be in a loop so
							// that
							// we just prompt the user to try again
			} else {
				throw new IOException("Unrecognized message sent");
			}
			System.out.println("About to connect to socket "+ XMLMessagePortNumber + " at "+ System.nanoTime());
			statusMgr.setMessage("Receiving XML stream");
			byte[] CompressedXMLMessage;
			if (XMLMessagePortNumber == port)
			{
				int exml = receiver.readInt();
				if (exml != XML_HEADER)
					System.out.println("Expected XML Message (" +XML_HEADER +")  on data socket, got " + exml);
				int size = receiver.readInt();
				CompressedXMLMessage = new byte[size];
				int numRead = 0;
				while (numRead < size)
				{
					numRead += receiver.read(CompressedXMLMessage, numRead, size- numRead);
				}
			}
			else
			{
				Socket xmlConnection = new Socket();
				SocketAddress xmlAddress = new InetSocketAddress(serverURL, XMLMessagePortNumber);
				xmlConnection.connect(xmlAddress, 1000);
				BufferedInputStream buf = new BufferedInputStream(xmlConnection.getInputStream());
				DataInputStream dxmlreader = new DataInputStream(buf);
				int size = dxmlreader.readInt();
				CompressedXMLMessage = new byte[size];
				int numRead = 0;
				while (numRead < size)
				{
					numRead += buf.read(CompressedXMLMessage, numRead, size- numRead);
				}
			}
			
			GZIPInputStream XMLStream = new GZIPInputStream(new 
					ByteArrayInputStream(CompressedXMLMessage));

			SpaceTimeDataControllerRemote stData;

			RemoteDataRetriever DR = new RemoteDataRetriever(serverConnection,
					statusMgr, window.getShell(), CompressionType);
			stData = new SpaceTimeDataControllerRemote(DR, window, statusMgr,
					XMLStream, serverPathToDB + " on " + serverURL, traceCount, ValuesX);

			sendInfoPacket(sender, stData);
			
			return stData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;// If an exception was thrown
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
		sender.writeInt(INFO);
		sender.writeLong(stData.getMinBegTime());
		sender.writeLong(stData.getMaxEndTime());
		sender.writeInt(stData.HEADER_SIZE);
		sender.flush();

	}

	private void sendOpenDB(String serverPathToDB) throws IOException {
		sender.writeInt(OPEN);
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

	@Override
	void closeDB() {
		try {
			DataOutputStream closer = new DataOutputStream(serverConnection.getOutputStream());
			closer.writeInt(DONE);
			closer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
