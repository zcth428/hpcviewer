
////
//ExperimentBuilder.java						//
////
//experiment.xml.ExperimentBuilder -- XML builder for hpcviewer	//
//Last edited: January 16, 2002 at 11:27 am			//
////
//(c) Copyright 2002 Rice University. All rights reserved.	//
////





package edu.rice.cs.hpc.data.experiment.xml;


import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.util.*;

import java.io.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
//johnmc
import java.util.Hashtable;
// laks 2008.08.27
import java.util.EmptyStackException;





//CLASS EXPERIMENT-BUILDER					//
// Laksono: we keep this class for backward compatibilty with old XML format

/**
 *
 * Builder for an XML parser for HPCView experiment files.
 *
 */


public class ExperimentBuilder extends Builder
{

	final static String BEGIN_LINE_ATTRIBUTE 	= "b";
	final static String END_LINE_ATTRIBUTE 		= "e";
	final static String NAME_ATTRIBUTE 			= "n";
	final static String FILENAME_ATTRIBUTE 		= "f";
	final static String VALUE_ATTRIBUTE 		= "v";
	final static String PATHNAME_ATTRIBUTE 		= "name";
	final static String SID_ATTRIBUTE 		= "sid";
	

	/** The experiment to own parsed objects. */
	protected Experiment experiment;

	/** The default name for the experiment, in case none is found by the parser. */
	protected String defaultName;

	/** The parsed configuration. */
	protected ExperimentConfiguration configuration;

	/** The parsed search paths. */
	protected List/*<File>*/ pathList;

	/** The parsed metric objects. */
	protected List/*<Metric>*/ metricList;

	/** The parsed scope objects. */
	protected List/*<Scope>*/ scopeList;

	/** The parsed root scope object. */
	protected Scope rootScope;
	protected Scope callingContextViewRootScope;
	protected Scope callersViewRootScope;
	protected Scope flatViewRootScope;

	/** A stack to keep track of scope nesting while parsing. */
	protected Stack/*<Scope>*/ scopeStack;

	/** The current source file while parsing. */
	protected Stack/*<SourceFile>*/ srcFileStack;

//	johnmc
	protected Hashtable<String, SourceFile> hashSourceFileFromName;
	// laks 2009.05.04: to keep compatibility
	protected Hashtable<Integer, SourceFile> hashSourceFileFromKey;

	/** Number of metrics provided by the experiment file.
    For each metric we will define one inclusive and one exclusive metric.*/
	protected int numberOfPrimaryMetrics; 

	/** Maximum number of metrics provided by the experiment file.
    We use the maxNumberOfMetrics value to generate short names for the self metrics*/
	protected int maxNumberOfMetrics;

	public boolean csviewer;


	/*************************************************************************
	 *	Creates a ExperimentBuilder.
	 *
	 *	Parsed objects are added to the experiment as soon as possible, but
	 *	all the parsed objects of a given kind must be added at once. The
	 *	builder keeps parsed objects on lists until a whole set can be added
	 *	to the experiment.
	 *	<p>
	 *	Because of the way <code>Metric</code>s are implemented, the metric
	 *	objects must be added to the experiment before any scope objects are
	 *	constructed. Scopes need to know how many metrics the experiment has,
	 *	and find out by asking the experiment.
	 *
	 *	@see begin_PGM
	 *	@see edu.rice.cs.hpcview.experiment.Experiment#setMetrics
	 *	@see edu.rice.cs.hpcview.experiment.metric.Metric#getMetric
	 *	@see edu.rice.cs.hpcview.experiment.scope.Scope#getMetric
	 *
	 ************************************************************************/

	public ExperimentBuilder(Experiment experiment, String defaultName)
	{
		super();

		// creation arguments
		this.experiment = experiment;
		this.defaultName = defaultName;

		this.csviewer = false;
		// temporary storage for parsed objects
		this.pathList   = new ArrayList/*<File>*/();
		//this.fileList   = new ArrayList/*<SourceFile>*/();
		this.metricList = new ArrayList/*<Metric>*/();
		this.scopeList  = new ArrayList/*<Scope>*/();

		// parse action data structures
		this.scopeStack = new Stack/*<Scope>*/();
		this.srcFileStack = new Stack/*<SourceFile>*/();
		this.srcFileStack.push(null); // mimic old behavior

		hashSourceFileFromKey = new Hashtable<Integer, SourceFile>();
		hashSourceFileFromName = new Hashtable<String, SourceFile>();
		numberOfPrimaryMetrics = 0;
		maxNumberOfMetrics = 100;
	}




//	PARSING SEMANTICS													//





