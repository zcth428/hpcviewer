package edu.rice.cs.hpc.data.util;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.StringTokenizer;


public class FileMerger {
	
	public static final int SIZEOF_LONG = 8;
	public static final int SIZEOF_INT = 4;
	public static final int FILE_BUFFER_SIZE = 4096 * 1024;
	
	public static void merge(String directory, String resultFileName, File[] fileHandles) throws IOException {
		mergeHelper(directory, resultFileName, fileHandles);
	}
	
	
	public static void merge(String directory, String resultFileName, final String filterSuffix) throws IOException {
		
		File dir = new File(directory);
		
		// select the files to be merged by file suffix
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(filterSuffix);
			}
		};
		
		// determine the set of data files to be merged
		File[] fileHandles = dir.listFiles(filter);
		
		mergeHelper(directory, resultFileName, fileHandles);
	}
	
		
	private static void mergeHelper(String directory, String resultFileName, File [] fileHandles) throws IOException {
		// sort the files by name; they are not guaranteed to be in sorted order upon entry
		java.util.Arrays.sort(fileHandles);
		
		// open a data output stream for the resulting merged file
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(directory + File.separatorChar + resultFileName));
		
		// write the header, which consists of the number of data files merged within  
		dos.writeInt(fileHandles.length);
		
		// write the index, which points to the head of each data file within
		long offset = fileHandles.length * SIZEOF_LONG + SIZEOF_INT;
		for(int i = 0; i < fileHandles.length; ++i) {
			dos.writeLong(offset);
			offset += fileHandles[i].length() + 2*SIZEOF_INT;
		}
		
		// the entry for each data file contains: int process_id, int thread_id, byte[] data_bytes
		for(int i = 0; i < fileHandles.length; ++i) {
			DataInputStream dis = new DataInputStream(new FileInputStream(fileHandles[i]));
			byte[] data = new byte[FILE_BUFFER_SIZE];
			
			// retrieve the process id and thread id from the data file name
			StringTokenizer st = new StringTokenizer(fileHandles[i].getName(), "-");
			st.nextToken(); // executable name
			int process_id = Integer.parseInt(st.nextToken());
			int thread_id = Integer.parseInt(st.nextToken());
			
			// write the header for the current input data file into the merged file
			dos.writeInt(process_id);
			dos.writeInt(thread_id);
			
			// copy the data from the current input data file into the merged file
			int numRead = dis.read(data);
			while(numRead > 0) {
				dos.write(data, 0, numRead);
				numRead = dis.read(data);
			}
			dis.close();
		}
		dos.close();
	}
}
