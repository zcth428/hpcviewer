package edu.rice.cs.hpc.traceviewer.timeline;

public interface ITimeline {

	/****
	 * retrieve the index of a given line
	 * @param line
	 * @param maxLine
	 * @param numPixels : number of pixels
	 * 
	 * @return
	 */
	public int getLineIndex(int line, int maxLine, int numPixels, int processBegin);
	
	/******
	 * compute the maximum distance between the end and the beginning of processes
	 * 
	 * @param processBegin
	 * @param processEnd
	 * @return
	 */
	public int getDistance(int processBegin, int processEnd);
	
	/***
	 * retrieve the name of the line
	 * @param line : line relative number
	 * @param maxLine : maximum number of lines
	 * @param numPixels : number of pixels
	 * 
	 * @return
	 */
	public String getLineName(int line, int maxLine, int numPixels, String []processNames);
}
