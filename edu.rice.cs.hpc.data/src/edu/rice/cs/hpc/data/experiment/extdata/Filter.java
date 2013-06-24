package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.DataOutputStream;
import java.io.IOException;


public class Filter {
	private static final String PROCESS_THREAD_SEPARATOR = "$";
	private final Range process;
	private final Range thread;
	
	public Filter(String strForm){
		String[] pieces = format(strForm).split("\\"+PROCESS_THREAD_SEPARATOR);
		process = new Range (pieces[0]);
		thread = new Range(pieces[1]);

	}
	
	/**
	 * Yeah, this is kind of obnoxious, but it's because Java doesn't include
	 * blank strings in the split, even though they should be there..
	 */
	private String format(String strForm) {
		String fixing = strForm;
		if (fixing.startsWith(PROCESS_THREAD_SEPARATOR) || fixing.startsWith(":")) 
			fixing = "*" + fixing;
		if (!fixing.contains(PROCESS_THREAD_SEPARATOR))
			fixing = fixing + PROCESS_THREAD_SEPARATOR;
		if (fixing.endsWith(PROCESS_THREAD_SEPARATOR)|| fixing.endsWith(":")) 
			fixing = fixing + "*";
		return fixing;
	}

	Filter(Range process, Range thread){
		this.process = process;
		this.thread = thread;
	}
	boolean matches(TraceName name){
		return matches(name.process, name.thread);
	}
	boolean matches (int processNum, int threadNum){
		return process.matches(processNum) && thread.matches(threadNum);
	}
	@Override
	public String toString() {
		return process + PROCESS_THREAD_SEPARATOR + thread;
	}
	//Unit test:
	public static void main(String[] args){
		String[] patterns = {"3:7:2$", "$1" ,"1::2$2:4:2"};
String[] messages = {"should match all threads of ranks 3, 5, and 7.", "will match thread 1 of all processes.", "will match 1.2, 1.4, 3.2, 3.4, 5.2 ..."};
TraceName[][] tests = {
		{ new TraceName(2,1), new TraceName(3,2), new TraceName(4,7), new TraceName(10,10)},
		{ new TraceName(0,1), new TraceName(10,2), new TraceName(999,1), new TraceName(1,7)},
		{ new TraceName(5,2), new TraceName(3,4), new TraceName(1,1), new TraceName(2,2)}
		};
	for (int i = 0; i < patterns.length; i++) {
		Filter f = new Filter(patterns[i]);
		System.out.println(patterns[i] + " decoded to " + f.toString() + " in full form");
		System.out.println(messages[i]);
		TraceName[] theseTests = tests[i];
		for (int j = 0; j < theseTests.length; j++) {
			System.out.println(theseTests[j] + (f.matches(theseTests[j])? ": matches" : ": does not match."));
		}
	}
	}

	public void serializeSelfToStream(DataOutputStream stream) throws IOException {
		process.serializeSelfToStream(stream);
		thread.serializeSelfToStream(stream);
	}
}

class Range{
	private static final int END = Integer.MAX_VALUE;
	private static final int START = 0;
	final int min, max, stride;
	Range(){//Match everything
		min = START;
		max = END;
		stride = 1;
	}
	public void serializeSelfToStream(DataOutputStream stream) throws IOException {
		stream.writeInt(min);
		stream.writeInt(max);
		stream.writeInt(stride);
	}
	Range(int min, int max, int stride){
		this.min = min;
		this.max = max;
		this.stride = stride;
	}
	public Range(String string) {
		String[] pieces =string.split(":");
		//default value in case they are not specified
		int min = START;
		int max = END;
		int stride = 1;
		switch (pieces.length){//Don't break on the switch so that we can get all the pieces we need
		case 3:
			stride = specialParse(pieces[2], stride);
		case 2:
			max = specialParse(pieces[1], max);
			min = specialParse(pieces[0], min);
			break;
			//The reason for this difference is that "4" means [4,4], which is very different from "4:", which means [4, inf].
		case 1:
			max = specialParse(pieces[0], max);
			min = specialParse(pieces[0], min);
			break;
		}
		this.min = min;
		this.max = max;
		this.stride = stride;
	}
	private static int specialParse(String string, int defaultValue) {
		if (string.length()==0) return defaultValue;
		try{
			return Integer.parseInt(string);
		} catch (NumberFormatException e){
			return defaultValue;
		}

	}
	boolean matches (int i){
		if (i < min) return false;
		if (i > max) return false;
		return ((i - min) % stride) == 0;
	}
	@Override
	public String toString() {
		return (min == START? "start" : min)+ ":" + (max == END ? "end" : max) + ":"
				+ stride;
	}
}

