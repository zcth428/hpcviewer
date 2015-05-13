package edu.rice.cs.hpc.traceviewer.data.version3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.db.DataCommon;
import edu.rice.cs.hpc.traceviewer.data.db.DataRecord;

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
	static final private int RECORD_INDEX_SIZE = Constants.SIZEOF_LONG + 
										Constants.SIZEOF_LONG + Constants.SIZEOF_LONG;
	static final private int RECORD_ENTRY_SIZE = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;
	
	long index_start, index_length;
	long trace_start, trace_length;
	long min_time,	  max_time;

	int  size_offset, size_length;
	int  size_gtid,	  size_time;
	int  size_cctid;
	
	private RandomAccessFile file;
	private FileChannel channel;

	private long []table_offset;
	private long []table_length;
	private long []table_global_tid;
	
	public DataTrace() {
	}

	@Override
	public void open(final String file)
			throws IOException
	{
		super.open(file);
		
		open_internal(file);
		// fill the cct offset table
		fillOffsetTable(file);
	}
	
	/***
	 * Return the lowest begin time of all ranks
	 * 
	 * @return the minimum time
	 */
	public long getMinTime()
	{
		return min_time;
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
	protected boolean readNextHeader(FileChannel input)
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
			
			// we cannot afford if the size of cct is not integer
			if (size_cctid != 4)
			{
				throw new IOException("The size of CCT is not supported: " + size_cctid);
			} 
		}
		
		return true;
	}

	/****
	 * get a trace record from a given rank and sample index
	 * 
	 * @param rank : the rank 
	 * @param index : sample index
	 * 
	 * @return DataRecord containing time and cct ID
	 * 
	 * @throws IOException
	 */
	public DataRecord getSampledData(int rank, long index) throws IOException
	{
		if (table_offset[rank] <=0)
			return null;
		
		long file_size = file.length();
		long offset = table_offset[rank] + (index * RECORD_ENTRY_SIZE);
		
		if (file_size > offset)
		{
			file.seek(offset);
			byte []buffer_byte = new byte[RECORD_ENTRY_SIZE];
			file.readFully(buffer_byte);
			ByteBuffer buffer  = ByteBuffer.wrap(buffer_byte);
			
			long time = buffer.getLong();
			int cct   = buffer.getInt();
			return new DataRecord(time, cct, 0);
		}
		return null;
	}
	
	public int getNumberOfSamples(int rank)
	{
		return (int) (table_length[rank] / RECORD_ENTRY_SIZE);
	}
	
	public int getNumberOfRanks()
	{
		return table_offset.length;
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
		
		// print the table index
		for(int i=0; i<table_offset.length; i++)
		{
			out.format(" %d. %05x : %04x\n", table_global_tid[i], table_offset[i], table_length[i]);
		}
		Random r = new Random();
		for(int i=0; i< 10; i++)
		{
			int rank = r.nextInt(getNumberOfRanks()-1);
			int numsamples = getNumberOfSamples(rank);
			int sample = r.nextInt(numsamples);
			try {
				out.format("%d:  %s\n", rank, getSampledData(rank, sample));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#dispose()
	 */
	public void dispose() throws IOException
	{
		channel.close();
		file.close();
	}
	
	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------
	
	private void open_internal(String filename) throws FileNotFoundException
	{
		file 	= new RandomAccessFile(filename, "r");
		channel = file.getChannel();
	}


	private void fillOffsetTable(final String filename)
			throws IOException
	{
		// map all the table into memory. 
		// This statement can be problematic if the offset_size is huge
		
		MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, index_start, index_length);
		LongBuffer longBuffer = mappedBuffer.asLongBuffer();
		
		final int num_pos = (int) (index_length /  RECORD_INDEX_SIZE);
		
		table_offset 	 = new long[(int) num_pos];
		table_length 	 = new long[(int) num_pos];
		table_global_tid = new long[(int) num_pos];
		
		for (int i=0; i<num_pos; i++)
		{
			int index		    = 3 * i; 
			table_offset[i] 	= longBuffer.get(index);
			table_length[i] 	= longBuffer.get(index+1);
			table_global_tid[i] = longBuffer.get(index+2);
		}
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
			trace_data.open("/home/la5/data/new-database/db-lulesh-new/trace.db");			
			trace_data.printInfo(System.out);
			trace_data.dispose();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
