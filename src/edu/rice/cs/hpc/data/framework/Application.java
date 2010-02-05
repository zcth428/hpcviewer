package edu.rice.cs.hpc.data.framework;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.jface.viewers.TreeNode;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
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
