package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
/************************************************************************
 * A thread that gets first and last timestamps from the files in a SpaceTimeData
 * and checks them into SpaceTimeData.
 * @author Reed Landrum
 ***********************************************************************/
public class InitializeThread extends Thread 
{
	/**The SpaceTimeData this thread will add its line data and images to.*/
	SpaceTimeData stData;
	
	/**The file that contains all the data for everything*/
	File traceFile;
	
	/**Creates an InitializeThread with SpaceTimeData _stData*/
	public InitializeThread(SpaceTimeData _stData)
	{
		System.out.println("initialize?");
		stData = _stData;
		traceFile = stData.getTraceFile();
	}
	
	/******************************************************************************************************************
	 * Gets the next available file from stData, reads in the first and last timestamp and checks them in with stData,
	 * and then repeats the process if there are more files.
	 *****************************************************************************************************************/
	public void run()
	{
		System.out.println("GOING NOW!!!!");
		RandomAccessFile in = null;
		FileChannel f = null;
		try
		{
			try
			{
				in = new RandomAccessFile(traceFile, "r");
				f = in.getChannel();
				ByteBuffer b = ByteBuffer.allocateDirect(stData.getHeight()*8);
				
				in.seek(ProcessTimeline.SIZE_OF_MASTER_HEADER);
				f.read(b);
				b.flip();
				long offsetToLast = b.getLong();
				
				//reads through the index of .megatrace file to find first locations of individual traces
				for(int x = 0; x <= stData.getHeight(); x++)
				{
					ByteBuffer b2 = ByteBuffer.allocateDirect(8);
					
					long offsetToFirst = offsetToLast;
					in.seek(offsetToFirst);
					f.read(b2);
					b2.flip();
					long firstTime = b2.getLong();
					
					offsetToLast = b.getLong();
					in.seek(offsetToLast - ProcessTimeline.SIZE_OF_TRACE_RECORD);
					//do we have to clear b2 here?
					f.read(b2);
					b2.flip();
					long lastTime = b2.getLong();
					
					stData.checkIn(firstTime, lastTime);
					
					System.out.println(firstTime+" "+lastTime);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			try
			{
				f.close();
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
