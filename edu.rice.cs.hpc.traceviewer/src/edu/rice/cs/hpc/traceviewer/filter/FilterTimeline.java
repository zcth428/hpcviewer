package edu.rice.cs.hpc.traceviewer.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterTimeline implements IFilterTimeline{

	private ArrayList<String> listOfGlobs;
	private boolean isShownMode ;
	
	private ArrayList<Integer> indexes;
	private ArrayList<String> filteredData;
	
	
	public void filter( String []data) {
		
		int initialSize = (int) (data.length*.2);
		filteredData = new ArrayList<String>( initialSize );
		indexes = new ArrayList<Integer>( initialSize );
		
		for (String glob: listOfGlobs) {
			String globPattern = glob.replace("*", ".*").replace("?",".?");
			for (int i=0; i<data.length; i++) {
				
				String item = data[i];
				boolean isMatched = item.matches(globPattern);
				
				if ( (isMatched && isShownMode) || (!isShownMode && !isMatched)) {
					filteredData.add(item);
					indexes.add(i);
				}
			}
		}
	}
	
	private ArrayList<Integer> getIndexes() {
		if (indexes == null) {
			throw new RuntimeException("Need to perform filtering first !");
		} else {
			return indexes;
		}
	} 
	
		
	
	public void setShownMode( boolean shown ) {
		isShownMode = shown;
	}
	
	public boolean getShownMode () {
		return isShownMode;
	}
	
	public List<String> getPatterns() {
		return listOfGlobs;
	}
	
	public void setPatterns(List<String> list) {
		listOfGlobs = (ArrayList<String>) list;
	}


	public int getLineIndex(int line, int maxLine, int numPixels, int processBegin) {
		int lineIndex = processBegin +  getRelativeIndex(line, maxLine, numPixels);
		System.out.println("FT p-b: " + processBegin + ", l: " + line + ": " + lineIndex);
		return lineIndex;
	}


	public String getLineName(int line, int maxLine, int numPixels, String []processNames) {
		int index = getRelativeIndex( line, maxLine, numPixels );
		return processNames[index];
	}
	
	
	private int getRelativeIndex(int line, int maxLine, int numPixels) {
		ArrayList<Integer> indexes = getIndexes();
		int filteredIndex = 0;
		if ( indexes.size() > numPixels ) {
			filteredIndex = indexes.get((line)*numPixels/indexes.size());
		} else {
			filteredIndex = indexes.get(line);
		}
		return filteredIndex;
	}


	public int getDistance(int processBegin, int processEnd) {
		return getIndexes().size();
	}
}
