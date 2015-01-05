package edu.rice.cs.hpc.data.db;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

abstract public class DataCommon 
{
	private final static int MESSAGE_SIZE = 32;
	private final static int MAGIC = 0x06870630;
	
	protected int format;
	protected long num_threads;
	protected long num_cctid;
	protected long num_metric;
	
	protected String filename;
	
	public void open(final String file) 
			throws IOException
	{
		filename = file;
		
		final FileInputStream fis = new FileInputStream(file);
		final DataInputStream input = new DataInputStream( fis );
		
		byte buffer[] = new byte[MESSAGE_SIZE];
		if (input.read(buffer, 0, MESSAGE_SIZE) > 0)
		{
			// -------------------------------------------------------------
			// check the message header
			// -------------------------------------------------------------
			final String message = new String(buffer);
			boolean is_correct_format = isFileHeaderCorrect(message);			
			
			if (!is_correct_format)
			{
				throw_exception(input, file + " has incorrect file header: " + message );
			}
			
			// -------------------------------------------------------------
			// check the magic number
			// -------------------------------------------------------------
			final int magic_number = input.readInt();
			if (magic_number != MAGIC) 
			{
				throw_exception(input, "Magic number is incorrect: " + magic_number);
			}
			
			// -------------------------------------------------------------
			// check the type
			// -------------------------------------------------------------
			final int type = input.readInt();
			is_correct_format = isTypeFormatCorrect(type);
			
			if (!is_correct_format)
			{
				throw_exception(input, file + " has inconsistent type " + type);
			}
			
			// -------------------------------------------------------------
			// check the format
			// -------------------------------------------------------------
			// to be ignored at the moment
			format = input.readInt();
			
			// -------------------------------------------------------------
			// read number of threads
			// -------------------------------------------------------------
			num_threads = input.readLong();
			
			// -------------------------------------------------------------
			// read number of cct
			// -------------------------------------------------------------
			num_cctid = input.readLong();
			
			// -------------------------------------------------------------
			// read number of metrics
			// -------------------------------------------------------------
			num_metric = input.readLong();
			
			if (num_threads <= 0 || num_cctid <= 0 || num_metric <=0) 
			{
				// warning: empty database.
				// this doesn't mean the file is invalid, but just anomaly
			}
			
			// -------------------------------------------------------------
			// Read the next header (if any)
			// -------------------------------------------------------------
			if ( readNext(input) ) 
			{
				// the implementer can perform other operations
			}
		}
		
		input.close();
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
	private void throw_exception(DataInputStream input, String message)
			throws IOException
	{
		input.close();
		throw new IOException(message);
	}
	
	protected abstract boolean isTypeFormatCorrect(int type);
	protected abstract boolean isFileHeaderCorrect(String header);
	protected abstract boolean readNext(DataInputStream input) throws IOException;
}
