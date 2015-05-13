package edu.rice.cs.hpc.traceviewer.data.version2;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.extdata.FileDB2;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;
import edu.rice.cs.hpc.data.util.Constants;

public abstract class AbstractBaseData implements IBaseData 
{
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
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getLong(long)
	 */
	@Override
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
	@Override
	public int getInt(long position) {
		try {
			return baseDataFile.getMasterBuffer().getInt(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}


	@Override
	public boolean isHybridRank() {
		return baseDataFile.getParallelismLevel() > 1;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#dispose()
	 */
	@Override
	public void dispose() {
		this.baseDataFile.dispose();
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getRecordSize()
	 */
	@Override
	public int getRecordSize() {
		return Constants.SIZEOF_INT + Constants.SIZEOF_LONG;
	}

}
