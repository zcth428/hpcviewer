import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class to retrieve the required Java and GTK version in order to run the viewer
 * If the env (java+gtk) and the viewer do not match, return error 
 * 
 * @author laksonoadhianto
 *
 */
public class CheckPlatform {

	static final private String EXTENSION[] = {".dylib",".so"};
	
	public CheckPlatform() {
		String prop = System.getProperty("java.library.path");
		String []props = prop.split(":");

		// ------------------------------------------------------
		// find a .so or .dylib java file by iterating all possible
		//  java lib path. Once a dynamic file is found, we check the status
		//  of the file by using "file -L" command line to retrieve if
		//  the library is 32 or 64 bits
		// ------------------------------------------------------
		boolean found = false;
		for (int i=0; i<props.length && !found; i++) {
			File objPath = new File(props[i]);    	
			if (objPath.exists()) {
				File []files = objPath.listFiles(new SoFilenameFilter());	    		
				found = (files.length > 0);
				if (found) {
					Runtime objRuntime = Runtime.getRuntime();
					try {
						// ------------------------------------------------------
						// check the status of the dynamic library
						// ------------------------------------------------------
						Process p = objRuntime.exec("file -L "+files[i], null, null);
						InputStream in = p.getInputStream();
						BufferedInputStream buf = new BufferedInputStream(in);
						InputStreamReader inread = new InputStreamReader(buf);
						BufferedReader bufferedreader = new BufferedReader(inread);
						
						String line = bufferedreader.readLine();
						String lines[] = line.split("[:,]");
						if (lines.length>0) {
							if (lines[1].contains("32-bit"))
								System.out.println("32 bits");
							else if (lines[1].contains("64-bit"))
								System.out.println("64 bits");
							else
								System.out.println("File: " + lines[1]);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} 
	}

	/**
	 * main function to start with
 	 */
	public static void main(String[] args) {
		CheckPlatform o = new CheckPlatform();
	}

	/**
	 * Filter class to select certain libraries
	 * @author laksonoadhianto
	 *
	 */
	class SoFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			for (int i=0; i<EXTENSION.length; i++) {
				if (name.contains(EXTENSION[i]))
					return true;
			}
			return false;
		}

	}
}
