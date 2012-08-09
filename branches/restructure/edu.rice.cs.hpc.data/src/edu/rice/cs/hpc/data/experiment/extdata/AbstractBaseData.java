package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;

public abstract class AbstractBaseData implements IBaseData {
	protected BaseDataFile baseDataFile;
	final int headerSize;

	public AbstractBaseData(String filename, int headerSize, int recordSz) throws IOException {
		baseDataFile = new BaseDataFile(filename, headerSize, recordSz);
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

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#dispose()
	 */
	public void dispose() {
		this.baseDataFile.dispose();
	}
}
