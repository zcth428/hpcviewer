package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataControllerRemote;

public class RemoteDBOpener extends AbstractDBOpener {

	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {
		System.out.println("OpenDB called");
		String serverURL = "localhost";
		int port = 21590;
		String serverPathToDB = "/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/";
		String clientPathToXml = "/Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace/experiment.xml";
		// The way it used to be was that the RemoteDataRetriever was passed in
		// the SpaceTimeDataController constructor. However, now the server
		// needs information that can only be gotten (as far as I know) from the
		// XML processing that happens in the SpaceTimeDataController
		// constructor, so we construct it, get what we need, then pass in the
		// RemoteDataRetriever as soon as possible.
		SpaceTimeDataControllerRemote stData = new SpaceTimeDataControllerRemote(window, statusMgr,
				new File(clientPathToXml));
		Socket serverConnection = null;
		try {
			serverConnection = new Socket(serverURL, port);
			DataOutputStream sender = new DataOutputStream(
					new BufferedOutputStream(serverConnection.getOutputStream()));
			sender.writeInt(0x4F50454E);// "OPEN" in ascii
			sender.writeUTF(serverPathToDB);
			/*
			 * Then:
			 * 		overallMinTime (long)
			 * 		overallMaxTime (long)
			 * 		headerSize (int)
			 */
			sender.writeLong(stData.getMinBegTime());
			sender.writeLong(stData.getMaxEndTime());
			sender.writeInt(stData.HEADER_SIZE);
			sender.flush();
			System.out.println("Open databse message sent");

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			RemoteDataRetriever DR = new RemoteDataRetriever(serverConnection);
			stData.setDataRetriever(DR);
			return stData;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;//If an exception was thrown
	}

}
