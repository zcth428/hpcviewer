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
	
	/**Creates an InitializeThread with SpaceTimeData _stData.*/
	public InitializeThread(SpaceTimeData _stData)
	{
		stData = _stData;
	}
	
	/******************************************************************************************************************
	 * Gets the next available file from stData, reads in the first and last timestamp and checks them in with stData,
	 * and then repeats the process if there are more files.
	 *****************************************************************************************************************/
	public void run()
	{
		File nextFile = stData.getNextFile();
		long firstTime;
		long lastTime;
		try
		{
			while(nextFile != null)
			{
				//long programTime = System.currentTimeMillis();
				ByteBuffer b = ByteBuffer.allocateDirect(8);
				ByteBuffer b2 = ByteBuffer.allocateDirect(8);
				RandomAccessFile in = new RandomAccessFile(nextFile, "r");
				//System.out.println("Made bytebuffer "+(System.currentTimeMillis()-programTime));
				
				in.seek(ProcessTimeline.SIZE_OF_HEADER);
				FileChannel f = in.getChannel();
				f.read(b);
				b.flip();
				firstTime = b.getLong();
				//System.out.println("Read first timestamp "+(System.currentTimeMillis()-programTime));
				
				in.seek(nextFile.length() - ProcessTimeline.SIZE_OF_TRACE_RECORD);
				f = in.getChannel();
				f.read(b2);
				b2.flip();
				lastTime = b2.getLong();
				//System.out.println("Read second timestamp "+(System.currentTimeMillis()-programTime));
				
				stData.checkIn(firstTime, lastTime);
				nextFile = stData.getNextFile();
				//System.out.println(firstTime+" "+lastTime);
				f.close();
				in.close();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
