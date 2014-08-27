package edu.rice.cs.hpc.data.db;

import java.io.DataInputStream;
import java.io.IOException;

public class DataSummary extends DataCommon 
{
	private final static String SUMMARY_NAME = "hpctoolkit summary file";

	@Override
	protected boolean isTypeFormatCorrect(int type) {
		return type==1;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.startsWith(SUMMARY_NAME);
	}

	@Override
	protected boolean readNext(DataInputStream input) 
			throws IOException
	{
		// TODO Auto-generated method stub
		return false;
	}

}
