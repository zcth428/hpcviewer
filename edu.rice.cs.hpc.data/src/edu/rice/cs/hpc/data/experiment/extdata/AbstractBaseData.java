package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;

public abstract class AbstractBaseData implements IBaseData {
	protected BaseDataFile baseDataFile;
	final int headerSize;

	public AbstractBaseData(String filename, int headerSize) throws IOException {
		baseDataFile = new BaseDataFile(filename);
		this.headerSize = headerSize;
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
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getHeaderSize()
	 */
	public int getHeaderSize() {
		return headerSize;
	}

}
