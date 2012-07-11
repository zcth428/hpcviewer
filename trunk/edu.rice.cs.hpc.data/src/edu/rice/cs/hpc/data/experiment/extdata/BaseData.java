package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;

/*******
 * 
 * basic implementation of IBaseData
 *
 */
public class BaseData implements IBaseData {

	final private BaseDataFile baseDataFile;
	final int headerSize;
	
	public BaseData(String filename, int headerSize) throws IOException 
	{
		baseDataFile = new BaseDataFile(filename);
		this.headerSize = headerSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getHeaderSize()
	 */
	public int getHeaderSize() {
		return headerSize;
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
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getOffsets()
	 */
	public long[] getOffsets() {
		return baseDataFile.getOffsets();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getString(long)
	 */
	public String getString(long position, long length) {
		return baseDataFile.getMasterBuffer().getString(position, length);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getLong(long)
	 */
	public long getLong(long position) {
		return baseDataFile.getMasterBuffer().getLong(position);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getInt(long)
	 */
	public int getInt(long position) {
		return baseDataFile.getMasterBuffer().getInt(position);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getDouble(long)
	 */
	public double getDouble(long position) {
		return baseDataFile.getMasterBuffer().getDouble(position);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMinLoc(int)
	 */
	public long getMinLoc(int rank) {
		final long offsets[] = baseDataFile.getOffsets();
		return offsets[rank] + headerSize;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMaxLoc(int)
	 */
	public long getMaxLoc(int rank, int recordSize) {
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (rank+1<baseDataFile.getNumberOfFiles())? 
				offsets[rank+1] : baseDataFile.getMasterBuffer().size()-1 )
				- recordSize;
		return maxloc;
	}

}
