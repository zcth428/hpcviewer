package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
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
		String serverURL = "localhost";
		int port = 21590;
		String serverPathToDB = "Users/pat2/Downloads/hpctoolkit-chombo-crayxe6-1024pe-trace";
		Socket serverConnection = null;
		try {
			serverConnection = new Socket(serverURL, port);
			DataOutputStream sender = new DataOutputStream(new BufferedOutputStream(serverConnection.getOutputStream()));
			sender.writeInt(0x4F50454E);//"OPEN" in ascii
			sender.writeUTF(serverPathToDB);
			sender.flush();

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		//Connect socket, send open db command
		RemoteDataRetriever DR;
		try {
			DR = new RemoteDataRetriever(serverConnection);
			return new SpaceTimeDataControllerRemote(DR);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
