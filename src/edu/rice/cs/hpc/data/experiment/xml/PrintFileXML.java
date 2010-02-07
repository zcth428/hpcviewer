package edu.rice.cs.hpc.data.experiment.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.jface.viewers.TreeNode;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.PrintFlatViewScopeVisitor;


/***************************************************************************************
 * Class to print the content of an experiment database into an output stream
 * @author laksonoadhianto
 *
 ***************************************************************************************/
public class PrintFileXML {
	//-----------------------------------------------------------------------
	// Constants
	//-----------------------------------------------------------------------
	final private String DTD_FILE_NAME = "/edu/rice/cs/hpc/data/experiment/xml/experiment.dtd";
	final private int MAX_BUFFER = 1024;

	
	/**--------------------------------------------------------------------------------**
	 * print an experiment into a given output stream
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	public void print(PrintStream objPrint, Experiment experiment) {
		TreeNode []rootChildren = experiment.getRootScopeChildren();
		if (rootChildren != null) {
			PrintStream objStream = System.out;
			int nbChildren = rootChildren.length;
			
			// flat root must be the last one
			RootScope flatRoot = (RootScope) rootChildren[nbChildren-1].getValue();
			
			//---------------------------------------------------------------------------------
			// print the DTD
			//---------------------------------------------------------------------------------
			this.printDTD(objStream);
			
			//---------------------------------------------------------------------------------
			// print the header
			//---------------------------------------------------------------------------------
			this.printHeader(objStream, experiment);
			
			
			//---------------------------------------------------------------------------------
			// print the content
			//---------------------------------------------------------------------------------
			objStream.println("<SecFlatProfileData>");
			PrintFlatViewScopeVisitor objPrintFlat = new PrintFlatViewScopeVisitor(experiment, objStream);
			//flatRoot.dfsVisitScopeTree(objPrintFlat);
			objStream.println("<SecFlatProfileData>");
		} else {
			System.err.println("The database contains no information");
		}

	}
	
	
	/**--------------------------------------------------------------------------------**
	 * Static method to print an attribute and its value to a specific format
	 * @param objPrint
	 * @param attribute
	 * @param value
	 **--------------------------------------------------------------------------------**/
	static public void printAttribute(PrintStream objPrint, String attribute, String value) {
		objPrint.print(attribute + "=\"" + value + "\" ");
	}

	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param objPrint
	 * @param attribute
	 * @param value
	 **--------------------------------------------------------------------------------**/
	static public void printAttribute(PrintStream objPrint, String attribute, int value) {
		objPrint.print(attribute + "=\"" + value + "\" ");
	}

	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	private void printMetricTable(PrintStream objPrint, Experiment experiment) {
		objPrint.println(" <MetricTable>");
		BaseMetric metrics[] = experiment.getMetrics();
		for(int i=0; i<metrics.length; i++) {
			BaseMetric m = metrics[i];
			objPrint.print("    <Metric "); 
			{
				printAttribute(objPrint, "i", m.getIndex());				
				printAttribute(objPrint, "n", m.getDisplayName().trim());				
				printAttribute(objPrint, "v", "final");				
				printAttribute(objPrint, "t", getMetricType(m) );				
				printAttribute(objPrint, "s", booleanToInt(m.getDisplayed()) );				
			}
			objPrint.print(">");

			objPrint.println(" </Metric>");
		}
		objPrint.println(" </MetricTable>");
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param m
	 * @return
	 **--------------------------------------------------------------------------------**/
	private String getMetricType (BaseMetric m) {
		if (m.getMetricType() == MetricType.EXCLUSIVE )
			return "exclusive";
		else if (m.getMetricType() == MetricType.INCLUSIVE )
			return "inclusive";
		return "nil";
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param b
	 * @return
	 **--------------------------------------------------------------------------------**/
	private int booleanToInt(boolean b) {
		if (b)
			return 1;
		else
			return 0;
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	private void printHeader(PrintStream objPrint, Experiment experiment) {
		objPrint.println("<HPCToolkitExperiment version=\"" + experiment.getMajorVersion() + "\">");
		objPrint.println("<Header n=\"" + experiment.getName()+"\">\n  <Info/>\n</Header>");
		objPrint.println("<SecHeader>");
		this.printMetricTable(objPrint, experiment);
		objPrint.println("</SecHeader>");
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
	

}
