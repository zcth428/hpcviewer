package edu.rice.cs.hpc.traceviewer.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;


public class TraceCompactor {
	
	public static final int SIZEOF_LONG = 8;
	public static final int SIZEOF_INT = 4;
	public static final int PAGE_SIZE_GUESS = 4096;
	public static final String MASTER_FILE_NAME = "SingleTraceFile.megatrace";
	
	public static void compact(String directory) throws IOException {
		
		File dir = new File(directory);
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(directory + File.separatorChar + MASTER_FILE_NAME));
		
		//select the (non-buggy) trace files
		//TODO: this won't work when there are multiple threads running on a core,
		//the contains at the end of this filter is just a hack to get around a bug
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".hpctrace") && name.contains("-000-");
			}
		};
		
		//write the header
		File[] traces = dir.listFiles(filter);
		dos.writeInt(traces.length);
		
		//write the index
		long offset = traces.length*SIZEOF_LONG + SIZEOF_INT;
		for(int i = 0; i < traces.length; ++i) {
			dos.writeLong(offset);
			offset += traces[i].length();
		}
		
		//copy the traces
		for(int i = 0; i < traces.length; ++i) {
			DataInputStream dis = new DataInputStream(new FileInputStream(traces[i]));
			byte[] data = new byte[PAGE_SIZE_GUESS];
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
