
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
import edu.rice.cs.hpc.data.experiment.xml.DatabaseToken.TokenXML;
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


/**
 *
 * Builder for an XML parser for HPCView experiment files.
 *
 */


public class ExperimentDatabaseBuilder extends Builder
{

	//final static String BEGIN_LINE_ATTRIBUTE 	= "b";
	//final static String END_LINE_ATTRIBUTE 		= "e";
	private final static String LINE_ATTRIBUTE			= "l";
	private final static String NAME_ATTRIBUTE 			= "n";
	private final static String FILENAME_ATTRIBUTE 		= "f";
	private final static String VALUE_ATTRIBUTE 		= "v";
	

	/** The experiment to own parsed objects. */
	protected Experiment experiment;

	/** The default name for the experiment, in case none is found by the parser. */
	protected String defaultName;

	/** The parsed configuration. */
	protected ExperimentConfiguration configuration;

	/** The parsed search paths. */
	protected List/*<File>*/ pathList;

	/** The parsed source file objects. */
	protected List/*<SourceFile>*/ fileList;

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
	//protected StackScope/*<Scope>*/ stack;
	protected Stack/*<Scope>*/ stack;

	/** The current source file while parsing. */
	protected Stack/*<SourceFile>*/ srcFileStack;

//	johnmc
	protected Hashtable/*<String, SourceFile>*/ FileHashTable = new Hashtable();

	/** Number of metrics provided by the experiment file.
    For each metric we will define one inclusive and one exclusive metric.*/
	protected int numberOfPrimaryMetrics; 

	/** Maximum number of metrics provided by the experiment file.
    We use the maxNumberOfMetrics value to generate short names for the self metrics*/
	protected int maxNumberOfMetrics;

	// Laks
	private DatabaseToken.TokenXML previousToken = TokenXML.T_INVALID_ELEMENT_NAME;
	private DatabaseToken.TokenXML previousState = TokenXML.T_INVALID_ELEMENT_NAME;
	//private DatabaseToken.TokenXML currentToken = TokenXML.T_INVALID_ELEMENT_NAME;
	private boolean csviewer;

//	INITIALIZATION							//



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

