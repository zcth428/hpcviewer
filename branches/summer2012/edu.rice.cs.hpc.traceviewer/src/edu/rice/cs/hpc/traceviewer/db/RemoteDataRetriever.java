package edu.rice.cs.hpc.traceviewer.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import edu.rice.cs.hpc.traceviewer.db.TimeCPID;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

/**
 * Handles communication with the remote server, including asking for data and
 * parsing data, but not opening the connection or closing the connection. It
 * assumes the connection has already been opened by RemoteDBOpener and can be
 * retrieved from SpaceTimeDataControllerRemote.
 * 
 * @author Philip Taffet
 * 
 */
public class RemoteDataRetriever {
	private final Socket socket;
	DataInputStream receiver;
	DataOutputStream sender;
	public RemoteDataRetriever(Socket _serverConnection) throws IOException {
		socket = _serverConnection;
		//TODO:Wrap in GZip stream
		receiver = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		sender = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
	public ProcessTimeline[] getData(int P0, int Pn, double t0, double tn, int vertRes, int horizRes) throws IOException
	{
		//Make the call
		//Check to make sure the server is sending back data
		//Wait/Receive/Parse:
				//			Make into TimeCPID[]
				//			Make into DataByRank
				//			Make into ProcessTimeline
				//			Put into appropriate place in array
		//When all are done, return the array

		requestData(P0, Pn, t0, tn, vertRes, horizRes);
		if (receiver.readInt() != 0x48455245)//"HERE" in ASCII
			throw new IOException("The server did not send back data");
		int RanksReceived = 0;
		int RanksExpected = vertRes;
		ProcessTimeline[] timelines = new ProcessTimeline[RanksExpected];
		while (RanksReceived < RanksExpected)
		{
			int RankNumber = receiver.readInt();
			int Length = receiver.readInt();
			TimeCPID[] ranksData = readTimeCPIDArray(Length, t0, tn);
			TraceDataByRankRemote dataAsTraceDBR = new TraceDataByRankRemote(ranksData);
			//TODO: Figure out how to get the CallPath map!
			ProcessTimeline PTl = new ProcessTimeline(dataAsTraceDBR, null, RankNumber, RanksExpected, tn-t0, t0);
			int IndexInArray;
			if (Pn-P0 > vertRes)
				IndexInArray = (int)Math.round((RankNumber-P0)*(double)vertRes/(Pn-P0));//Its like a line: P0 -> 0, the slope is number of pixels/number of ranks
			else
				IndexInArray = RankNumber-P0;
			timelines[IndexInArray]= PTl;
			RanksReceived++;
		}
		
		return timelines;
	}

	/**
	 * Reads from the stream and creates an array of Timestamp-CPID pairs containing the data for this rank
	 * @param length The number of Timestamp-CPID pairs in this rank (not the length in bytes)
	 * @param t0 The start time
	 * @param tn The end time
	 * @return The array of data for this rank
	 * @throws IOException
	 */
	private TimeCPID[] readTimeCPIDArray(int length, double t0, double tn) throws IOException {
		TimeCPID[] ToReturn = new TimeCPID[length];
		double deltaT = (tn-t0)/length;
		for (int i = 0; i < ToReturn.length; i++) {
			ToReturn[i] = new TimeCPID(t0+i*deltaT, receiver.readInt());//Does this method of getting timestamps actually work???
		}
		return ToReturn;
	}
	private void requestData(int P0, int Pn, double t0, double tn, int vertRes,
			int horizRes) throws IOException {
		sender.writeInt(0x44415441);//"DATA" in ASCII
		sender.writeInt(32);//There will be 32 more bytes in this message
		sender.writeInt(P0);
		sender.writeInt(Pn);
		sender.writeDouble(t0);
		sender.writeDouble(tn);
		sender.writeInt(vertRes);
		sender.writeInt(horizRes);
		//That's it for the message
		sender.flush();
	}

}
