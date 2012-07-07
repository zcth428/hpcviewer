package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerRemote;

public class RemoteDBOpener extends AbstractDBOpener {

	DataOutputStream sender;
	DataInputStream receiver;

	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {
		System.out.println("OpenDB called");
		String serverURL = "localhost";
		int port = 21590;
		String serverPathToDB = "/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/";
		//serverPathToDB = "./database";
		// String clientPathToXml =
		// "/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.xml";

		Socket serverConnection = null;
		try {
			serverConnection = new Socket(serverURL, port);
			sender = new DataOutputStream(new BufferedOutputStream(
					serverConnection.getOutputStream()));
			receiver = new DataInputStream(new BufferedInputStream(
					serverConnection.getInputStream()));
		} catch (ConnectException e1) {

			System.out.println("Could not connect. Is the server running?");// This
																			// is
																			// a
																			// legitimate
																			// catch
																			// that
																			// we
																			// need
																			// to
																			// expect.
																			// The
																			// rest
																			// should
																			// be
																			// very
																			// rare
																			// (ex.
																			// the
																			// internet
																			// goes
																			// down
																			// in
																			// the
																			// middle
																			// of
																			// a
																			// transmission)
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
			if (Message == 0x44424F4B)// DBOK
			{
				XMLMessagePortNumber  = receiver.readInt();
				traceCount = receiver.readInt();
			} else if (Message == 0x4E4F4442)// NODB
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
			Socket xmlConnection = new Socket();
			SocketAddress xmlAddress = new InetSocketAddress(serverURL, XMLMessagePortNumber);
			xmlConnection.connect(xmlAddress, 1000);
			GZIPInputStream XMLStream = new GZIPInputStream(
					new BufferedInputStream(xmlConnection.getInputStream()));

			SpaceTimeDataControllerRemote stData;

			RemoteDataRetriever DR = new RemoteDataRetriever(serverConnection,
					statusMgr, window.getShell());
			stData = new SpaceTimeDataControllerRemote(DR, window, statusMgr,
					XMLStream, serverPathToDB + " on " + serverURL, traceCount);

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
		sender.writeInt(0x494E464F);// INFO
		sender.writeLong(stData.getMinBegTime());
		sender.writeLong(stData.getMaxEndTime());
		sender.writeInt(stData.HEADER_SIZE);
		sender.flush();

	}

	private void sendOpenDB(String serverPathToDB) throws IOException {
		sender.writeInt(0x4F50454E);// "OPEN" in ascii
		sender.writeUTF(serverPathToDB);
		sender.flush();

		System.out.println("Open databse message sent");
	}

	@Override
	void closeDB() {
		try {
			sender.writeInt(0x444F4E45);
			sender.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// DONE in ASCII

	}

}
