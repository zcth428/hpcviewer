package edu.rice.cs.hpc.data.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.xml.PrintFileXML;
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

	
	/***---------------------------------------------------------------------**
	 * Open a XML database file, and return true if everything is OK.
	 * @param objFile: the XML experiment file
	 * @return
	 ***---------------------------------------------------------------------**/
	private boolean openExperiment(PrintStream objPrint, File objFile) {
		Experiment experiment;

		try {
			experiment = new Experiment(objFile);	// prepare the experiment
			experiment.open();						// parse the database
			experiment.postprocess(false);			// create the flat view
			this.printFlatView(objPrint, experiment);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/***---------------------------------------------------------------------**
	 * 
	 * @param experiment
	 ***---------------------------------------------------------------------**/
	private void printFlatView(PrintStream objPrint, Experiment experiment) {
		PrintFileXML objPrintXML = new PrintFileXML();
		objPrintXML.print(objPrint, experiment);
	}
	
	
	/**---------------------------------------------------------------------**
	 * Main application
	 * @param args
	 **---------------------------------------------------------------------**/
	public static void main(String[] args) {
		Application objApp = new Application();
		PrintStream objPrint = System.out;
		String sFilename = args[0];
		
		if ( (args == null) || (args.length==0)) {
			System.out.println("Usage: hpcdata.sh [-o output_file] experiment_database");
			return;
		} else  {
			for (int i=0; i<args.length; i++) {
				if (args[i].equals("-o") && (i<args.length-1)) {
					String sOutput = args[i+1];
					try {
						objPrint = new PrintStream( sOutput );
						i++;
					} catch (FileNotFoundException e) {
						System.err.println("Error: cannot create file " + sOutput);
						return;
					}
				} else {
					sFilename = args[i];
				}
			}
		}
		//------------------------------------------------------------------------------------
		// open the experiment if possible
		//------------------------------------------------------------------------------------
		File objFile = new File(sFilename);
		if (objFile.isDirectory()) {
			File files[] = Util.getListOfXMLFiles(sFilename);
			boolean done = false;
			for (int i=0; i<files.length && !done; i++) {
				done = objApp.openExperiment(objPrint, files[i]);
			}
		} else {
			objApp.openExperiment(objPrint, objFile);
			
		}

	}

}