	public ExperimentDatabaseBuilder(Experiment experiment, String defaultName)
	{
		super();

		// creation arguments
		this.experiment = experiment;
		this.defaultName = defaultName;

		this.csviewer = false;
		// temporary storage for parsed objects
		this.pathList   = new ArrayList/*<File>*/();
		this.fileList   = new ArrayList/*<SourceFile>*/();
		this.metricList = new ArrayList/*<Metric>*/();
		this.scopeList  = new ArrayList/*<Scope>*/();

		// parse action data structures
		this.stack = new Stack/*<Scope>*/();
		//this.stack = new StackScope/*<Scope>*/();
		this.srcFileStack = new Stack/*<SourceFile>*/();
		this.srcFileStack.push(null); // mimic old behavior

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
	 * @throws OldXMLFormatException 
	 ************************************************************************/

	public void beginElement(String element, String[] attributes, String[] values) 
	{
		TokenXML current = DatabaseToken.map(element);
		//currentToken = current;
		switch(current)
		{
		case T_HPCTOOLKIT_EXPERIMENT:
			break;
		case T_HEADER:
			this.do_Header(attributes,values);	
			break;
		case T_INFO:
			this.do_Info();
			break;
		case T_NAME_VALUE:
			this.do_NV(attributes, values);
			break;
		
		// CONFIG elements
		case T_METRIC:
			this.do_METRIC(attributes, values);	break;
			// PGM elements
		case T_SEC_FLAT_PROFILE_DATA:
		case T_SEC_CALLPATH_PROFILE_DATA:
			this.begin_PGM(attributes, values);	break;
		case T_LM:
			this.begin_LM (attributes, values);	break;
		case T_F:
			this.begin_F  (attributes, values);	break;
		case T_P:
		case T_PR:
		case T_PF:
			this.begin_PF  (attributes, values);	break;
		case T_A:
			this.begin_A  (attributes, values);	break;
		case T_L:
			this.begin_L  (attributes, values);	break;
		case T_S:
			this.begin_S  (attributes, values);	break;
		case T_M:
			this.do_M     (attributes, values);	break;

			// callstack elements
		case T_SEC_CALLPATH_PROFILE:
			// we need to retrieve the profile name and the ID
			this.csviewer = true;
			break;

		case T_C:
			//System.out.println("callSite...");
			this.begin_CALLSITE(attributes,values); 
			break;
			
			// old token from old XML
		case T_CSPROFILE:
			throw new java.lang.RuntimeException(new OldXMLFormatException());
			// unknown elements

		// ---------------------
		// Tokens to be ignored 
		// ---------------------
		case T_SEC_HEADER:
		case T_SEC_FLAT_PROFILE:
		case T_METRIC_TABLE:
			break;
		
		default:
			this.error();
		break;
		} 
		// laks: preserve the state of the current token for the next parsing state
		this.previousToken = current;

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
		TokenXML current = DatabaseToken.map(element);
		//currentToken = current;
		switch(current)
		{
		// PGM elements
		case T_SEC_FLAT_PROFILE_DATA:
		case T_SEC_CALLPATH_PROFILE_DATA:
			this.end_PGM();
			break;
		case T_LM:
			this.end_LM();
			break;
		case T_F:
			this.end_F();
			break;
			
		case T_P:
		case T_PR:
		case T_PF:
			this.end_PF();
			break;
		case T_A:
			this.end_A();
			break;
		case T_L:
			this.end_L();
			break;
		case T_S:
			this.end_S();
			break;
		case T_C: 		
			this.end_CALLSITE();
			break;

			// ignored elements
		case T_M:
			break;
		case T_HPCTOOLKIT_EXPERIMENT:
			break;
		case T_NAME_VALUE:
		case T_HEADER:
		case T_INFO:
		case T_METRIC_TABLE:
		case T_SEC_HEADER:
		case T_METRIC:
		case T_SEC_FLAT_PROFILE:
		case T_SEC_CALLPATH_PROFILE:
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
			this.stack.pop();
			//System.out.println("end:"+parser.getLineNumber()+"\t"+this.stack.size());
		} catch (EmptyStackException e) {
			System.err.println("ExperimentBuilder: no root scope !");
		}
		
		// check that input was properly nested
		if (!this.stack.empty()) {
			Scope topScope = (Scope) this.stack.peek();
			System.out.println("Stack is not empty; remaining top scope = " + topScope.getName());
			this.error();
		}

		// check semantic constraints
		if( this.fileList.size() == 0 ) {
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
		this.experiment.setSourceFiles(this.fileList);
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





//	BUILDING															//





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

	private void do_Header(String[] attributes, String[] values)
	{
		// TITLE name = "experiment title"
		this.Assert(attributes.length == 1);

		this.configuration.setName(values[0]);
	}


	/*************************************************************************
	 *	Processes a METRIC element.
	 *    <!ELEMENT Metric (MetricFormula?, Info?) >
    	  <!ATTLIST Metric
              i    CDATA #REQUIRED
              n    CDATA #REQUIRED
              fmt  CDATA #IMPLIED
              show (1|0) "1">

	 ************************************************************************/


	private void do_METRIC(String[] attributes, String[] values)
	{
		//if(this.csviewer)
		{
			int nID = 0;	// 1st index of values = metric ID
			int nName = 1;	// 2nd index of values = metric name
			int nbMetrics = this.metricList.size();
			// set the inclusive metric
			Metric metricInc = new Metric(this.experiment,
					values[nID],			// short name
					values[nName],			// native name
					values[nName] + " (I)", 	// display name
					true, true, 			// displayed ? percent ?
					"",						// period (not defined at the moment)
					MetricType.INCLUSIVE, nbMetrics+1);
			this.metricList.add(metricInc);
			
			// set the exclusive metric
			int iSelf = Integer.parseInt(values[nID]) + this.maxNumberOfMetrics;
			String sSelfName = "" + iSelf;
			String sSelfDisplayName = values[nName] + " (E)";
			Metric metricExc = new Metric(this.experiment,
					sSelfName,			// short name
					sSelfDisplayName,	// native name
					sSelfDisplayName, 	// display name
					true, true, 		// displayed ? percent ?
					"",					// period (not defined at the moment)
					MetricType.EXCLUSIVE, nbMetrics);
			this.metricList.add(metricExc);
		}
		/*else
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
		}*/
	}




	/*************************************************************************
	 *	Begins processing a PGM (program) element.
	 ************************************************************************/

	private void begin_PGM(String[] attributes, String[] values) 
	{
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		
		this.experiment.setMetrics(this.metricList);

		// make the root scope
		this.rootScope = new RootScope(this.experiment, name,"Invisible Outer Root Scope", RootScopeType.Invisible);
		this.stack.push(this.rootScope);	// don't use 'beginScope'
		//System.out.println("begin_PGM:"+parser.getLineNumber()+"\t"+this.stack.size());
		if (this.csviewer) {
			// create Calling Context Tree scope
			this.callingContextViewRootScope  = new RootScope(this.experiment, name,"Calling Context View", RootScopeType.CallingContextTree);
			beginScope(this.callingContextViewRootScope);
		} else {
			// flat scope
			this.flatViewRootScope = new RootScope(this.experiment, name, "Flat View", RootScopeType.Flat);
			this.beginScope(this.flatViewRootScope);	
		}
	}




	/*************************************************************************
	 *	Finishes processing a PGM (program) element.
	 ************************************************************************/

	private void end_PGM()
	{
		this.endScope();
	}



	/*************************************************************************
	 *	Begins processing an LM (load module) element.
	 ************************************************************************/

	private void begin_LM(String[] attributes, String[] values)
	{
		// LM n="load module name"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		
		// make a new load module scope object
		Scope lmScope = new LoadModuleScope(this.experiment, name);
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
	 *	Begins processing an F (file) element.
	 ************************************************************************/
	private void begin_F(String[] attributes, String[] values)

	{
		// F n="filename"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);

		// make a new source file object
		File filename = new File(name);
		SourceFile sourceFile = new FileSystemSourceFile(experiment, filename);
		this.fileList.add(sourceFile);
		this.srcFileStack.push(sourceFile);

		// make a new file scope object
		Scope fileScope = new FileScope(this.experiment, sourceFile);

		this.beginScope(fileScope);
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

	private void begin_PF(String[] attributes, String[] values)
	{
		//if(this.csviewer)
		{
			// <PROCEDURE_FRAME sid="" f="filename" p="procname" alien="false">

			boolean istext = false; 
			boolean isalien = false; 

			int      attr_sid      = 0;
			int firstLn = 0, lastLn = 0;
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
				if (attributes[i].equals("s")) { 
					attr_sid = Integer.parseInt(values[i]); 
				}
				if(attributes[i].equals("f")) { 
					istext = true;
					fileLine = values[i]; 
				}
				else if(attributes[i].equals("lm")) { 
					istext = false;
					fileLine = values[i]; 
				}
				else if(attributes[i].equals("p") || attributes[i].equals("n")) {
					val_function[0] = values[i];
				}
				else if(attributes[i].equals("l")) {
					StatementRange objRange = new StatementRange(values[i]);
					firstLn = objRange.getFirstLine();
					lastLn = objRange.getLastLine();
				} else if(attributes[i].equals("a")) { 
					if (values[i].equals("1")) {
						isalien = true;
					}
				} else if(attributes[i].equals("v")) {
					String sV = attributes[i];
					int iComma = sV.indexOf(",");
					while(iComma>=0) {
						val_line[0] = sV.substring(0, iComma-1);
						val_line[1] = sV.substring(iComma+1);
					}
				}
			}

			SourceFile srcFile = this.getFileForCallsite(fileLine);
			srcFile.setIsText(istext);
			this.srcFileStack.add(srcFile);

			Scope procScope  = new ProcedureScope(this.experiment, (SourceFile)srcFile, 
					firstLn-1, lastLn-1, 
					val_function[0], attr_sid, isalien);

			/** Laks 2008.08.25: original code
			 * 			Scope procScope  = new ProcedureScope(this.experiment, (SourceFile)srcFile, 
					firstLn-1, lastLn-1, 
					val_function[0], isalien);
			 */
			if (this.stack.peek() instanceof LineScope) {

				//System.out.println("CallSiteScope Building..."+((Scope) this.stack.peek()).getName());

				LineScope ls = (LineScope)this.stack.pop();
				CallSiteScope csn = new CallSiteScope((LineScope) ls, (ProcedureScope) procScope, CallSiteScopeType.CALL_TO_PROCEDURE);

				// beginScope pushes csn onto the node stack and connects it with its parent
				// this is done while the ls is off the stack so the parent of csn is ls's parent. 
				// afterward, we rearrange the top of stack to tuck ls back underneath csn in case it is 
				// needed for a subsequent procedure frame that is a sibling of csn in the tree.
				this.beginScope(csn);
				CallSiteScope csn2 = (CallSiteScope) this.stack.pop();
				this.stack.push(ls);
				this.stack.push(csn2);
				//System.out.println("begin_PF-Linescope:"+parser.getLineNumber()+"\t"+this.stack.size());

			} else {
				this.beginScope(procScope);
			}
		}
		/*else
		{
			SourceFile topSrcFile = (SourceFile)this.srcFileStack.peek();
			topSrcFile.setIsText(true);

			// P n="procname" [ln="linkname"] b="123" e="456" [vma=""]

			// make a new procedure scope object
			String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
			int firstLn = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
			int lastLn  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));

			boolean isalien = false;
			Scope procScope     = new ProcedureScope(this.experiment, (SourceFile)this.srcFileStack.peek(), firstLn-1, lastLn-1, name, isalien);

			this.beginScope(procScope);
		} */
	}


	/*************************************************************************
	 *	Finishes processing a P (procedure) element.
	 ************************************************************************/

	private void end_PF()
	{
		this.srcFileStack.pop();
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
		String sLine = getAttributeByName(LINE_ATTRIBUTE, attributes, values);
		int firstLn, lastLn;
		StatementRange objRange = new StatementRange(sLine);
		firstLn = objRange.getFirstLine();
		lastLn = objRange.getLastLine();

		File file = new File(filenm);
		SourceFile sourceFile = new FileSystemSourceFile(experiment, file);
		sourceFile.setIsText(true);
		this.fileList.add(sourceFile);
		this.srcFileStack.push(sourceFile);

		Scope alienScope = new AlienScope(this.experiment, sourceFile, filenm, procnm, firstLn-1, lastLn-1);

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
		String sID;
		int firstLn = 0;
		int lastLn = 0;
		for(int i=0; i<attributes.length; i++) {
			if(attributes[i].equals("s")) {
				sID = values[i];
			} else if(attributes[i].equals("l")) {
				String sLine = values[i];
				StatementRange objRange = new StatementRange( sLine );
				firstLn = objRange.getFirstLine();
				lastLn = objRange.getLastLine();				
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
		Scope loopScope = new LoopScope(this.experiment, sourceFile, firstLn-1, lastLn-1);

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
		//if(csviewer)
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

			SourceFile srcFile = (SourceFile)this.srcFileStack.peek();
			/*
			Scope scopePrevious = this.stack.peek();
			if(scopePrevious instanceof ProcedureScope) {
				srcFile = ( (ProcedureScope)scopePrevious ).getSourceFile();
			} else {
				srcFile = this.getFileForCallsite(fileLine);
			} */
			//System.out.println("Callsite "+values[0]+"\t"+scopePrevious.getClass()+"\t"+srcFile.getName()+"\t"+srcFile.isText());
			
			//srcFile.setIsText(istext);

			// make a new statement-range scope object
			int firstLn = Integer.parseInt(val_line[0]);
			int lastLn  = Integer.parseInt(val_line[1]);

			Scope scope;
			if( firstLn == lastLn )
				scope = new LineScope(this.experiment, srcFile, firstLn-1);
			else
				scope = new StatementRangeScope(this.experiment, srcFile, 
						firstLn-1, lastLn-1);

			if (isCallSite) {
				this.beginScope_internal(scope, false);

				//System.out.println("beginning a call site");
				//System.out.println(this.getCurrentScope().getName());
			} else {
				this.beginScope(scope);
			}
		} /*else {
			SourceFile topSrcFile = (SourceFile)this.srcFileStack.peek();
			topSrcFile.setIsText(true);

			int firstLn = Integer.parseInt(getAttributeByName(BEGIN_LINE_ATTRIBUTE, attributes, values));
			int lastLn  = Integer.parseInt(getAttributeByName(END_LINE_ATTRIBUTE, attributes, values));

			Scope scope;
			if( firstLn == lastLn )
				scope = new LineScope(this.experiment, (SourceFile)this.srcFileStack.peek(), firstLn-1);
			else
				scope = new StatementRangeScope(this.experiment, (SourceFile)this.srcFileStack.peek(), firstLn-1, lastLn-1);

			this.beginScope(scope);
		} */
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
		
		//if (this.csviewer) 
		{
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
		} /* else {
			Metric metric = this.experiment.getMetric(internalName);
			MetricValue metricValue = new MetricValue(actualValue);
			this.getCurrentScope().setMetricValue(metric.getIndex(), metricValue);
		} */
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
		this.stack.push(scope);
		//System.out.println("begin_"+scope.getName()+":"+parser.getLineNumber()+"\t"+this.stack.size());

		if (addToTree) recordOuterScope(scope);
	}



	/*************************************************************************
	 *	Ends a newly parsed scope.
	 ************************************************************************/

	private void endScope()
	{
		try {
			Scope scope = (Scope) this.stack.peek();
			//System.out.println("end-scope "+scope.getName()+":"+parser.getLineNumber()+"\t"+this.stack.size());
			this.stack.pop();
		} catch (java.util.EmptyStackException e) {
			System.out.println("End of stack:"+this.parser.getLineNumber());
		}
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
		SourceFile sourceFile=(SourceFile)FileHashTable.get(fileLine);
		if (sourceFile == null) {
			File filename = new File(fileLine);
			sourceFile = new FileSystemSourceFile(experiment, filename);
			FileHashTable.put(fileLine,sourceFile);
		}  
		this.fileList.add(sourceFile);
		return sourceFile;
	}

	/*************************************************************************
	 * 	end a callsite.
	 ************************************************************************/
	private void end_CALLSITE() 
	{
		end_S();
	}

	/**
	 * Laks: special treatement when NV is called under INFO
	 * @param attributes
	 * @param values
	 */
	private void do_NV(String[] attributes, String[] values) {
		if(this.previousState == TokenXML.T_METRIC) {
			// previous state is metric. The attribute should be about periodicity or flags
			if(values[0].startsWith("p")) {
				// get the sample period of this metric
				String sPeriod = values[1];
				int nbMetrics= this.metricList.size();
				if(nbMetrics > 1) {
					// get the current metric (inc)
					Metric metric = (Metric) this.metricList.get(nbMetrics-1);
					metric.setSamplePeriod(sPeriod);
					// get the current metric (exc)
					metric = (Metric) this.metricList.get(nbMetrics-2);
					metric.setSamplePeriod(sPeriod);
				}
			}
		}
	}
	
	private void do_Info() {
		this.previousState = this.previousToken;
	}
	/*************************************************************************
	 *	Returns the current scope.
	 ************************************************************************/

	private Scope getCurrentScope()
	{
		return (Scope) this.stack.peek();
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
	String getAttributeByName(String name, String[] attributes, String[] values)
	{
		for (int i = 0; i < attributes.length; i++) if (name == attributes[i]) return values[i];
		return null;
	}
	
	/**
	 * Class to treat a string of line or range of lines into two lines: first line and last line 
	 * @author laksonoadhianto
	 *
	 */
	private class StatementRange {
		private int firstLn;
		private int lastLn;
		
		public StatementRange(String sLine) {
			// find the range separator
			int iSeparator = sLine.indexOf('-');
			if(iSeparator > 0) {
				// separator exist, it should be a range
				this.firstLn = Integer.parseInt( sLine.substring(0,iSeparator) );
				this.lastLn = Integer.parseInt( sLine.substring(iSeparator+1) );
			} else {
				// no separator: no range
				this.firstLn = Integer.parseInt(sLine);
				this.lastLn = this.firstLn;
			}
		}
		
		public int getFirstLine( ) { return this.firstLn; }
		public int getLastLine( ) { return this.lastLn; }
	}

	/*
	private class StackScope extends java.util.Stack<Scope> {
		public Scope push(Scope scope) {
			super.push(scope);
			//System.out.println(this.size()+"\t"+currentToken.name() + " Push " + scope.getName() + "\t"+scope.getClass());
			return scope;
		}
		public Scope pop() {
			int nSize = this.size();
			if(nSize <= 0)
				System.out.println(this.size()+"\t"+ " Pop null ");
			/*else
				System.out.println(this.size()+"\t"+currentToken.name() + " Pop " + this.peek().getName());
			return super.pop();
		}
	} */

}

