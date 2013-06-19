package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;


/******************************************************************
 * 
 * Filtered version for accessing a raw data file
 * A data file can either thread level data metric, or trace data
 * @see IBaseData
 * @see AbstractBaseData
 * @see BaseDataFile
 *******************************************************************/
public class FilteredBaseData extends AbstractBaseData {

	private static final int SIZE_OF_END_OF_FILE_MARKER = 4;//The end of file marker is 0xdeadfood
	private Filter filter;
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
		ArrayList<String> listOfGlobs = filter.getPatterns();
		boolean isShownMode = filter.isShownMode();
		
		if (listOfGlobs != null && listOfGlobs.size()>0) {
			// ------------------------------------------------------------------
			// search for the matching string
			// this is not an optimized version O(n^2), but at the moment it works
			// ------------------------------------------------------------------
			for (int k=0; k<listOfGlobs.size(); k++) {
				String glob = listOfGlobs.get(k);
				//Compiling outside the loop gives a performance increase
				String globPatternString = glob.replace("*", ".*").replace("?",".?");
				Pattern compGlob = Pattern.compile(globPatternString);
				
				int j=0;
				for (int i=0; i<data.length; i++) {
					
					String item = data[i];
					boolean isMatched = compGlob.matcher(item).matches();
					
					//-----------------------------------------------------------------------
					// for show mode: for every glob, we add everything that matches
					// for hide mode: we add everything that doesn't match. 
					//					for the second glob, we remove everything that match
					//-----------------------------------------------------------------------
					if (isShownMode) {
						if (isMatched) {
							mapIndex.put(i, j);
						}
					} else {
						//if we have multiple glob pattern, we need to match with the existing
						//	filtered ranks
						if ( k==0 || (k>0 && mapIndex.containsKey(i)) ) {
							// Needs to remove duplicates
							if ( (isMatched && isShownMode) || (!isShownMode && !isMatched)) {
								mapIndex.put(i, j);
								j++;
							} else {
								// remove from the existing un-filtered list
								mapIndex.remove(i);
							}
						}
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
	public void setFilter(Filter filter) {
		this.filter = filter;
		filter();
	}
	
	
	public Filter getFilter() {
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

}