	/*************************************************************************
	 *	Initializes the build process.
	 ************************************************************************/

	public void begin()
	{
		this.configuration = new ExperimentConfiguration();
	}


	/*************************************************************************
	 *	Takes notice of the beginning of an element.
	 ************************************************************************/

	public void beginElement(String element, String[] attributes, String[] values)
	{
		switch(Token.map(element))
		{
		// CONFIG elements
		case Token.TITLE:
			this.do_TITLE (attributes, values);	break;
		case Token.PATH:
			this.do_PATH  (attributes, values);	break;
		case Token.METRIC:
			this.do_METRIC(attributes, values);	break;

			// PGM elements
		case Token.PGM:
			this.begin_PGM(attributes, values);	break;
		case Token.LM:
			this.begin_LM (attributes, values);	break;
		case Token.G:
			this.begin_G  (attributes, values);	break;
		case Token.F:
			this.begin_F  (attributes, values);	break;
		case Token.P:
			this.begin_P  (attributes, values);	break;
		case Token.A:
			this.begin_A  (attributes, values);	break;
		case Token.L:
			this.begin_L  (attributes, values);	break;
		case Token.S:
			this.begin_S  (attributes, values);	break;
		case Token.M:
			this.do_M     (attributes, values);	break;

			// ignored elements
		case Token.HPCVIEWER:
			// Flat profile
			this.csviewer = false;
			//this.initExperiment();
			break;
		case Token.CONFIG:
		case Token.METRICS:
		case Token.REPLACE:
			break;

			// callstack elements
		case Token.CSPROFILE:
			this.csviewer = true;
			break;
		case Token.TARGET:		
			this.do_TARGET(attributes,values);	
			break;
		case Token.CALLSITE:
			this.begin_CALLSITE(attributes,values); 
			break;
		case Token.PROCEDURE_FRAME:	
			this.begin_P(attributes,values); 
			break;
		case Token.STATEMENT:		
			this.begin_S(attributes,values); 
			break;

		case Token.SCOPETREE:
		case Token.CSPROFILEHDR:
		case Token.CSPROFILEPARAMS:
		case Token.CSPROFILETREE:
			break;

			// unknown elements
		default:
			this.error();
		break;
		} 
	}




	/*************************************************************************
	 *	Takes notice of content characters within an element.
	 *
	 *	None of the elements in an hpcviewer experiment XML file have content
	 *	characters, so this method should never be called.
	 *
	 ************************************************************************/

	public void content(String s)
	{
		Dialogs.notCalled("ExperimentBuilder.content");
	}




	/*************************************************************************
	 *	Takes notice of the ending of an element.
	 ************************************************************************/

	public void endElement(String element)
	{
		switch(Token.map(element))
		{
		// PGM elements
		case Token.PGM:
			this.end_PGM();
			break;
		case Token.LM:
			this.end_LM();
			break;
		case Token.G:
			this.end_G();
			break;
		case Token.F:
			this.end_F();
			break;
		case Token.P:
			this.end_P();
			break;
		case Token.A:
			this.end_A();
			break;
		case Token.L:
			this.end_L();
			break;
		case Token.S:
			this.end_S();
			break;


			// ignored elements
		case Token.HPCVIEWER:
		case Token.TITLE:
		case Token.PATH:
		case Token.METRIC:
		case Token.CONFIG:
		case Token.METRICS:
		case Token.REPLACE:
		case Token.M:
			break;

			//callstack elements---FMZ
		case Token.CALLSITE: 		
			this.end_CALLSITE();
			break;
		case Token.PROCEDURE_FRAME:     
			this.end_P(); 
			break;
		case Token.STATEMENT:
			this.end_S();
			break;
		case Token.CSPROFILE:
		case Token.SCOPETREE:
		case Token.CSPROFILEHDR:
		case Token.CSPROFILEPARAMS:
		case Token.CSPROFILETREE:
		case Token.TARGET:
			break;


		default:
			this.error();
		break;
		} 
	}




	/*************************************************************************
	 *	Finalizes the build process.
	 ************************************************************************/

