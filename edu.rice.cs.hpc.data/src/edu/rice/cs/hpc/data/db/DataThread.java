package edu.rice.cs.hpc.data.db;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import edu.rice.cs.hpc.data.util.Constants;


/**********************************************************************
 * 
 * Class for reading threads ID database
 *
 **********************************************************************/
public class DataThread extends DataCommon 
{
	final static private String THREADS_NAME = "hpctoolkit thread index";
	final static private int MESSAGE_SIZE	 = 32;
	
	private long string_start, string_length;
	private long index_start,  index_length;
	private int  num_fields;
	private int  size_string, size_field;
	
	private String []message_title;
	private int []ranks;

	@Override
	public void open(final String file) throws IOException
	{
		super.open(file);
		
		RandomAccessFile file_to_read = new RandomAccessFile(filename, "r");		
		fillOffsetTable(file_to_read);
		file_to_read.close();
	}
	
	public int getParallelismLevel()
	{
		return num_fields;
	}
	
	public String[] getParallelismTitle()
	{
		return message_title;
	}
	
	
	public int[] getParallelismRank()
	{
		return ranks;
	}
	
	@Override
	protected boolean isTypeFormatCorrect(long type) {
		return type == 4;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return THREADS_NAME.equals(header);
	}

	@Override
	protected boolean readNextHeader(FileChannel input) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(256);
		int numBytes      = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			
			string_start	= buffer.getLong();
			string_length	= buffer.getLong();
			
			index_start		= buffer.getLong();
			index_length	= buffer.getLong();
			
			num_fields		= buffer.getInt();
			size_string		= buffer.getInt();
			size_field		= buffer.getInt();
		}
		return true;
	}

	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		
		out.println("\nString start: " + string_start);
		out.println("String length: " + string_length);
		out.println("Index start: " + index_start);
		out.println("Index length: " + index_length);
		out.println("Num fields: " + num_fields);
		out.println("size string: " + size_string);
		out.println("Size fields: " + size_field);
		
		out.println("\nTitle:");
		if (message_title != null)
			for(String title: this.message_title)
			{
				out.println("\t" + title);
			}
		out.println("\nRanks:");
		if (ranks != null)
			for(int rank : ranks)
			{
				out.println("\t" + rank);
			}
	}
	

	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------
	

	private void fillOffsetTable(RandomAccessFile file) throws IOException
	{
		file.seek(string_start);
		byte []buffer = new byte[MESSAGE_SIZE];
		
		int num_titles = (int) (string_length / MESSAGE_SIZE);
		message_title 		   = new String[num_titles];
		
		for (int i=0; i<num_titles; i++)
		{
			file.read(buffer);
			message_title[i] = new String(buffer);
		}
		int num_ranks = (int) (index_length / Constants.SIZEOF_INT);
		ranks = new int[num_ranks];
		
		final FileChannel channel = file.getChannel();
		final MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, index_start, index_length);
		final IntBuffer intBuffer = mappedBuffer.asIntBuffer();
		for (int i=0; i<num_ranks; i++)
		{
			ranks[i] = intBuffer.get(i);
		}
		channel.close();
	}

	static public void main(String []main)
	{
		DataThread data = new DataThread();
		
		try {
			data.open("/home/la5/data/new-database/db-lulesh-new/threads.db");
			data.printInfo(System.out);
			data.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
