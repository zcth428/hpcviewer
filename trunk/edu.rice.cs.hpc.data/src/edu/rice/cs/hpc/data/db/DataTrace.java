package edu.rice.cs.hpc.data.db;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	private final static String TRACE_NAME = "hpctoolkit trace file";

	long min_time;
	long max_time;
	long index_start;
	long index_length;
	int size_offset;
	int size_length;
	int size_time;
	int size_cctid;
	
	public DataTrace() {
	}

	@Override
	protected boolean isTypeFormatCorrect(int type) {
		return type == 2;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.compareTo(TRACE_NAME) >= 0;
	}

	@Override
	protected boolean readNext(DataInputStream input)
			throws IOException
	{
		min_time = input.readLong();
		max_time = input.readLong();
		
		index_start = input.readLong();
		index_length = input.readLong();
		
		size_offset = input.readInt();
		size_length = input.readInt();
		size_time   = input.readInt();
		size_cctid  = input.readInt();
		
		return true;
	}

	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		out.println("Min time: " + min_time);
		out.println("Max time: " + max_time);
		
		out.println("index start: " + index_start);
		out.println("index length: " + index_length);
		
		out.println("size offset: " + size_offset);
		out.println("size length: " + size_length);
		out.println("size time: " + size_time);
		out.println("size cctid: " + size_cctid);
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
			trace_data.open("/Users/laksonoadhianto/work/data/new-prof/database-mpi-newdb/trace.db");			
			trace_data.printInfo(System.out);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
