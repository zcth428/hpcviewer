package edu.rice.cs.hpc.traceviewer.data.version3;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.hpc.data.db.DataThread;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;

public class FileDB3 implements IFileDB 
{
	DataTrace dataTrace;
	DataThread dataThread;
	
	public FileDB3()
	{
		
	}
	
	@Override
	@Deprecated
	public void open(String filename, int headerSize, int recordSize)
			throws IOException 
	{
	}
	
	public void open(String directory) throws IOException
	{
		String filename = directory + File.separatorChar + "trace.db";
		dataTrace = new DataTrace();
		dataTrace.open(filename);
		
		filename = directory + File.separatorChar + "threads.db";
		dataThread = new DataThread();
		dataThread.open(filename);
	}

	@Override
	public int getNumberOfRanks() {
		return dataTrace.getNumberOfRanks();
	}

	@Override
	public String[] getRankLabels() {
		int numLevels = dataThread.getParallelismLevel();
		int []ranks   = dataThread.getParallelismRank();
		int num_ranks  = ranks.length / numLevels;
		String []rank_label = new String[num_ranks]; 

		StringBuffer sbRank = new StringBuffer();
		for (int i=0; i<num_ranks; i++)
		{
			for(int j=0; j<numLevels; j++)
			{
				sbRank.append( String.valueOf(ranks[i]) );
				if (j == numLevels-1)
				{
					rank_label[i] = sbRank.toString();
				} else
				{
					sbRank.append(".");
				}
			}
		}
		return rank_label;
	}

	@Override
	public long[] getOffsets() {
		return dataTrace.getOffsets();
	}

	@Override
	public long getLong(long position) throws IOException {

		return dataTrace.getLong(position);
	}

	@Override
	public int getInt(long position) throws IOException {
		return dataTrace.getInt(position);
	}

	@Override
	public double getDouble(long position) throws IOException {
		return dataTrace.getDouble(position);
	}

	@Override
	public int getParallelismLevel() {
		return dataThread.getParallelismLevel();
	}

	@Override
	public long getMinLoc(int rank) {
		long []offsets = dataTrace.getOffsets();
		return offsets[rank];
	}

	@Override
	public long getMaxLoc(int rank) {
		return getMinLoc(rank + 1);
	}

	@Override
	public void dispose() {
		try {
			dataTrace.dispose();
			dataThread.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
