package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.io.PrintStream;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.experiment.xml.PrintFileXML;


/****************************************************************************************
 * 
 * @author laksonoadhianto
 *
 ****************************************************************************************/
public class PrintFlatViewScopeVisitor implements IScopeVisitor {
	static private StringBuffer indent;

	private Experiment objExperiment;
	private PrintStream objOutputStream;

	
	public PrintFlatViewScopeVisitor(Experiment experiment, PrintStream stream) {
		this.objExperiment = experiment;
		this.objOutputStream = stream;
		indent = new StringBuffer();
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) { print(scope, "u", vt, false, false); }
	public void visit(RootScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) printMetrics(scope); 
	}
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { print(scope, "LM", vt, true, false); }
	public void visit(FileScope scope, ScopeVisitType vt) { print(scope, "F", vt, true, false); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { print(scope, "P", vt, true, true); }
	public void visit(AlienScope scope, ScopeVisitType vt) { print(scope, "A", vt, true, true); }
	public void visit(LoopScope scope, ScopeVisitType vt) { print(scope, "L", vt, false, true); }
	public void visit(LineScope scope, ScopeVisitType vt) { print(scope, "S", vt, false, true); }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { print(scope, "S", vt, false, true); }
	public void visit(CallSiteScope scope, ScopeVisitType vt) { print(scope, "C", vt, true, false); }
	public void visit(GroupScope scope, ScopeVisitType vt) { print(scope, "G", vt, false, true); }

	/**-------------------------------------------------------------------------------**
	 * Print the scope information into XML tag
	 * @param scope
	 * @param initial
	 * @param vt
	 * @param name
	 * @param line
	 **-------------------------------------------------------------------------------**/
	private void print(Scope scope, String initial, ScopeVisitType vt, 
			boolean name, boolean line) {
		if (vt == ScopeVisitType.PreVisit) {
			
			//--------------------------------------------------
			// print the scope tag, attributes and values
			//--------------------------------------------------
			this.objOutputStream.print(indent + "<" + initial + " ");
			PrintFileXML.printAttribute(objOutputStream, "i", scope.hashCode());
			
			if (name)
				PrintFileXML.printAttribute(objOutputStream, "n", scope.getName());
			
			if (line) {
				int line1 = scope.getFirstLineNumber();
				int line2 = scope.getLastLineNumber();
				if (line1 == line2)
					PrintFileXML.printAttribute(objOutputStream, "l", line1);
				else 
					PrintFileXML.printAttribute(objOutputStream, "l", line1 + "-" + line2);
			}
			
			if (scope instanceof AlienScope) {
				SourceFile objFile = scope.getSourceFile();
				if (objFile != null)
					PrintFileXML.printAttribute(objOutputStream, "f", objFile.getFilename());
			}
			this.objOutputStream.println(" >" );

			//--------------------------------------------------
			// print the metric values of this scope
			//--------------------------------------------------
			this.printMetrics(scope);
			
			//--------------------------------------------------
			// increment the indentation for future usage
			//--------------------------------------------------
			indent.append(' ');
			
		} else {
			
			indent.deleteCharAt(0);
			this.objOutputStream.println(indent + "</" + initial + ">" );
			
		}
	}
	
	
	/***-------------------------------------------------------------------------------**
	 * 
	 * @param scope
	 ***-------------------------------------------------------------------------------**/
	private void printMetrics(Scope scope) {

		int nbMetrics = objExperiment.getMetricCount();
		for (int i=0; i<nbMetrics; i++) {
			MetricValue value = scope.getMetricValue(i);
			if (value.isAvailable()) {
				BaseMetric m = objExperiment.getMetric(i);
				this.objOutputStream.print(indent + "<M ");
				PrintFileXML.printAttribute(this.objOutputStream, "n", m.getIndex());
				PrintFileXML.printAttribute(this.objOutputStream, "v", value.getValue());
				this.objOutputStream.print(" />");
			}
		}
		this.objOutputStream.println();
	}
}