	public void end()
	{
		// bugs no 224: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=224&group_id=22&atid=169
		try {
			// pop out root scope
			this.scopeStack.pop();
		} catch (EmptyStackException e) {
			System.err.println("ExperimentBuilder: no root scope !");
		}
		
		// check that input was properly nested
		if (!this.scopeStack.empty()) {
			Scope topScope = (Scope) this.scopeStack.peek();
			System.out.println("Stack is not empty; remaining top scope = " + topScope.getName());
			this.error();
		}

		// check semantic constraints
		if( this.hashSourceFileFromKey.size() == 0 ) {
			System.out.println("Warning: no source files found!");
			// bug no 189: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=189&group_id=22&atid=169
			//this.error();
		} 

		if( this.scopeList.size() == 0 ) {
			System.out.println("Warning: scope tree is empty!");
			this.error();
		}

		// copy parse results into configuration
		this.configuration.setSearchPaths(this.pathList);
		this.experiment.setConfiguration(this.configuration);
		// Laks 2009.01.06: get rid off unused methods and attributes
		// this.experiment.setSourceFiles(this.fileList);
		this.experiment.setScopes(this.scopeList, this.rootScope);

		// supply defaults for missing info
		if( this.configuration.getName() == null )
			this.configuration.setName(this.defaultName);
		if( this.configuration.getSearchPathCount() == 0 )
		{
			List/*<File>*/ paths = new ArrayList/*<File>*/();
			paths.add(new File(""));
			paths.add(new File("src"));
			paths.add(new File("compile"));
			this.configuration.setSearchPaths(paths);
		}

	}


	// ========================================================================
	// ========================================================================
	//	BUILDING															//
	// ========================================================================
	// ========================================================================

	
	/*************************************************************************
	 *	Processes a TITLE element.
	 ************************************************************************/

	private void do_TITLE(String[] attributes, String[] values)
	{
		// TITLE name = "experiment title"
		this.Assert(attributes.length == 1);

		this.configuration.setName(values[0]);
	}


	/*************************************************************************
	 *      Processes a TARGET element as TITLE.
	 ************************************************************************/

	private void do_TARGET(String[] attributes, String[] values)
	{
		// TITLE name = "experiment title"
		this.Assert(attributes.length == 1);

		this.configuration.setName(values[0]);
	}


	/*************************************************************************
	 *	Processes a PATH element.
	 ************************************************************************/

	private void do_PATH(String[] attributes, String[] values)
	{
		// PATH name="somepath"
		String name = getAttributeByName(PATHNAME_ATTRIBUTE, attributes, values);
		File path = new File(name);
		this.pathList.add(path);
	}

	/*************************************************************************
	 *	Processes a METRIC element.
	 ************************************************************************/

