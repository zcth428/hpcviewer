package edu.rice.cs.hpc.data.framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.jface.viewers.TreeNode;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PrintFlatViewScopeVisitor;
import edu.rice.cs.hpc.data.util.Util;

/******************************************************************************************
 * Class to manage the execution of the light version of hpcviewer
 * This class will require an argument for a database or XML file, then
 * 	output the result into XML file
 * Otherwise, output error message.
 * No user interaction is needed in this light version
 * @author laksonoadhianto
 *
 ******************************************************************************************/
public class Application {

	//-----------------------------------------------------------------------
	// Constants
	//-----------------------------------------------------------------------
	final private String DTD_FILE_NAME = "/edu/rice/cs/hpc/data/experiment/xml/experiment.dtd";
	final private int MAX_BUFFER = 1024;
	
	/***---------------------------------------------------------------------**
	 * Open a XML database file, and return true if everything is OK.
	 * @param objFile: the XML experiment file
	 * @return
	 ***---------------------------------------------------------------------**/
	private boolean openExperiment(File objFile) {
		Experiment experiment;
		System.out.print("Opening " + objFile.getAbsolutePath() );

		try {
			experiment = new Experiment(objFile);	// prepare the experiment
			experiment.open();						// parse the database
			experiment.postprocess(false);			// create the flat view
			this.printFlatView(experiment);
			System.out.println(" .... successfully");
			return true;
		} catch (Exception e) {
			System.out.println(" .... failed");
			e.printStackTrace();
			return false;
		}
	}
	
	
	/***---------------------------------------------------------------------**
	 * 
	 * @param experiment
	 ***---------------------------------------------------------------------**/
	private void printFlatView(Experiment experiment) {
		TreeNode []rootChildren = experiment.getRootScopeChildren();
		if (rootChildren != null) {
			PrintStream objStream = System.out;
			int nbChildren = rootChildren.length;
			
			// flat root must be the last one
			RootScope flatRoot = (RootScope) rootChildren[nbChildren-1].getValue();
			
			this.printDTD(objStream);
			//---------------------------------------------------------------------------------
			// print the title
			//---------------------------------------------------------------------------------
			objStream.print("Scope ");
			
			// print the metrics
			int nbMetrics = experiment.getMetricCount();
			for (int i=0; i<nbMetrics; i++ ) {
				objStream.print(", " + experiment.getMetric(i).getDisplayName());
			}
			objStream.println();
			
			//---------------------------------------------------------------------------------
			// print the content
			//---------------------------------------------------------------------------------
			PrintFlatViewScopeVisitor objPrintFlat = new PrintFlatViewScopeVisitor(experiment, objStream);
			flatRoot.dfsVisitScopeTree(objPrintFlat);
		} else {
			System.err.println("The database contains no information");
		}
	}
	

	/**---------------------------------------------------------------------**
	 * Printing DTD of an experiment. The sample of DTD is located in edu.rice.cs.hpc.data.experiment.xml package
	 * This method will first load the file, then print it. 
	 * This is not the most effecient way to do, but it is the most configurable way I can think. 
	 * @param objPrint
	 **---------------------------------------------------------------------**/
	private void printDTD(PrintStream objPrint) {
		InputStream objFile = this.getClass().getResourceAsStream(DTD_FILE_NAME);
		if ( objFile != null ) {

		    byte[] buf=new byte[MAX_BUFFER];
		    
            objPrint.println();
		    //---------------------------------------------------------
		    // iteratively read DTD file and print partially to the stream
		    //---------------------------------------------------------
            while (true) {
                int numRead = 0;
				try {
					numRead = objFile.read(buf, 0, buf.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
                if (numRead <= 0) {
                    break;
                } else {
                    String dtd = new String(buf, 0, numRead);
                    objPrint.print(dtd);
                }

            }
		    //---------------------------------------------------------
            // DTD has been printed, we need a new line to make nice format 
		    //---------------------------------------------------------
            objPrint.println();
		}
	}
	
	
	/**---------------------------------------------------------------------**
	 * Main application
	 * @param args
	 **---------------------------------------------------------------------**/
	public static void main(String[] args) {
		Application objApp = new Application();
		
		if ( (args == null) || (args.length==0)) {
			System.out.println("Usage: java -jar hpcdata.jar experiment_database");
			return;
		}
		String sFilename = args[0];
		// open the experiment if possible
		File objFile = new File(sFilename);
		if (objFile.isDirectory()) {
			File files[] = Util.getListOfXMLFiles(sFilename);
			boolean done = false;
			for (int i=0; i<files.length && !done; i++) {
				done = objApp.openExperiment(files[i]);
			}
		} else {
			objApp.openExperiment(objFile);
			
		}

	}

}
