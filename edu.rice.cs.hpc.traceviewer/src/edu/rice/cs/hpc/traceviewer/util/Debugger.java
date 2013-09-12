package edu.rice.cs.hpc.traceviewer.util;

public class Debugger {
	
	final static int MAX_TRACES = 14;
	final static int MIN_TRACES = 1;
	
	final static private String MSG_HEADER = "[hpctr] ";
	static private int debugLevel = 0;
	
	/***
	 * print the call stack debugging info
	 * 
	 * @param str
	 */
	static public void printTrace(String str) {
		System.out.print(str + ":\t");
		Throwable t = new Throwable();
		StackTraceElement traces[] = t.getStackTrace();
		for (int i=MIN_TRACES; i<MAX_TRACES; i++) {
			System.out.println("\t" + traces[i]);
		}
	}
	
	/***
	 * set a new debug level
	 * 
	 * @param level
	 */
	static public void setDebugLevel(int level) {
		debugLevel = level;
		System.out.println(MSG_HEADER +"Set debug level: " + level);
	}
	
	/***
	 * return the current debug level (set by user)
	 * @return
	 */
	static public int getDebugLevel() {
		return debugLevel;
	}
	
	/**
	 * check the command line argument if it contains debugging info
	 * 
	 * @param args
	 */
	static public void checkArgDebug(String args[]) {
		boolean debugFlag = false;
		
		for (String arg: args) {
			if (!debugFlag && arg.compareTo("-g")==0) {
				debugFlag = true;
			} else if (debugFlag) {
				int level = Integer.valueOf(arg);
				setDebugLevel( level );
			}
		}
	}
	
	/***
	 * synchronously output the debugging message if it exceeds certain level
	 * 
	 * @param level: minimum debug level 
	 * @param msg
	 */
	static public void printDebug( int level, String msg ) {
		if (debugLevel > level)
			System.out.println( MSG_HEADER + msg);
	}
}