	private void do_METRIC(String[] attributes, String[] values)
	{
		if(this.csviewer)
		{
			// METRIC shortName="internal" nativeName="native" 
			//         displayName="User Visible" display="bool" percent="bool" 
			//
			// CSPROF: shortName="0" nativeName="IA64_INST_RETIRE" 
			//         displayName=NULL display=NULL percent=NULL 
			//         period="int number"  
			//
			// Alpha CSPROF: shortName="0" nativeName="# bytes allocated" default period=1
			//               shortName="1" nativeName="# bytes freed" default period=1;
			//               shortName="2" nativeName="SIGSEGVs received" period="0"

			String[] new_values=new String[6];

			final int N_shortName = 0, N_nativeName = 1, N_displayName = 2, N_display = 3, N_percent = 4, N_sampleperiod = 5;
			if (attributes.length==5) { 

				int exclusiveIndex = this.metricList.size() + 1;

				new_values[N_sampleperiod]="1";
				Metric metric = new Metric(this.experiment,
						values[N_shortName], 
						values[N_nativeName], 
						values[N_displayName]+" (total)",
						Util.booleanValue(values[N_display]), 
						Util.booleanValue(values[N_percent]),
						new_values[N_sampleperiod],
						MetricType.INCLUSIVE, exclusiveIndex);
				this.metricList.add(metric);


				// add a new metric for self values
				//  use internal name = <old internal name> + MaxMetrics 
				// for now use MaxMetrics = 100
				int intShortName = Integer.parseInt(values[N_shortName]);
				int newShortName = intShortName + 100;
				String stringNewShortName = ""+newShortName;
				String selfMetricDisplayName = values[N_displayName]+" (self)";
				Metric selfMetric = new Metric(this.experiment,
						stringNewShortName, 
						values[N_nativeName], 
						selfMetricDisplayName,
						Util.booleanValue(values[N_display]), 
						Util.booleanValue(values[N_percent]),
						new_values[N_sampleperiod],
						MetricType.EXCLUSIVE, 
						exclusiveIndex-1);

				this.metricList.add(selfMetric);


			} else {
				this.Assert(attributes.length == 4);   
				// call stack profiler: <shortName, nativeName, period,flags>
				//                       nativeName as "displayName" 
				//                       flags: not used right now
				// System.out.println(Double.valueOf(values[2]).doubleValue()); 

				new_values[0]=values[N_shortName]; 
				new_values[1]=values[N_shortName];
				new_values[2]=values[N_nativeName];
				new_values[3]="true"; 
				new_values[4]="true"; 
				new_values[5]=values[2];  //sample period

				int exclusiveIndex = this.metricList.size() + 1;
				Metric metric = new Metric(this.experiment,
						new_values[N_shortName], new_values[N_nativeName], 
						new_values[N_displayName]+" (I)",
						Util.booleanValue(new_values[N_display]), 
						Util.booleanValue(new_values[N_percent]),
						new_values[N_sampleperiod],
						MetricType.INCLUSIVE, exclusiveIndex);
				this.metricList.add(metric);    

				// add a new metric for self values
				//  use internal name = <old internal name> + MaxMetrics 
				// for now use MaxMetrics = 100
				int intShortName = Integer.parseInt(values[N_shortName]);
				int newShortName = intShortName + 100; 

				String selfMetricDisplayName = 
					new_values[N_displayName]+" (E)";

				String stringNewShortName = ""+newShortName; 

				String displayValue = "true";
				String displayPercentValue = "true";

				Metric selfMetric = 
					new Metric(this.experiment, 
							stringNewShortName, 
							new_values[N_nativeName], 
							selfMetricDisplayName,
							Util.booleanValue(displayValue), 
							Util.booleanValue(displayPercentValue),
							new_values[N_sampleperiod],	MetricType.EXCLUSIVE, 
							exclusiveIndex-1);
				this.metricList.add(selfMetric);
			} 
			this.numberOfPrimaryMetrics += 2;
		}
		else
		{
			// METRIC shortName="internal" nativeName="native" displayName="User Visible" display="bool" percent="bool"
			final int N_shortName = 0, N_nativeName = 1, N_displayName = 2, N_display = 3, N_percent = 4;
			this.Assert(attributes.length == 5);

			Metric metric = new Metric(this.experiment,
					values[N_shortName], values[N_nativeName], 
					values[N_displayName],
					Util.booleanValue(values[N_display]), Util.booleanValue(values[N_percent]), defaultName,
					MetricType.INCLUSIVE, 
					Metric.NO_PARTNER_INDEX);
			this.metricList.add(metric);
			
			this.numberOfPrimaryMetrics++;
		}
	}




	/*************************************************************************
	 *	Begins processing a PGM (program) element.
	 ************************************************************************/

	private void begin_PGM(String[] attributes, String[] values) 
	{
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		
		//this.experiment.setMetrics(this.metricList);

		// make the root scope
		this.rootScope = new RootScope(this.experiment, name,"Invisible Outer Root Scope", RootScopeType.Invisible);
		this.scopeStack.push(this.rootScope);	// don't use 'beginScope'
		
		if (this.csviewer) {
			// create Calling Context Tree scope
			this.callingContextViewRootScope  = new RootScope(this.experiment, name,"Calling Context View", RootScopeType.CallingContextTree);
			beginScope(this.callingContextViewRootScope);
		} else {
			// flat scope
			this.flatViewRootScope = new RootScope(this.experiment, name, "Flat View", RootScopeType.Flat);
			this.beginScope(this.flatViewRootScope);	
		}
		this.initExperiment();
	}

	/************************************************************************
	 * Initialize the experiment database
	 ************************************************************************/
	private void initExperiment() 
	{
		this.experiment.setMetrics(metricList);
		this.experiment.setFileTable(this.hashSourceFileFromKey);
	}


	/*************************************************************************
	 *	Finishes processing a PGM (program) element.
	 ************************************************************************/

	private void end_PGM()
	{
		this.endScope();
		this.experiment.setFileTable(this.hashSourceFileFromKey);
	}



	/*************************************************************************
	 *	Begins processing an LM (load module) element.
	 ************************************************************************/

