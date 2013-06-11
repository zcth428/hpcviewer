package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;
import edu.rice.cs.hpc.traceviewer.util.Constants;

/**
 * Handles communication with the remote server, including asking for data and
 * parsing data, but not opening the connection or closing the connection. It
 * assumes the connection has already been opened by RemoteDBOpener and can be
 * retrieved from SpaceTimeDataControllerRemote.
 * See protocol documentation at the end of this file.
 * 
 * @author Philip Taffet
 * 
 */
public class RemoteDataRetriever {
	//For more information on message structure, see protocol documentation at the end of this file. 
	private static final int DATA = 0x44415441;
	private static final int HERE = 0x48455245;
	private final Socket socket;
	DataInputStream receiver;
	BufferedInputStream rcvBacking;
	DataOutputStream sender;
	
	private final Shell shell;
	
	final int compressionType;
	
	private final IStatusLineManager statusMgr;

	public RemoteDataRetriever(Socket _serverConnection, IStatusLineManager _statusMgr, Shell _shell, int _compressionType) throws IOException {
		socket = _serverConnection;
		
		compressionType = _compressionType;
		
		rcvBacking = new BufferedInputStream(socket.getInputStream());
		receiver = new DataInputStream(rcvBacking);
		
		sender = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		
		
		statusMgr = _statusMgr;
		shell = _shell;
		
	}
	//TODO: Inclusive or exclusive?
	/**
	 * Issues a command to the remote server for the data requested, and waits for a response.
	 * @param P0 The lower bound of the ranks to get
	 * @param Pn The upper bound of the ranks to get
	 * @param t0 The lower bound for the time to get
	 * @param tn The upper bound for the time to get
	 * @param vertRes The number of pixels in the vertical direction (process axis). This is used to compute a stride so that not every rank is included
	 * @param horizRes The number of pixels in the horizontal direction (time axis). This is used to compute a delta_t that controls how many samples are returned per rank
	 * @return
	 * @throws IOException 
	 */
	public void getData(int P0, int Pn, double t0, double tn, int vertRes, int horizRes, HashMap<Integer, CallPath> _scopeMap) throws IOException
	{
		//Make the call
		//Check to make sure the server is sending back data
		//Wait/Receive/Parse:
				//			Make into TimeCPID[]
				//			Make into DataByRank
				//			Make into ProcessTimeline
				//			Put into appropriate place in array
		//When all are done, return the array
		
		statusMgr.setMessage("Requesting data");
		shell.update();
		requestData(P0, Pn, t0, tn, vertRes, horizRes);
		System.out.println("Data request finished");
		
		int ResponseCommand = waitAndReadInt(receiver);
		statusMgr.setMessage("Receiving data");
		shell.update();
		
		if (ResponseCommand != HERE)//"HERE" in ASCII
			throw new IOException("The server did not send back data");
		System.out.println("Data receive begin");
		
		
		int RanksReceived = 0;
		int RanksExpected = Math.min(Pn-P0, vertRes);
		
		
		TimelineProgressMonitor monitor = new TimelineProgressMonitor(statusMgr);
		monitor.beginProgress(RanksExpected, "Receiving data...", "data", shell);
	
		DataInputStream DataReader;
		
		DataReader = receiver;
		
		while (RanksReceived < RanksExpected)
		{
			monitor.announceProgress();
			int rankNumber = DataReader.readInt();
			int Length = DataReader.readInt();//Number of CPID's
			
			
			double startTimeForThisTimeline = DataReader.readDouble();
			double endTimeForThisTimeline = DataReader.readDouble();
			int compressedSize = DataReader.readInt();
			byte[] compressedTraceLine = new byte[compressedSize];
			
			int numRead = 0;
			while (numRead < compressedSize)
			{
				numRead += DataReader.read(compressedTraceLine, numRead, compressedSize- numRead);
				
			}
					
			DecompressionThread.workToDo.add(new DecompressionThread.DecompressionItemToDo(compressedTraceLine, Length, startTimeForThisTimeline, endTimeForThisTimeline, rankNumber, compressionType));
			
			RanksReceived++;
			monitor.announceProgress();
		}
		monitor.endProgress();
		System.out.println("Data receive end");
		
	}

	
	private void requestData(int P0, int Pn, double t0, double tn, int vertRes,
			int horizRes) throws IOException {
		sender.writeInt(DATA);
		sender.writeInt(P0);
		sender.writeInt(Pn);
		sender.writeDouble(t0);
		sender.writeDouble(tn);
		sender.writeInt(vertRes);
		sender.writeInt(horizRes);
		//That's it for the message
		sender.flush();
	}


