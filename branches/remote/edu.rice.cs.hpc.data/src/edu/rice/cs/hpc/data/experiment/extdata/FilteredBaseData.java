package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.util.ArrayList;


/******************************************************************
 * 
 * Filtered version for accessing a raw data file
 * A data file can either thread level data metric, or trace data
 * @see IBaseData
 * @see AbstractBaseData
 * @see BaseDataFile
 *******************************************************************/
public class FilteredBaseData extends AbstractBaseData implements IFilteredData {

	private FilterSet filter;
	private String []filteredRanks;
	private int []indexes;

	/*****
	 * construct a filtered data
	 * The user is responsible to make sure the filter has been set with setFilters()
	 * 
	 * @param filename
	 * @param headerSize
	 * @throws IOException
	 */
	public FilteredBaseData(String filename, int headerSize, int recordSz) throws IOException 
	{
		super( filename, headerSize, recordSz);
		filter = new FilterSet();
	}

	/****
	 * start to filter the ranks
	 */
	private void applyFilter() {
		if (baseDataFile == null)
			throw new RuntimeException("Fatal error: cannot find data.");
		
		String data[] = baseDataFile.getValuesX();

		filteredRanks = null;

		ArrayList<Integer> lindexes = new ArrayList<Integer>();

		if (filter.hasAnyFilters()) {
			for (int i = 0; i < data.length; i++) {
				if (filter.includes(data[i]))
					lindexes.add(i);
			}
			//Convert ArrayList to array
			indexes = new int[lindexes.size()];
			for (int i = 0; i < indexes.length; i++) {
				indexes[i] = lindexes.get(i);
			}
		} else {
			// no glob pattern to filter
			// warning: not optimized code
			indexes = new int[data.length];
			for(int i=0; i<data.length; i++) {
				indexes[i] = i;
			}
		}
	}
	
	/****
	 * set oatterns to filter ranks
	 * @param filters
	 */
	public void setFilter(FilterSet filter) {
		this.filter = filter;
		applyFilter();
	}
	
	
	public FilterSet getFilter() {
		return filter;
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getListOfRanks()
	 */
	public String[] getListOfRanks() {
		if (filteredRanks == null) {
			filteredRanks = new String[indexes.length];
			final String ranks[] = baseDataFile.getValuesX();
			
			for(int i=0; i<indexes.length; i++) {
				filteredRanks[i] = ranks[indexes[i]];
			}
		}
		return filteredRanks;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return indexes.length;
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMinLoc(int)
	 */
	public long getMinLoc(int rank) {
		int filteredRank = indexes[rank];
		final long offsets[] = baseDataFile.getOffsets();
		return offsets[filteredRank] + headerSize;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMaxLoc(int)
	 */
	public long getMaxLoc(int rank, int recordSize) {
		int filteredRank = indexes[rank];
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (filteredRank+1<baseDataFile.getNumberOfFiles())? 
				offsets[filteredRank+1] : baseDataFile.getMasterBuffer().size()-SIZE_OF_END_OF_FILE_MARKER )
				- recordSize;
		return maxloc;
	}

	@Override
	public boolean isGoodFilter() {
		return getNumberOfRanks() > 0;
	}

	@Override
	public int getFirstIncluded() {
		return indexes[0];
	}

	@Override
	public int getLastIncluded() {
		return indexes[indexes.length-1];
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		return indexes[indexes.length-1]-indexes[0] == indexes.length-1;
	}

	@Override
	public boolean isHybridRank() {
		return baseDataFile.isHybrid();
	}

}