	private void begin_LM(String[] attributes, String[] values)
	{
		// LM n="load module name"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		// make a new load module scope object
		Scope lmScope = new LoadModuleScope(this.experiment, name, SourceFile.NONE);
		this.beginScope(lmScope);
	}




	/*************************************************************************
	 *	Finishes processing an LM (load module) element.
	 ************************************************************************/

	private void end_LM()
	{
		this.endScope();
	}


	/*************************************************************************
	 *	Begins processing an G (group) element.
	 ************************************************************************/

	private void begin_G(String[] attributes, String[] values)
	{
		// G n="group name"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);

		// make a new group scope object
		Scope gs = new GroupScope(this.experiment, name);
		this.beginScope(gs);
	}



	/*************************************************************************
	 *	Finishes processing a G (group) element.
	 ************************************************************************/

	private void end_G()
	{
		this.endScope();
	}


	/*************************************************************************
	 *	Begins processing an F (file) element.
	 ************************************************************************/
	private void begin_F(String[] attributes, String[] values)

	{
		// F n="filename"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);

		SourceFile file = this.getOrCreateSourceFile(name);
		this.srcFileStack.push(file);
		// make a new file scope object
		Scope fileScope = new FileScope(this.experiment, file);

		this.beginScope(fileScope);
	}


	/*************************************************************************
	 * Create source file and then add it into the dictionary database if necessary
	 * @param sFilename
	 * @return the source file
	 *************************************************************************/
	private SourceFile getOrCreateSourceFile(String sFilename) {
		SourceFile sourceFile=(SourceFile)hashSourceFileFromName.get(sFilename);
		if (sourceFile == null) {
			// make a new source file object
			File filename = new File(sFilename);
			int index = this.hashSourceFileFromKey.size()+1;
			sourceFile = new FileSystemSourceFile(experiment, filename, index);
			sourceFile.setIsText(true);

			this.hashSourceFileFromKey.put(Integer.valueOf(index), sourceFile);
		}
		return sourceFile;
	}


	/*************************************************************************
	 *	Finishes processing an F (file) element.
	 ************************************************************************/

	private void end_F()
	{
		this.endScope();
		this.srcFileStack.pop();
		this.Assert(srcFileStack.peek() == null); // mimic old behavior
	}




	/*************************************************************************
	 *	Begins processing a P (procedure) element.
	 ************************************************************************/

	private void begin_P(String[] attributes, String[] values)
	{
		if(this.csviewer)
		{
			// <PROCEDURE_FRAME sid="" f="filename" p="procname" alien="false">

			boolean istext = false; 
			boolean isalien = false; 

			int      attr_sid      = 0;
			String[] attr_file     = new String[1];
			String fileLine;
			String[] attr_function = new String[3];
			String[] val_function  = new String[3];
			String[] attr_line     = new String[2];
			String[] val_line      = new String[2];

			attr_file[0]= "n";
			fileLine ="unknown file line"; 

			attr_function[0]="n";
			attr_function[1]="b";
			attr_function[2]="e";

			val_function[0]="unknown procedure";
			val_function[1]="0";
			val_function[2]="0";

			attr_line[0]="b";
			attr_line[1]="e";

			val_line[0]="0";
			val_line[1]="0";
			

			for(int i=0; i<attributes.length; i++) {
				String sAtt = attributes[i];
				if (sAtt.equals("sid")) { 
					// In old XML format, sometime sid is a star (don;t know why)
					if(!values[i].startsWith("*"))
						attr_sid = Integer.parseInt(values[i]); 
				}
				if(sAtt.equals("f")) { 
					istext = true;
					fileLine = values[i]; 
				}
				else if(sAtt.equals("lm")) { 
					istext = false;
					fileLine = values[i]; 
				}
				else if(sAtt.equals("p")) {
					val_function[0] = values[i];
				}
				else if(sAtt.equals("l")) {
					val_function[1]=values[i];
					val_function[2]=values[i];
					val_line[0] = values[i];
					val_line[1] = values[i];	 
				} else if(sAtt.equals("alien")) { 
					if (values[i].equals("true")) {
						isalien = true;
					}
				}
			}

			SourceFile srcFile = this.getFileForCallsite(fileLine);
			srcFile.setIsText(istext);

			int firstLn = Integer.parseInt(val_line[0]);
			int lastLn  = Integer.parseInt(val_line[1]);
			if (attr_sid == 0) {
				attr_sid = this.getProcHashcode(srcFile.getName(), val_function[0]);
			}
			Scope procScope  = new ProcedureScope(this.experiment, null, srcFile, 
					firstLn-1, lastLn-1, 
					val_function[0], attr_sid, isalien);

			/** Laks 2008.08.25: original code
			 * 			Scope procScope  = new ProcedureScope(this.experiment, (SourceFile)srcFile, 
					firstLn-1, lastLn-1, 
					val_function[0], isalien);
			 */
			if (this.scopeStack.peek() instanceof LineScope) {

				//System.out.println("CallSiteScope Building..."+((Scope) this.stack.peek()).getName());

				LineScope ls = (LineScope)this.scopeStack.pop();
				CallSiteScope csn = new CallSiteScope((LineScope) ls, (ProcedureScope) procScope, CallSiteScopeType.CALL_TO_PROCEDURE,attr_sid);

				// beginScope pushes csn onto the node stack and connects it with its parent
				// this is done while the ls is off the stack so the parent of csn is ls's parent. 
				// afterward, we rearrange the top of stack to tuck ls back underneath csn in case it is 
				// needed for a subsequent procedure frame that is a sibling of csn in the tree.
				this.beginScope(csn);
				CallSiteScope csn2 = (CallSiteScope) this.scopeStack.pop();
				this.scopeStack.push(ls);
				this.scopeStack.push(csn2);

			} else {
				this.beginScope(procScope);
			}
		}
		else
		{
			SourceFile topSrcFile = (SourceFile)this.srcFileStack.peek();
			topSrcFile.setIsText(true);

			// P n="procname" [ln="linkname"] b="123" e="456" [vma=""]

			// make a new procedure scope object
			String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
			int firstLn = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
			int lastLn  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));