	static int waitAndReadInt(DataInputStream receiver)
			throws IOException {
		int nextCommand;
		// Sometime the buffer is filled with 0s for some reason. This flushes
		// them out. This is awful, but otherwise we just get 0s

		while (receiver.available() <= 4
				|| ((nextCommand = receiver.readInt()) == 0)) {

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		if (receiver.available() < 4)// There certainly isn't a message
										// available, since every message is at
										// least 4 bytes, but the next time the
										// buffer has anything there will be a
										// message
		{
			receiver.read(new byte[receiver.available()]);// Flush the rest of
															// the buffer
			while (receiver.available() <= 0) {

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			nextCommand = receiver.readInt();
		}
		return nextCommand;
	}
	public void Close() throws IOException {
		sender.writeInt(Constants.DONE);
		sender.flush();
		sender.close();
		receiver.close();
		socket.close();
		
	}
}
/**
 ******* PROTOCOL DOCUMENTATION *******
 * 
 * Global note: Big-endian encoding is used throughout. Client indicates the computer with the front end, while server indicates the supercomputer doing the processing.
 * 
 * Message OPEN  Client -> Server
 * Notes: This should be the first message sent. It tells the remote server to open the database file. It also gives the server a little additional information to help it process the database.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x4F50454E (OPEN in ASCII)
 * 0x04		Path length		short-2				The length (in bytes) of the string that follows.
 * 0x06 	Database path	string-m			UTF-8 encoded path to the database. Should end in the folder that contains the actual trace file. If the path contains strange characters that don't fit in 8 bits, it is not considered a valid path.
 * 
 * The server can then reply with DBOK or NODB
 * 
 * Message DBOK  Server -> Client
 * Notes: This indicates that the server could find the specified database and opened it. It also contains a little additional information to help the client in later rendering.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x44424F4B (DBOK in ASCII)
 * 0x04		XML Port		int-4				The port to which the client should connect to receive the XML file
 * 0x08		Trace count		int-4				The number of traces contained in the database/trace file
 * 0x0C		Compression		int-4				The compression type and algorithm used. Right now the only values are 0 = uncompressed and 1 = zlib compressed, but this could be extended
 * 0x10+6n	Process ID		int-4				The process number for rank n. This is used only to label the location of the cursor. n goes from 0 to (Traces count-1)
 * 0x14+6n	Thread ID		short-2				The thread number for rank n. If this has a value of -1, then neither it nor the period between the process and thread numbers should be displayed
 * 
 * Message NODB	Server -> Client
 * Notes: This indicates that the server could not find the database or could not open it for some reason. The user should be notified and the next message the client sends should be another OPEN command.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x4E4F4442 (NODB in ASCII)
 * 0x04		Error Code		int-4				Currently unused, but the server could specify a code to make diagnosing the error easier. Set to 0 for right now.
 * 
 * The XML file should be sent as soon as the client connects to the appropriate port. It is GZIP compressed.
 * 
 * Message INFO Client -> Server
 * Notes: This contains information derived from the XML data that the server needs in order to understand the later data requests
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x494E464F (INFO in ASCII)
 * 0x04		Global Min Time long-8				The lowest starting time for all the traces. This is mapped to 0 at some point during the execution. This value comes from the experiment.xml file
 * 0x0C		Global Max Time long-8				The highest ending time for all the traces. This can also be found in experiment.xml
 * 0x12		DB Header Size	int-4				The size of the header in the DB file. TraceDataByRank uses it to pinpoint the location of each trace.
 *  
 * Message DATA Client -> Server
 * Notes: This message represents a request for the server to retrieve data from the file and return it to the client
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x44415441 (DATA in ASCII)
 * 0x04		First Process	int-4				The lower bound on the processes to be retrieved
 * 0x08		Last Process	int-4				The upper bound on the processes to be retrieved
 * 0x0C		Time Start		double-8			The lower bound on the time of the traces to be retrieved. This is the absolute time, not the time since Global Min Time.
 * 0x12		Time End		double-8			The upper bound on the time of the traces to be retrieved. Again, the absolute time.
 * 0x1A		Vertical Res	int-4				The vertical resolution of the detail view. The server uses this to determine which processes should be returned from the range [First Process, Last Process]
 * 0x1E		Horizontal Res	int-4				The horizontal resolution of the detail view. The server will return approximately this many CPIDs for each trace
 * 
 * Message HERE Server -> Client
 * Notes: This is a response to the DATA request. After this message, the client may send another DATA request or a DONE shutdown command. After each rank is received, k should be incremented by (28+c). The client should expect the message to contain min(Last Process-First Process, Vertical Resolution) tracelines.
 * The raw trace data is a pair of 4-byte ints. The first int is the difference between the timestamp for this Record and the previous Record. For the first Record in the message, it should be zero, as that record will have Begin Time as its timestamp. The second int in the pair is the CPID.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x48455245 (HERE in ASCII)
 * 0x04+k	Line Number		int-4				The rank number whose data follows. Should be unique in the message
 * 0x08+k	Entry Count		int-4				The number of CPID's that follow
 * 0x0C+k	Begin Time		double-8			The start time of this rank, calculated by taking the timestamp of the first TimeCPID in the line
 * 0x14+k	End Time		double-8			The end time of this rank, calculated by taking the timestamp of the last TimeCPID in the line
 * 0x1C+k	Compressed Size	int-4				The size of the data, c, that follows. If compression is disabled, this should be equal to 4*(Entry Count)
 * 0x20+k+c	Trace Data		ints or bytes		The raw trace data. If compression is disabled, this is an array of 2x(4 bytes), one after the other. If compression is enabled, this is a compressed array of 2x(4 bytes). See the message notes for more information.
 * 
 * 
 * Message DONE Client -> Server
 * Notes: After receiving this message, the server should close. The client cannot send any messages after this without opening a new connection
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x444F4E45 (DONE in ASCII)
 */
