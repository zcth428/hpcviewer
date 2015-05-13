package edu.rice.cs.hpc.traceviewer.data.version3;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;

abstract public class AbstractBaseData implements IBaseData 
{
	final private DataTrace data;
	
	public AbstractBaseData(DataTrace data)
	{
		this.data = data;
	}


	@Override
	public String[] getListOfRanks() {

		return null;
	}

	@Override
	public int getNumberOfRanks() {
		return data.getNumberOfRanks();
	}

	@Override
	public boolean isHybridRank() {
		return true;
	}

	@Override
	public void dispose() {
		try {
			data.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public long getLong(long position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(long position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRecordSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMinLoc(int rank) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMaxLoc(int rank) {
		// TODO Auto-generated method stub
		return 0;
	}

}