			boolean isalien = false;
			Scope procScope     = new ProcedureScope(this.experiment, ((SourceFile)this.srcFileStack.peek()), 
					firstLn-1, lastLn-1, name, isalien);

			this.beginScope(procScope);
		}
	}

	/**
	 * Simulate hascode for ProcedureScope
	 * @param sFilename
	 * @param firstLn
	 * @param lastLn
	 * @return
	 */
	private int getProcHashcode (String sFilename, String sProc) {
		String sHashcode = sFilename + "/" + sProc;
		int sid = sHashcode.hashCode();
		return sid;
	}

	/*************************************************************************
	 *	Finishes processing a P (procedure) element.
	 ************************************************************************/

	private void end_P()
	{
		this.endScope();
	}



	/*************************************************************************
	 *	Begins processing a A (alien) element.
	 ************************************************************************/

	private void begin_A(String[] attributes, String[] values)
	{
		// A f="filename" n="procname" b="257" e="259" [vma=""]

		// make a new alien scope object
		String filenm = getAttributeByName(FILENAME_ATTRIBUTE, attributes, values);
		String procnm = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		int firstLn = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
		int lastLn  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));

		SourceFile index = this.getOrCreateSourceFile(filenm);
		Scope alienScope = new AlienScope(this.experiment, index, filenm, procnm, firstLn-1, lastLn-1);

		this.beginScope(alienScope);
	}


	/*************************************************************************
	 *	Finishes processing a A (alien) element.
	 ************************************************************************/

	private void end_A()
	{
		this.srcFileStack.pop();
		this.endScope();
	}


	/*************************************************************************
	 *	Begins processing an L (loop) element.
	 ************************************************************************/

	private void begin_L(String[] attributes, String[] values)
	{
		int firstLn = 0; // = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
		int lastLn = 0; //  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));
		int sid = 0;
		for (int i=0; i<attributes.length; i++) {
			// some old xml database format contains 'sid' attribute for loops
			if (attributes[i].equals(SID_ATTRIBUTE)) {
				try {
					sid = Integer.parseInt(values[i]);
				} catch (Exception e) {
					//sid = -1; // sid is not a recognized format
				}
			} else if (attributes[i].equals(BEGIN_LINE_ATTRIBUTE)) {
				firstLn = Integer.parseInt(values[i]);
			} else if (attributes[i].equals(END_LINE_ATTRIBUTE)) {
				lastLn = Integer.parseInt(values[i]);
			}
		}
		SourceFile sourceFile = (SourceFile)this.srcFileStack.peek();
		if (this.csviewer) {
			// Use the source file of the Procedure Frame
			// NOTE: the current scope (i.e. the parent of this
			// nascent loop scope) should be either a procedure frame
			// or a loop that recursively obtained its file from the
			// procedure frame.
			Scope frameScope = this.getCurrentScope();
			//while ( !(frameScope instanceof ProcedureScope) ) {
			//  frameScope = frameScope.getParentScope();
			//}
			sourceFile = frameScope.getSourceFile();
		}
		
		Scope loopScope; // = new LoopScope(this.experiment, sourceFile, firstLn-1, lastLn-1);
		if (sid == 0) 
			loopScope = new LoopScope(this.experiment, sourceFile, firstLn-1, lastLn-1);
		else
			loopScope = new LoopScope(this.experiment, sourceFile, firstLn-1, lastLn-1, sid);
		this.beginScope(loopScope);
	}

	/*************************************************************************
	 *	Finishes processing an L (loop) element.
	 ************************************************************************/

	private void end_L()
	{
		this.endScope();
	}

	/*************************************************************************
	 *	Begins processing an LN (line) element.
	 ************************************************************************/
	private void begin_S(String[] attributes, String[] values)
	{
		begin_S_internal( attributes,  values, false);
	}
	private void begin_S_internal(String[] attributes, String[] values, boolean isCallSite)
	{

		// System.out.println("begin line");
		if(csviewer)
		{
			boolean istext = false; 
			String[] attr_file     = new String[1];
			String fileLine;
			String[] attr_function = new String[3];
			String[] val_function  = new String[3];
			String[] attr_line     = new String[2];
			String[] val_line      = new String[2];

			attr_file[0]= "n";
			fileLine ="unknown file line"; 

			attr_function[0]="n";
			attr_function[1]="b";
			attr_function[2]="e";

			val_function[0]="unknown procedure";
			val_function[1]="0";
			val_function[2]="0";

			attr_line[0]="b";
			attr_line[1]="e";

			val_line[0]="0";
			val_line[1]="0";

			for(int i=0; i<attributes.length; i++) {
				if(attributes[i].equals("f")) { 
					istext = true;
					fileLine = values[i]; 
				}
				else if(attributes[i].equals("lm")) { 
					istext = false;
					fileLine = values[i]; 
				}
				else if(attributes[i].equals("p")) {
					val_function[0] = values[i];
				}
				else if(attributes[i].equals("l")) {
					val_function[1]=values[i];
					val_function[2]=values[i];
					val_line[0] = values[i];
					val_line[1] = values[i];	 
				} 
			}

			SourceFile srcFile = this.getFileForCallsite(fileLine);

			srcFile.setIsText(istext);

			// make a new statement-range scope object
			int firstLn = Integer.parseInt(val_line[0]);
			int lastLn  = Integer.parseInt(val_line[1]);
			int sid = this.getLineHashcode(fileLine, firstLn, lastLn);
			
			Scope scope;
			if( firstLn == lastLn )
				scope = new LineScope(this.experiment, srcFile, firstLn-1, sid);
			else
				scope = new StatementRangeScope(this.experiment, srcFile, 
						firstLn-1, lastLn-1, sid);

			if (isCallSite) {
				this.beginScope_internal(scope, false);

				//System.out.println("beginning a call site");
				//System.out.println(this.getCurrentScope().getName());
			} else {
				this.beginScope(scope);
			}
		} else {
			SourceFile topSrcFile = (SourceFile)this.srcFileStack.peek();
			topSrcFile.setIsText(true);

			int firstLn = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
			int lastLn  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));
			int sid = this.getLineHashcode(topSrcFile.getName(), firstLn, lastLn);
			
			Scope scope;
			if( firstLn == lastLn )
				scope = new LineScope(this.experiment, ((SourceFile)this.srcFileStack.peek()), firstLn-1, sid);
			else
				scope = new StatementRangeScope(this.experiment, ((SourceFile)this.srcFileStack.peek()), 
						firstLn-1, lastLn-1, sid);

			this.beginScope(scope);
		}
	}

	/**
	 * Simulate hascode for LineScope and StatementRangeScope
	 * @param sFilename
	 * @param firstLn
	 * @param lastLn
	 * @return
	 */
	private int getLineHashcode (String sFilename, int firstLn, int lastLn) {
		String sHashcode = sFilename + ":" + firstLn + "-" + lastLn;
		int sid = sHashcode.hashCode();
		return sid;
	}
	/*************************************************************************
	 *	Finishes processing an S (line) element.
	 ************************************************************************/

	private void end_S()
	{
		this.endScope(); 
	}




	/*************************************************************************
	 *	Processes an M (metric value) element.
	 ************************************************************************/

	private void do_M(String[] attributes, String[] values)
	{
		// m n="abc" v="4.56e7"
		// add a metric value to the current scope
		String internalName = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		String value = getAttributeByName(VALUE_ATTRIBUTE, attributes, values);
		double actualValue  = Double.valueOf(value).doubleValue();
		
		if (this.csviewer) {
			Metric metric = this.experiment.getMetric(internalName);
			
			// get the sample period
			String prd_string =  metric.getSamplePeriod();
			double prd=Double.valueOf(prd_string).doubleValue();
			// System.out.println(prd); 
			// multiple by sample period 
			actualValue = prd * actualValue;
			MetricValue metricValue = new MetricValue(actualValue);
			this.getCurrentScope().setMetricValue(metric.getIndex(), metricValue);

			// update also the self metric value
			int intShortName = Integer.parseInt(internalName);
			int newShortName = intShortName + this.maxNumberOfMetrics;
			String selfShortName = "" + newShortName;

			Metric selfMetric = this.experiment.getMetric(selfShortName); 
			MetricValue selfMetricValue = new MetricValue(actualValue);
			this.getCurrentScope().setMetricValue(selfMetric.getIndex(), selfMetricValue);  
		} else {
			Metric metric = this.experiment.getMetric(internalName);
			MetricValue metricValue = new MetricValue(actualValue);
			this.getCurrentScope().setMetricValue(metric.getIndex(), metricValue);
		}
	}



