package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;

public abstract class AbstractBaseData implements IBaseData {
	protected static final int SIZE_OF_END_OF_FILE_MARKER = 4;
	protected IFileDB baseDataFile;
	final int headerSize;

	public AbstractBaseData(String filename, int headerSize, int recordSz) throws IOException {
		baseDataFile = new FileDB2();
		baseDataFile.open(filename, headerSize, recordSz);
		this.headerSize = headerSize;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getOffsets()
	 */
	public long[] getOffsets() {
		return baseDataFile.getOffsets();
	}

	/***
	 * retrieve the start location of a rank in a database
	 * @param rank
	 * @return
	 */
	public abstract long getMinLoc(int rank);
	
	/****
	 * retrieve the end of file location of a rank
	 * @param rank
	 * @return
	 */
	public abstract long getMaxLoc(int rank, int recordSize);
	


	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getString(long)
	 */
	public String getString(long position, long length) {
		try {
			return baseDataFile.getMasterBuffer().getString(position, length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getLong(long)
	 */
	public long getLong(long position) {
		try {
			return baseDataFile.getMasterBuffer().getLong(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getInt(long)
	 */
	public int getInt(long position) {
		try {
			return baseDataFile.getMasterBuffer().getInt(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getDouble(long)
	 */
	public double getDouble(long position) {
		try {
			return baseDataFile.getMasterBuffer().getDouble(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getHeaderSize()
	 */
	public int getHeaderSize() {
		return headerSize;
	}


	@Override
	public boolean isHybridRank() {
		return baseDataFile.getParallelismLevel() > 1;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#dispose()
	 */
	public void dispose() {
		this.baseDataFile.dispose();
	}
}
