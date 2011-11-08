package edu.rice.cs.hpc.data.util;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

/**The idea for this class is credited to Stu Thompson.
 * stackoverflow.com/questions/736556/binary-search-in-a-sorted-memory-mapped-file-in-java
 * 
 * The implementation is credited to
 * @author Michael
 * @author Reed
 */
public class LargeByteBuffer
{
	
	/**The masterBuffer holds a vector of all bytebuffers*/
	private Vector<MappedByteBuffer> masterBuffer;
	
	private long length;
	
	private static final int PAGE_SIZE = Integer.MAX_VALUE;
	
	public LargeByteBuffer(FileChannel in)
		throws IOException
	{
		masterBuffer = new Vector<MappedByteBuffer>();
		long start = 0;
		long currentSize = 0;
		length = 0;
		for (long index = 0; length < in.size(); index++)
		{
			if ((in.size()/PAGE_SIZE) == index)
			{
				currentSize = (in.size() - index*PAGE_SIZE);
			}
			else
			{
				currentSize = PAGE_SIZE;
			}
			start = index*PAGE_SIZE;
			masterBuffer.add(in.map(FileChannel.MapMode.READ_ONLY, start, currentSize));
			length += currentSize;
		}
	}
	
	public int get(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).get(loc);
	}
	
	public int getInt(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getInt(loc);
	}
	
	public long getLong(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getLong(loc);
	}
	
	public double getDouble(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getDouble(loc);
	}
	
	public char getChar(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getChar(loc);
	}
	
	public float getFloat(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getFloat(loc);
	}
	
	public short getShort(long position)
	{
		int page = (int) (position / PAGE_SIZE);
		int loc = (int) (position % PAGE_SIZE);
		return masterBuffer.get(page).getShort(loc);
	}
	
	public long size()
	{
		return length;
	}
}