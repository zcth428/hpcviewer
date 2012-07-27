package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.common.ui.TimelineProgressMonitor;
import edu.rice.cs.hpc.traceviewer.db.TimeCPID;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.CallPath;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

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
	private final Socket socket;
	DataInputStream receiver;
	BufferedInputStream rcvBacking;
	DataOutputStream sender;
	
	private final Shell shell;
	
	private final static boolean Compressed = false;
	
	private final IStatusLineManager statusMgr;
	public RemoteDataRetriever(Socket _serverConnection, IStatusLineManager _statusMgr, Shell _shell) throws IOException {
		socket = _serverConnection;
		
		if (Compressed)
		{
			receiver = new DataInputStream(new GZIPInputStream(socket.getInputStream()));
		}
		else
		{
			rcvBacking = new BufferedInputStream(socket.getInputStream());
			receiver = new DataInputStream(rcvBacking);
		}
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
	 * @param horizRes The number of pixels in the horizontal direction (time axis). This is used to compute a delta t that controls how many samples are returned per rank
	 * @return
	 * @throws IOException 
	 */
	public ProcessTimeline[] getData(int P0, int Pn, double t0, double tn, int vertRes, int horizRes, HashMap<Integer, CallPath> _scopeMap) throws IOException
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
		
		if (ResponseCommand != 0x48455245)//"HERE" in ASCII
			throw new IOException("The server did not send back data");
		System.out.println("Data receive begin");
		
		
		int RanksReceived = 0;
		int RanksExpected = Math.min(Pn-P0, vertRes);
		ProcessTimeline[] timelines = new ProcessTimeline[RanksExpected];
		
		TimelineProgressMonitor monitor = new TimelineProgressMonitor(statusMgr);
		monitor.beginProgress(RanksExpected, "Receiving data...", "data", shell);
	
		
		boolean DataCompressed = true;
		
		DataInputStream DataReader;
		
		if (DataCompressed)
			 DataReader = new DataInputStream(new InflaterInputStream(rcvBacking));
		else 
			DataReader = receiver;
		
		while (RanksReceived < RanksExpected)
		{
			monitor.announceProgress();
			int RankNumber = DataReader.readInt();
			int Length = DataReader.readInt();//Number of CPID's
			
			if (RanksExpected - RanksReceived < 3)
				System.out.println(RanksReceived + "/" + RanksExpected );
			
			double startTimeForThisTimeline = DataReader.readDouble();
			double endTimeForThisTimeline = DataReader.readDouble();
			TimeCPID[] ranksData = readTimeCPIDArray(DataReader, Length, startTimeForThisTimeline, endTimeForThisTimeline);
			TraceDataByRankRemote dataAsTraceDBR = new TraceDataByRankRemote(ranksData);
			
			
			int lineNumber = RankNumber;
			/*if (false)//if (Pn-P0 > vertRes)
				lineNumber = (int)Math.round((RankNumber-P0)*(double)vertRes/(Pn-P0));//Its like a line: P0 -> 0, the slope is number of pixels/number of ranks
			else
				lineNumber = RankNumber-P0;*/
			
			ProcessTimeline PTl = new ProcessTimeline(dataAsTraceDBR, _scopeMap, lineNumber, RanksExpected, tn-t0, t0);
			timelines[RankNumber]= PTl;//RankNumber or RankNumber-P0??
			RanksReceived++;
			monitor.announceProgress();
		}
		monitor.endProgress();
		System.out.println("Data receive end");
		return timelines;
	}

	/**
	 * Reads from the stream and creates an array of Timestamp-CPID pairs containing the data for this rank
	 * @param dataReader 
	 * @param length The number of Timestamp-CPID pairs in this rank (not the length in bytes)
	 * @param t0 The start time
	 * @param tn The end time
	 * @return The array of data for this rank
	 * @throws IOException
	 */
	private TimeCPID[] readTimeCPIDArray(DataInputStream dataReader, int length, double t0, double tn) throws IOException {
		TimeCPID[] ToReturn = new TimeCPID[length];
		double deltaT = (tn-t0)/length;
		for (int i = 0; i < ToReturn.length; i++) {
			int CPID = dataReader.readInt();
			if (CPID == 0)
				System.out.println("CPID too small");
			ToReturn[i] = new TimeCPID(t0+i*deltaT, CPID);//Does this method of getting timestamps actually work???
		}
		return ToReturn;
	}
	private void requestData(int P0, int Pn, double t0, double tn, int vertRes,
			int horizRes) throws IOException {
		sender.writeInt(0x44415441);//"DATA" in ASCII
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
		if (Compressed)
			return waitAndReadCompressedInt(receiver);
		else
			return waitAndReadUncompressedInt(receiver);
	}

	private static int waitAndReadCompressedInt(DataInputStream receiver) throws IOException {
		
		// So available is not reliable at all. Sometimes it'll return 1 and
		// then the next byte it reads is -1. It's saying it has data, and then
		// as soon as you try to get the data, it says it doesn't have data.
		// That means we have to do it the hard way...
		int byte1 = 0;
		while((receiver.available()<=0)|| ((byte1 = receiver.read())<=0))
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		
		
		int byte2 = receiver.read();
		int byte3 = receiver.read();
		int byte4 = receiver.read();
		int nextCommand = (byte1<<24)| (byte2<<16)|(byte3<<8)|byte4;
		if (nextCommand < 0)
			System.out.println("Not good");
		System.out.println("Client received a "+ nextCommand);
		return nextCommand;
	}

	private static int waitAndReadUncompressedInt(DataInputStream receiver)
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
		sender.writeInt(0x444F4E45);
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
 * 0x04		Database Path	string-m			Modified UTF-8 encoded path to the database. Should end in the folder that contains the actual trace file. The first two bytes of this message are the length (in bytes) of the string that follows.
 * 
 * The server can then reply with DBOK or NODB
 * 
 * Message DBOK  Server -> Client
 * Notes: This indicates that the server could find the specified database and opened it. It also contains a little additional information to help the client in later rendering.
 * Offset	Name			Type-Length (bytes)	Value
 * 0x00		Message ID		int-4				Must be set to 0x44424F4B (DBOK in ASCII)
 * 0x04		XML Port		int-4				The port to which the client should connect to receive the XML file
 * 0x08		Trace count		int-4				The number of traces contained in the database/trace file
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
 */
