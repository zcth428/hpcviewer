package edu.rice.cs.hpc.traceviewer.util;

public class Debugger {
	
	final static int MAX_TRACES = 8;
	final static int MIN_TRACES = 2;
	
	static public void printTrace(String str) {
		System.out.print(str + ":\t");
		Throwable t = new Throwable();
		StackTraceElement traces[] = t.getStackTrace();
		for (int i=MIN_TRACES; i<MAX_TRACES; i++) {
			System.out.println("\t" + traces[i]);
		}
	}
}
