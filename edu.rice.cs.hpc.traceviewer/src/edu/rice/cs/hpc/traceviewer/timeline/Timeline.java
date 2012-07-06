package edu.rice.cs.hpc.traceviewer.timeline;

public class Timeline implements ITimeline {
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.timeline.ILine#getLineIndex(int, int, int)
	 */
	public int getLineIndex(int line, int maxLine, int numPixels, int processBegin) {
		int lineIndex = processBegin + getRelativeIndex(line, maxLine, numPixels); 
		System.out.println("Tl " + line + ": " + lineIndex);
		return lineIndex;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.timeline.ILine#getLineName(int, int, int)
	 */
	public String getLineName(int line, int maxLine, int numPixels, String []processNames) {
		int relIndex = getRelativeIndex(line, maxLine, numPixels);
		return processNames[relIndex];
	}

	
	private int getRelativeIndex(int line, int maxLine, int numPixels) {
		if(maxLine > numPixels)
			return (line * maxLine)/(numPixels);
		else
			return line;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.traceviewer.timeline.ILine#getLength(int, int)
	 */
	public int getDistance(int processBegin, int processEnd) {
		return (processEnd - processBegin);
	}
}
