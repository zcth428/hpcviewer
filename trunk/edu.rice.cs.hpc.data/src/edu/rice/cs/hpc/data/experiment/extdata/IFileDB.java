package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;

import edu.rice.cs.hpc.data.util.LargeByteBuffer;

public interface IFileDB 
{
	public void		open(String filename, int headerSize, int recordSize) throws IOException;
	
	public int 		getNumberOfRanks();
	public String[]	getRankLabels();
	public long[]	getOffsets();
	
	public LargeByteBuffer getMasterBuffer();
	
	public int 		getParallelismLevel();
	public void		dispose();
}
