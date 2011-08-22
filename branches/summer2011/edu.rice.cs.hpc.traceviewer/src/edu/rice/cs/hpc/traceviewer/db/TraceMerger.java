package edu.rice.cs.hpc.traceviewer.db;

import java.io.IOException;

import edu.rice.cs.hpc.data.util.FileMerger;

public class TraceMerger {
	
	public static final String RESULT_FILE_NAME = "experiment.mt";
	public static final String FILTER_SUFFIX = ".hpctrace";
	
	public static void merge(String directory) throws IOException {
		FileMerger.merge(directory, RESULT_FILE_NAME, FILTER_SUFFIX);
	}
}