//	SCOPE TREE BUILDING													//





	/*************************************************************************
	 *	Adds a newly parsed scope to the scope tree.
	 ************************************************************************/
	private void beginScope(Scope scope)
	{
		beginScope_internal(scope, true);
	}

	private void beginScope_internal(Scope scope, boolean addToTree)
	{
		Scope top = this.getCurrentScope();
		if (addToTree) {
			top.addSubscope(scope);
			scope.setParentScope(top);
		}
		this.scopeStack.push(scope);

		if (addToTree) recordOuterScope(scope);
	}



	/*************************************************************************
	 *	Ends a newly parsed scope.
	 ************************************************************************/

	private void endScope()
	{
		this.scopeStack.pop();
		// System.out.println("pop scope ");
	}

	/*************************************************************************
	 * Begin a new CALLSITE
	 ************************************************************************/


	private void begin_CALLSITE(String[] attributes, String[] values) {  
		this.begin_S_internal(attributes, values, true);  
	}


	/*************************************************************************
	 *   Get the File for a callsite 	
	 *   Using Hashtable to store the "FileSystemSourceFile" object 
	 *   for the callsite's file attribute.
	 ************************************************************************/
	protected SourceFile getFileForCallsite(String fileLine)
	{
		SourceFile sourceFile=(SourceFile)hashSourceFileFromName.get(fileLine);
		if (sourceFile == null) {
			int index = this.hashSourceFileFromKey.size();
			File filename = new File(fileLine);
			sourceFile = new FileSystemSourceFile(experiment, filename, index);
			hashSourceFileFromName.put(fileLine,sourceFile);
			this.hashSourceFileFromKey.put(Integer.valueOf(index), sourceFile);
		}  
		//this.fileList.add(sourceFile);
		return sourceFile;
	}

	/*************************************************************************
	 * 	end a callsite.
	 ************************************************************************/
	private void end_CALLSITE() 
	{
		end_S();
	}


	/*************************************************************************
	 *	Returns the current scope.
	 ************************************************************************/

	private Scope getCurrentScope()
	{
		return (Scope) this.scopeStack.peek();
	}

	private void recordOuterScope(Scope scope) {
		this.scopeList.add(scope);
	}

	protected void myDebug( boolean debugFlag,
			String msg) {
		if (debugFlag) {
			System.out.println(msg);
		}
	}

	// treat XML attributes like a named property list; this is an alternative to a brittle
	// position-based approach for recognizing attributes
	private String getAttributeByName(String name, String[] attributes, String[] values)
	{
		for (int i = 0; i < attributes.length; i++) if (name == attributes[i]) return values[i];
		return null;
	}


}

