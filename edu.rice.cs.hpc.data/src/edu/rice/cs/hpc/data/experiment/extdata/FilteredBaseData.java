package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.util.ArrayList;

public class FilteredBaseData implements IBaseData {

	final private BaseDataFile baseDataFile;
	private ArrayList<String> listOfGlobs;
	private ArrayList<Integer> indexes;
	private String []filteredRanks;
	private boolean isShownMode = false;
	final int headerSize;

	public FilteredBaseData(String filename, int headerSize) throws IOException 
	{
		baseDataFile = new BaseDataFile(filename);
		this.headerSize = headerSize;
	}

	/****
	 * start to filter the ranks
	 * @param glob : pattern to filter the ranks
	 */
	public void filter() {
		if (baseDataFile == null)
			throw new RuntimeException("Fata error: cannot find data.");
		
		String data[] = baseDataFile.getValuesX();
		int initialSize = (int) (data.length*.2);
		filteredRanks = null;
		indexes = new ArrayList<Integer>( initialSize );
		
		for (String glob: listOfGlobs) {
			String globPattern = glob.replace("*", ".*").replace("?",".?");
			for (int i=0; i<data.length; i++) {
				
				String item = data[i];
				boolean isMatched = item.matches(globPattern);
				
				if ( (isMatched && isShownMode) || (!isShownMode && !isMatched)) {
					indexes.add(i);
				}
			}
		}
	}
	
	/****
	 * set oatterns to filter ranks
	 * @param filters
	 */
	public void setFilters(ArrayList<String> filters) {
		listOfGlobs = filters;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getListOfRanks()
	 */
	public String[] getListOfRanks() {
		if (filteredRanks == null) {
			filteredRanks = new String[indexes.size()];
			int i = 0;
			final String ranks[] = baseDataFile.getValuesX();
			
			// fill the filtered ranks
			for(Integer index: indexes) {
				filteredRanks[i] = ranks[index];
			}
		}
		return filteredRanks;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return indexes.size();
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
	public long getMaxLoc(int rank) {
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (rank+1<baseDataFile.getNumberOfFiles())? 
				offsets[rank+1] : baseDataFile.getMasterBuffer().size()-1 )
				- SIZE_OF_TRACE_RECORD;
		return maxloc;
	}

}
