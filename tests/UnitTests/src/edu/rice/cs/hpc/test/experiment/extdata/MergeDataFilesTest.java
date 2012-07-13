package edu.rice.cs.hpc.test.experiment.extdata;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import edu.rice.cs.hpc.data.util.IProgressReport;
import edu.rice.cs.hpc.data.util.MergeDataFiles;

import junit.framework.TestCase;

/*****
 * 
 * testing merging files
 *
 */
public class MergeDataFilesTest extends TestCase {
	
	@Test
	public void testMerge() {
		
		final String dir = "data/gauss/";
		File file = new File(dir);
		assertTrue ("Directory is not readable: " + dir,file.isDirectory());

		try {
			final String outputFile = "experiment.mt";
			IProgressReport progress = new IProgressReport(){

				public void begin(String title, int num_tasks) {
					System.out.println("start: " + title);
				}

				public void advance() {	System.out.print(".");}

				public void end() {
					System.out.println("\nDone.");
				}
				
			};
			final MergeDataFiles.MergeDataAttribute att = MergeDataFiles.merge(file, "*.hpctrace", outputFile, progress);
			
			assertNotNull("Fail to merge: " + dir,att);
			assertTrue("Fail to merge (no data): " + dir,MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA != att);
			
		} catch (IOException e) {
			assertFalse("Unable to merge: " + dir, false);
			e.printStackTrace();
		}
	}

}
