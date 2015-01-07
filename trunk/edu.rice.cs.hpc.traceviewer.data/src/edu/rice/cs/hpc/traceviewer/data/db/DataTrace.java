package edu.rice.cs.hpc.traceviewer.data.db;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.rice.cs.hpc.data.db.DataCommon;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	private final static String TRACE_NAME = "hpctoolkit trace metrics";

	long index_start, index_length;
	long trace_start, trace_length;
	long min_time,	  max_time;

	int  size_offset, size_length;
	int  size_gtid,	  size_time;
	int  size_cctid;
	
	public DataTrace() {
	}

	@Override
	protected boolean isTypeFormatCorrect(long type) {
		return type == 2;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.compareTo(TRACE_NAME) >= 0;
	}

	@Override
	protected boolean readNext(FileChannel input)
			throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(256);
		int numBytes      = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			
			index_start  = buffer.getLong();
			index_length = buffer.getLong();
			
			trace_start  = buffer.getLong();
			trace_length = buffer.getLong();
			
			min_time = buffer.getLong();
			max_time = buffer.getLong();
			
			size_offset = buffer.getInt();
			size_length = buffer.getInt();
			size_gtid   = buffer.getInt();
			size_time   = buffer.getInt();
			size_cctid  = buffer.getInt();
		}
		
		return true;
	}

	public void read(int rank, long index)
	{
		
	}
	
	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		out.println("Min time: " + min_time);
		out.println("Max time: " + max_time);
		
		out.println("index start: " + index_start);
		out.println("index length: " + index_length);
		
		out.println(" trace start: " + trace_start + "\n trace length: " + trace_length);
		
		out.println("size offset: " + size_offset);
		out.println("size length: " + size_length);
		out.println("size time: " + size_time);
		out.println("size cctid: " + size_cctid + "\n size gtid: " + size_gtid);
	}

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		DataTrace trace_data = new DataTrace();
		try {
			trace_data.open("/Users/laksonoadhianto/work/data/new-prof/hpctoolkit-trace-database-32465/trace.db");			
			trace_data.printInfo(System.out);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
