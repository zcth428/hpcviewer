package edu.rice.cs.hpc.traceviewer.data.version2;

import java.io.IOException;


/*******
 * 
 * basic implementation of IBaseData
 *
 */
public class BaseData extends AbstractBaseData {
	
	public BaseData(String filename, int headerSize, int recordSz) throws IOException 
	{
		super(filename, headerSize, recordSz);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getListOfRanks()
	 */
	public String[] getListOfRanks() {
		return baseDataFile.getRankLabels();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return baseDataFile.getNumberOfRanks();
	}
	

	@Override
	public long getMinLoc(int rank) {
		final long offsets[] = baseDataFile.getOffsets();
		return offsets[rank] + headerSize;
	}

	@Override
	public long getMaxLoc(int rank) {
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (rank+1<baseDataFile.getNumberOfRanks())? 
				offsets[rank+1] : baseDataFile.getMasterBuffer().size()-1 )
				- getRecordSize();
		return maxloc;
	}

	@Override
	public int getFirstIncluded() {
		return 0;
	}

	@Override
	public int getLastIncluded() {
		return baseDataFile.getNumberOfRanks()-1;
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		return true;//No filtering
	}
}
