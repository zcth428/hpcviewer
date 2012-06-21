package edu.rice.cs.hpc.traceviewer.db;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

public class RemoteDBOpener extends AbstractDBOpener {

	@Override
	SpaceTimeDataController openDBAndCreateSTDC(IWorkbenchWindow window,
			String[] args, IStatusLineManager statusMgr) {

		throw new UnsupportedOperationException("Not implemented yet");
		// Establish the network connection here (or elsewhere?) and tell the
		// server to open the file and get ready to respond to requests for data
	}

}
