package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/******************************************************************
 * 
 * Filtered version for accessing a raw data file
 * A data file can either thread level data metric, or trace data
 * @see IBaseData
 * @see BaseDataFile
 *******************************************************************/
public class FilteredBaseData implements IBaseData {

	final private BaseDataFile baseDataFile;
	private ArrayList<String> listOfGlobs;
	private int []indexes;
	private String []filteredRanks;
	private boolean isShownMode = false;
	final int headerSize;

	/*****
	 * construct a filtered data
	 * The user is responsible to make sure the filter has been set with setFilters()
	 * 
	 * @param filename
	 * @param headerSize
	 * @throws IOException
	 */
	public FilteredBaseData(String filename, int headerSize) throws IOException 
	{
		baseDataFile = new BaseDataFile(filename);
		this.headerSize = headerSize;
	}

	/****
	 * start to filter the ranks
	 * @param glob : pattern to filter the ranks
	 */
	private void filter() {
		if (baseDataFile == null)
			throw new RuntimeException("Fata error: cannot find data.");
		
		String data[] = baseDataFile.getValuesX();

		filteredRanks = null;
		TreeMap<Integer,Integer> mapIndex = new TreeMap<Integer,Integer>( );
		
		if (listOfGlobs != null && listOfGlobs.size()>0) {
			// ------------------------------------------------------------------
			// search for the matching string
			// this is not an optimized version O(n^2), but at the moment it works
			// ------------------------------------------------------------------
			for (String glob: listOfGlobs) {
				String globPattern = glob.replace("*", ".*").replace("?",".?");
				int j=0;
				for (int i=0; i<data.length; i++) {
					
					String item = data[i];
					boolean isMatched = item.matches(globPattern);
					
					// Needs to remove duplicates
					if ( (isMatched && isShownMode) || (!isShownMode && !isMatched)) {
						mapIndex.put(i, j);
						j++;
					}
				}
			}
			// ------------------------------------------------------------------
			// flat tree map into an array for ease of access
			// ------------------------------------------------------------------
			indexes = new int[mapIndex.size()];
			Set<Entry<Integer,Integer>> set = mapIndex.entrySet();
			int i=0;
			for (Iterator<Entry<Integer, Integer>> iterator = set.iterator(); iterator.hasNext();)
			{
				Entry<Integer,Integer> entry = iterator.next();
				int key = entry.getKey();
				indexes[i] = key;
				i++;
			}			
		} else {
			// no glob pattern to filter
			// not optimized
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
	public void setFilters(ArrayList<String> filters) {
		listOfGlobs = filters;
		filter();
	}
	
	
	public ArrayList<String> getFilters() {
		return listOfGlobs;
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
		int filteredRank = indexes[rank];
		final long offsets[] = baseDataFile.getOffsets();
		return offsets[filteredRank] + headerSize;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMaxLoc(int)
	 */
	public long getMaxLoc(int rank) {
		int filteredRank = indexes[rank];
		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (filteredRank+1<baseDataFile.getNumberOfFiles())? 
				offsets[filteredRank+1] : baseDataFile.getMasterBuffer().size()-1 )
				- SIZE_OF_TRACE_RECORD;
		return maxloc;
	}

}
