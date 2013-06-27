package edu.rice.cs.hpc.data.experiment.extdata;

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
		return baseDataFile.getValuesX();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return baseDataFile.getNumberOfFiles();
	}
	

	@Override
	public long getMinLoc(int rank) {
		final long offsets[] = baseDataFile.getOffsets();
		return offsets[rank] + headerSize;
	}

	@Override
	public long getMaxLoc(int rank, int recordSize) {
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (rank+1<baseDataFile.getNumberOfFiles())? 
				offsets[rank+1] : baseDataFile.getMasterBuffer().size()-1 )
				- recordSize;
		return maxloc;
	}

	@Override
	public int getFirstIncluded() {
		return 0;
	}

	@Override
	public int getLastIncluded() {
		return baseDataFile.getNumberOfFiles();
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		return true;//No filtering
	}
	

}
