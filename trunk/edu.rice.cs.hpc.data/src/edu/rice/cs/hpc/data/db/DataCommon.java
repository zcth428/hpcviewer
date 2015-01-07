package edu.rice.cs.hpc.data.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

abstract public class DataCommon 
{
	private final static int MESSAGE_SIZE = 32;
	private final static int MAGIC = 0x06870630;
	
	protected long format;
	protected long num_threads;
	protected long num_cctid;
	protected long num_metric;
	
	protected String filename;
	
	public void open(final String file) 
			throws IOException
	{
		filename = file;
		
		final FileInputStream fis = new FileInputStream(filename);
		final FileChannel channel  = fis.getChannel();
		
		ByteBuffer buffer = ByteBuffer.allocate(256);
		int numBytes      = channel.read(buffer);
		if (numBytes > 0) 
		{
			// -------------------------------------------------------------
			// get the message header file
			// -------------------------------------------------------------
			final byte []bytes = new byte[MESSAGE_SIZE]; 
			buffer.flip();
			buffer.get(bytes, 0, MESSAGE_SIZE);

			// -------------------------------------------------------------
			// get the magic number
			// -------------------------------------------------------------
			long magic_number = buffer.getLong();
			if (magic_number != MAGIC)
			{
				throw_exception(fis, "Magic number is incorrect: " + magic_number);
			}
			
			// -------------------------------------------------------------
			// check the version
			// -------------------------------------------------------------
			final long version = buffer.getLong();
			if (version < 3)
			{
				throw_exception(fis, "Incorrect file version: " + version);
			}

			// -------------------------------------------------------------
			// check the type
			// -------------------------------------------------------------
			final long type = buffer.getLong();			
			if (!isTypeFormatCorrect(type))
			{
				throw_exception(fis, file + " has inconsistent type " + type);
			}
			
			// -------------------------------------------------------------
			// check the format
			// -------------------------------------------------------------
			// to be ignored at the moment
			format = buffer.getLong();
			
			// -------------------------------------------------------------
			// read number of cct
			// -------------------------------------------------------------
			num_cctid = buffer.getLong();
			
			// -------------------------------------------------------------
			// read number of metrics
			// -------------------------------------------------------------
			num_metric = buffer.getLong();
			
			// -------------------------------------------------------------
			// read number of threads
			// -------------------------------------------------------------
			num_threads = buffer.getLong();
			
			if (num_threads <= 0 || num_cctid <= 0 || num_metric <=0) 
			{
				// warning: empty database.
				// this doesn't mean the file is invalid, but just anomaly
			}
			
			// -------------------------------------------------------------
			// Read the next header (if any)
			// -------------------------------------------------------------
			if ( readNext(channel) ) 
			{
				// the implementer can perform other operations
			}

		}

		channel.close();
		fis.close();
	}
	
	public void printInfo( PrintStream out)
	{
		out.println("Format: "      + format);
		out.println("Num threads: " + num_threads);
		out.println("Num cctid: "   + num_cctid);
		out.println("Num metric: "  + num_metric);
	}
	
	/*******
	 * function to close the input stream and thrown an  IO exception
	 * 
	 * @param input : the io to be closed
	 * @param message : message to be thrown
	 * 
	 * @throws IOException
	 */
	private void throw_exception(FileInputStream input, String message)
			throws IOException
	{
		input.close();
		throw new IOException(message);
	}
	
	protected abstract boolean isTypeFormatCorrect(long type);
	protected abstract boolean isFileHeaderCorrect(String header);
	protected abstract boolean readNext(FileChannel input) throws IOException;
}
