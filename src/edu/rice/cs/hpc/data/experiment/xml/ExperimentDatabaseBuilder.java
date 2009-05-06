
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

	private final static String LINE_ATTRIBUTE			= "l";
	private final static String NAME_ATTRIBUTE 			= "n";
	private final static String FILENAME_ATTRIBUTE 		= "f";
	private final static String VALUE_ATTRIBUTE 		= "v";
	private final static String ID_ATTRIBUTE 		= "i";
	

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

	/** The parsed root scope object. */
	protected Scope rootScope;
	protected Scope callingContextViewRootScope;
	protected Scope callersViewRootScope;
	protected Scope flatViewRootScope;

	/** A stack to keep track of scope nesting while parsing. */
	protected Stack/*<Scope>*/ stack;

	/** The current source file while parsing. */
	protected Stack/*<SourceFile>*/ srcFileStack;

	// Laks 2009.05.04: we need to use a hash table to preserve the file dictionary
	protected Hashtable <Integer, SourceFile> hashSourceFileTable = new Hashtable<Integer, SourceFile>();

	/** Number of metrics provided by the experiment file.
    For each metric we will define one inclusive and one exclusive metric.*/
	protected int numberOfPrimaryMetrics; 

	/** Maximum number of metrics provided by the experiment file.
    We use the maxNumberOfMetrics value to generate short names for the self metrics*/
	final protected int maxNumberOfMetrics = 100;

	// Laks
	private DatabaseToken.TokenXML previousToken = TokenXML.T_INVALID_ELEMENT_NAME;
	private DatabaseToken.TokenXML previousState = TokenXML.T_INVALID_ELEMENT_NAME;
	//--------------------------------------------------------------------------------------
	private java.util.Hashtable<Integer, String> hashProcedureTable;
	private java.util.Hashtable<Integer, String> hashLoadModuleTable;
	//private java.util.Hashtable<Integer, String> hashFileTable;
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
		this.metricList = new ArrayList/*<Metric>*/();

		// parse action data structures
		this.stack = new Stack/*<Scope>*/();
		this.srcFileStack = new Stack/*<SourceFile>*/();
		this.srcFileStack.push(null); // mimic old behavior

		hashProcedureTable = new Hashtable<Integer, String>();
		hashLoadModuleTable = new Hashtable<Integer, String>();
		//hashFileTable = new Hashtable<Integer, String> ();
		
		numberOfPrimaryMetrics = 0;
		
		// laks hack: somehow, a scope object may want to know its source file.
		//			However, the source file is stored in hashSourceFileTable, so we 
		//			need to set this dummy table to the experiment for temporary usage
		this.experiment.setFileTable(this.hashSourceFileTable);
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

		case T_SEC_CALLPATH_PROFILE:
			// we need to retrieve the profile name and the ID
			this.csviewer = true;
			this.do_TITLE(attributes, values);
			break;

		case T_SEC_FLAT_PROFILE:
			this.csviewer = false;
			this.do_TITLE(attributes, values);
			break;

		// CONFIG elements
		case T_METRIC:
			this.do_METRIC(attributes, values);	break;
			// PGM elements
		case T_SEC_FLAT_PROFILE_DATA:
		case T_SEC_CALLPATH_PROFILE_DATA:
			this.begin_SecData(attributes, values);	break;

			// load module dictionary
		case T_LOAD_MODULE:
			this.do_LoadModule(attributes, values);
			break;
			// file dictionary
		case T_FILE:
			this.do_File(attributes, values); break;
			
			// flat profiles
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
		case T_C:
			this.begin_CALLSITE(attributes,values); 
			break;
			
		case T_PROCEDURE:
			this.do_Procedure(attributes, values); break;

			// old token from old XML
		case T_CSPROFILE:
		case T_HPCVIEWER:
			throw new java.lang.RuntimeException(new OldXMLFormatException());
			// unknown elements

		// ---------------------
		// Tokens to be ignored 
		// ---------------------
		case T_PROCEDURE_TABLE:
		case T_FILE_TABLE:
		case T_LOAD_MODULE_TABLE:
		case T_SEC_HEADER:
		case T_METRIC_FORMULA:
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
		switch(current)
		{
		case T_SEC_FLAT_PROFILE:
		case T_SEC_CALLPATH_PROFILE:
			break;

		// Data elements
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
		case T_HPCTOOLKIT_EXPERIMENT:
		case T_NAME_VALUE:
		case T_HEADER:
		case T_INFO:
		case T_METRIC_TABLE:
		case T_METRIC_FORMULA:
		case T_SEC_HEADER:
		case T_METRIC:
		case T_PROCEDURE_TABLE:
		case T_PROCEDURE:
		case T_FILE_TABLE:
		case T_FILE:
		case T_LOAD_MODULE_TABLE:
		case T_LOAD_MODULE:
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
		} catch (EmptyStackException e) {
			System.err.println("ExperimentBuilder: no root scope !");
		}
		
		// check that input was properly nested
		if (!this.stack.empty()) {
			Scope topScope = (Scope) this.stack.peek();
			System.out.println("Stack is not empty; remaining top scope = " + topScope.getName());
			this.error();
		}

		// copy parse results into configuration
		this.configuration.setSearchPaths(this.pathList);
		this.experiment.setConfiguration(this.configuration);

		//this.experiment.setScopes(this.scopeList, this.rootScope);
		this.experiment.setScopes(null, this.rootScope);
		
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


//	------------------------------- BUILDING		---------------------------//

	/*************************************************************************
	 *	Processes a TITLE element.
	 ************************************************************************/

	private void do_TITLE(String[] attributes, String[] values)
	{
		// <!ATTLIST SecCallPathProfile
		//        i CDATA #REQUIRED
		//        n CDATA #REQUIRED>
		this.Assert(attributes.length == 2);
		String sTitle = "";
		if(values.length == 2) {
			sTitle = values[1];
		}
		this.configuration.setName(sTitle);
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
	 *      Processes a FILE.
	 *          <!ELEMENT File (Info?)>
    <!ATTLIST File
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 ************************************************************************/
	private void do_File(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		String sID = values[0];		
		try {
			Integer objFileID = Integer.parseInt(sID);
			// just in case if there is a duplicate key in the dictionary, we need to make a test
			SourceFile sourceFile=(SourceFile) this.hashSourceFileTable.get(objFileID);
			if (sourceFile == null) {
				// theoretically, this condition is always true (unless a bug occurred in the hpcprof)
				String sFile = values[1];
				File filename = new File(sFile);
				int iID = objFileID.intValue();
				sourceFile = new FileSystemSourceFile(experiment, filename, iID);
				this.hashSourceFileTable.put(objFileID, sourceFile);
			}  
		} catch (Exception e) {
			
		}

		/*
		try {
			Integer objID = Integer.parseInt(sID);
			//this.hashFileTable.put(objID, sFile);
		} catch (java.lang.NumberFormatException e) {
			
		} */
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
		int nID = 0;	// 1st index of values = metric ID
		int nName = 1;	// 2nd index of values = metric name
		int nbMetrics = this.metricList.size();
		String sID = values[nID];
		int iSelf ;
		
		// somehow, the ID of the metric is not number, but asterisk
		if(sID.charAt(0)=='*') {
			// parsing an asterisk can throw an exception, which is annoying
			// so we make an artificial ID for this particular case
			iSelf = this.maxNumberOfMetrics;
			sID = "0";
		} else {
			iSelf = Integer.parseInt(values[nID]) + this.maxNumberOfMetrics;
		}
		// laks 2009.01.14: add variable to switch from callpath to flatpath
		String sDisplayName = values[nName];
		MetricType objType = MetricType.EXCLUSIVE;
		
		// Laks 2009.01.14: if the database is call path database, then we need
		//	to distinguish between exclusive and inclusive
		if(this.csviewer) {
			sDisplayName = sDisplayName + " (I)";
			objType = MetricType.INCLUSIVE;
		}
		// set the inclusive metric
		Metric metricInc = new Metric(this.experiment,
				sID,			// short name
				values[nName],			// native name
				sDisplayName, 	// display name
				true, true, 			// displayed ? percent ?
				"",						// period (not defined at the moment)
				objType, nbMetrics+1);
		this.metricList.add(metricInc);

		// Laks 2009.01.14: only for call path profile
		// Laks 2009.01.14: if the database is call path database, then we need
		//	to distinguish between exclusive and inclusive
		if (this.csviewer) {
			// set the exclusive metric
			String sSelfName = "" + iSelf;
			// Laks 2009.02.09: bug fix for not reusing the existing inclusive display name
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
	}

	/************************************************************************
	 * <!ELEMENT LoadModule (Info?)>
    <!ATTLIST LoadModule
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 * Example:
	 *   <LoadModule i="43497" n="/lib64/libc-2.7.so"/>
	 * @param attributes
	 * @param values
	 ************************************************************************/
	private void do_LoadModule(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		
		// We assume that the 1st attribute is always the ID and the 2nd attribute is the value
		String sValue = values[1];
		String sID = values[0];
		try {
			Integer objID = new Integer(sID);
			this.hashLoadModuleTable.put(objID, sValue);
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Incorrect load module ID: "+sID);
		} catch (java.lang.NullPointerException e) {
			System.err.println("load module table is empty. Key: "+sID+" value: "+sValue);
		}
	}

	/*************************************************************************
	 *	Begins processing a profile data (program) element.
	 ************************************************************************/
	private void begin_SecData(String[] attributes, String[] values) 
	{
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		
		this.experiment.setMetrics(this.metricList);

		// make the root scope
		this.rootScope = new RootScope(this.experiment, name,"Invisible Outer Root Scope", RootScopeType.Invisible);
		this.stack.push(this.rootScope);	// don't use 'beginScope'

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
		// laks: tell the experiment that we have finished with the dictionary
		this.experiment.setFileTable(this.hashSourceFileTable);
	}



	/*************************************************************************
	 *	Begins processing an LM (load module) element.
	 *	<!ATTLIST LM
                i CDATA #IMPLIED
                n CDATA #REQUIRED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_LM(String[] attributes, String[] values)
	{
		// LM n="load module name"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		String sIndex = getAttributeByName(ID_ATTRIBUTE, attributes, values);
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);
			// make a new load module scope object
			Scope lmScope = new LoadModuleScope(this.experiment, name, objIndex.intValue());
			File filename = new File(name);
			SourceFile sourceFile = new FileSystemSourceFile(experiment, filename, objIndex);

			this.hashSourceFileTable.put(objIndex, sourceFile);
			this.beginScope(lmScope);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	 *      <!ATTLIST F
                i CDATA #IMPLIED
                n CDATA #REQUIRED>
	 ************************************************************************/
	private void begin_F(String[] attributes, String[] values)

	{
		// F n="filename"
		String inode = getAttributeByName(ID_ATTRIBUTE, attributes, values);
		try {
			Integer objFileKey = Integer.parseInt(inode);
			// make a new file scope object
			Scope fileScope = new FileScope(this.experiment, objFileKey);
			SourceFile sourceFile  = (SourceFile) this.hashSourceFileTable.get(objFileKey);
			if (sourceFile == null ) {
				// make a new source file object
				String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
				File filename = new File(name);
				sourceFile = new FileSystemSourceFile(experiment, filename, objFileKey.intValue());
				this.hashSourceFileTable.put(objFileKey, sourceFile);
			}
			this.srcFileStack.push(sourceFile);

			this.beginScope(fileScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

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
	 * 
	 * @param attributes
	 * @param values
	 * <!ATTLIST Procedure
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 *************************************************************************/
	private void do_Procedure(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		String sID = values[0];
		String sData = values[1];

		try {
			Integer objID = Integer.parseInt(sID);
			this.hashProcedureTable.put(objID, sData);
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/*************************************************************************
	 *	Begins processing a PF (procedure frame) element.
	 *       <!ATTLIST Pr
                i  CDATA #IMPLIED
                s  CDATA #IMPLIED
                n  CDATA #REQUIRED
                lm CDATA #IMPLIED
                f  CDATA #IMPLIED
                l  CDATA #IMPLIED
                a  (1|0) "0"
                v  CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_PF(String[] attributes, String[] values)
	{
			boolean istext = true; 
			boolean isalien = false; 
			boolean hashtableExist = (this.hashProcedureTable.size()>0);
			int      attr_sid      = 0;
			int firstLn = 0, lastLn = 0;
			int keyFile = 0;	// the index key of the procedure to the dictionary
			SourceFile srcFile = null; // file location of this procedure
			
			String[] attr_file     = new String[1];
			//String fileLine = null;
			String[] attr_function = new String[3];
			String sProcName = "unknown procedure";
			String[] attr_line     = new String[2];

			attr_file[0]= "n";

			attr_function[0]="n";
			attr_function[1]="b";
			attr_function[2]="e";

			attr_line[0]="b";
			attr_line[1]="e";

			for(int i=0; i<attributes.length; i++) {
				if (attributes[i].equals("s")) { 
					attr_sid = Integer.parseInt(values[i]); 
				} else if (attributes[i].equals("i")) {
					// id of the proc frame. needs to cross ref
					String sID = values[i];
					String sData = this.hashProcedureTable.get(sID);
					if(sData != null) {
						// found the key, get the data from the database
					}
				}
				else if(attributes[i].equals("f")) {
					// file
					istext = true;
					//fileLine = values[i];
					try {
						int indexFile = Integer.parseInt(values[i]);
						keyFile = indexFile;
						srcFile = this.hashSourceFileTable.get(keyFile);
						/*
						String sValue = this.hashFileTable.get(Integer.valueOf(keyFile));
						if(sValue != null)
							fileLine = sValue;*/
					} catch (java.lang.NumberFormatException e) {
						// in this case, either the value of "f" is invalid or it is the name of the file
						keyFile = this.hashSourceFileTable.size()+1;
						System.out.println("Warning: the XML file has unsupported format for attribute 'f':"+values[i]+
								" replaced by the index: "+keyFile); 
						// e.printStackTrace();
					}
					
				}
				else if(attributes[i].equals("lm")) { 
					// load module
					if (!istext) {
						istext = false;
						//fileLine = values[i]; 
						try {
							// let see if the value of ln is an ID or a simple load module name
							int indexFile = Integer.parseInt(values[i]);
							keyFile = indexFile;
							// look at the dictionary for the name of the load module
							String sValue = this.hashLoadModuleTable.get(Integer.valueOf(keyFile));
							//if(sValue != null)
							//	fileLine = sValue;
						} catch (java.lang.NumberFormatException e) {
							keyFile = this.hashSourceFileTable.size()+1;
							// this error means that the lm is not based on dictionary
						}
					}
				}
				else if (attributes[i].equals("p") ) {
					// obsolete format: p is the name of the procedure
					sProcName = values[i];
				}
				else if(attributes[i].equals("n")) {
					// new database format: n is the flat ID of the procedure
					sProcName = values[i];
					if(hashtableExist) {
						try {
							Integer objProcID = Integer.parseInt(values[i]); 
							// get the real name of the procedure from the dictionary
							String sProc = this.hashProcedureTable.get(objProcID);
							if(sProc != null) {
								sProcName = sProc;
							}
						} catch (java.lang.NumberFormatException e) {
							
						}
						
					} 
					// in case of error, we just refer the ID as the name of the procedure
				}
				else if(attributes[i].equals("l")) {
					// line number (or range)
					StatementRange objRange = new StatementRange(values[i]);
					firstLn = objRange.getFirstLine();
					lastLn = objRange.getLastLine();
				} else if(attributes[i].equals("a")) { 
					// alien
					if (values[i].equals("1")) {
						isalien = true;
					}
				} else if(attributes[i].equals("v")) {
//					String sV = attributes[i];
//					int iComma = sV.indexOf(",");
//					while(iComma>=0) {
//						val_line[0] = sV.substring(0, iComma-1);
//						val_line[1] = sV.substring(iComma+1);
//					}
				}
			}
			if(srcFile == null) {
				srcFile = (SourceFile) this.srcFileStack.peek();
				keyFile = srcFile.getFileID();
			} /*else {
				srcFile = this.hashSourceFileTable.get(keyFile); //this.getFileForCallsite(fileLine, keyFile);
			} */
			 
			srcFile.setIsText(istext);
			this.srcFileStack.add(srcFile);

			Scope procScope  = new ProcedureScope(this.experiment, keyFile, 
					firstLn-1, lastLn-1, 
					sProcName, attr_sid, isalien);

			/** Laks 2008.08.25: original code
			 * 			Scope procScope  = new ProcedureScope(this.experiment, (SourceFile)srcFile, 
					firstLn-1, lastLn-1, 
					val_function[0], isalien);
			 */
			if (this.stack.peek() instanceof LineScope) {

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

			} else {
				this.beginScope(procScope);
			}
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
      <!ELEMENT A (A|L|S|C|M)*>      <!ATTLIST A
                i CDATA #IMPLIED
                f CDATA #IMPLIED
                n CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_A(String[] attributes, String[] values)
	{
		// make a new alien scope object
		String sIndex = getAttributeByName(ID_ATTRIBUTE, attributes, values);
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);

			String filenm = getAttributeByName(FILENAME_ATTRIBUTE, attributes, values);
			String procnm = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
			String sLine = getAttributeByName(LINE_ATTRIBUTE, attributes, values);
			int firstLn, lastLn;
			StatementRange objRange = new StatementRange(sLine);
			firstLn = objRange.getFirstLine();
			lastLn = objRange.getLastLine();

			SourceFile sourceFile = this.hashSourceFileTable.get(objIndex);
			if (sourceFile == null) {
				File file = new File(filenm);
				sourceFile = new FileSystemSourceFile(experiment, file, objIndex.intValue());
				sourceFile.setIsText(true);
				this.hashSourceFileTable.put(objIndex, sourceFile);
			}
			this.srcFileStack.push(sourceFile);

			Scope alienScope = new AlienScope(this.experiment, objIndex.intValue(), filenm, procnm, firstLn-1, lastLn-1);

			this.beginScope(alienScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
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
	 *	<!ELEMENT L (A|L|S|C|M)*>
      <!ATTLIST L
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_L(String[] attributes, String[] values)
	{
		String sID;
		int iID = 0;
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
			} else if(attributes[i].equals("i")) {
				sID = values[i];
				try {
					iID = Integer.parseInt(sID);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
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
		Scope loopScope = new LoopScope(this.experiment, sourceFile.getFileID(), firstLn-1, lastLn-1);

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
	 * <!ATTLIST S
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_S(String[] attributes, String[] values)
	{
		begin_S_internal( attributes,  values, false);
	}
	private void begin_S_internal(String[] attributes, String[] values, boolean isCallSite)
	{

		{
			String[] val_line      = new String[2];
			int id = 0;
			
			val_line[0]="0";
			val_line[1]="0";

			for(int i=0; i<attributes.length; i++) {
				if(attributes[i].equals("l")) {
					val_line[0] = values[i];
					val_line[1] = values[i];	 
				} else if(attributes[i].equals("i"))  {
					id = Integer.parseInt(values[i]);
				}
			}

			SourceFile srcFile = (SourceFile)this.srcFileStack.peek();

			// make a new statement-range scope object
			int firstLn = Integer.parseInt(val_line[0]);
			int lastLn  = Integer.parseInt(val_line[1]);

			Scope scope;
			if( firstLn == lastLn )
				scope = new LineScope(this.experiment, srcFile.getFileID(), firstLn-1);
			else
				scope = new StatementRangeScope(this.experiment, srcFile.getFileID(), 
						firstLn-1, lastLn-1);

			if (isCallSite) {
				this.beginScope_internal(scope, false);
			} else {
				this.beginScope(scope);
			}
		} 
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
		
		{
			Metric metric = this.experiment.getMetric(internalName);
			
			String prd_string =  metric.getSamplePeriod();
			// get the sample period
			double prd;
			// get the sample period
			try {
				if (  (prd_string != null) && (prd_string.length()>0) )
					prd=Double.valueOf(prd_string).doubleValue();
				else
					prd = 1.0;
			} catch (java.lang.NumberFormatException e) {
				prd = 0.0;
				System.err.println("Error metric number:"+prd_string);
				e.printStackTrace();
			}

			// multiple by sample period 
			actualValue = prd * actualValue;
			MetricValue metricValue = new MetricValue(actualValue);
			this.getCurrentScope().setMetricValue(metric.getIndex(), metricValue);

			// update also the self metric value for calling context only
			if (this.csviewer) {
				int intShortName = Integer.parseInt(internalName);
				int newShortName = intShortName + this.maxNumberOfMetrics;
				String selfShortName = "" + newShortName;

				Metric selfMetric = this.experiment.getMetric(selfShortName); 
				MetricValue selfMetricValue = new MetricValue(actualValue);
				this.getCurrentScope().setMetricValue(selfMetric.getIndex(), selfMetricValue);  
			}
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

	/****************************************************************
	 * 
	 * @param scope
	 * @param addToTree
	 *****************************************************************/
	private void beginScope_internal(Scope scope, boolean addToTree)
	{
		Scope top = this.getCurrentScope();
		if (addToTree) {
			top.addSubscope(scope);
			scope.setParentScope(top);
		}
		this.stack.push(scope);
	}



	/*************************************************************************
	 *	Ends a newly parsed scope.
	 ************************************************************************/
	private void endScope()
	{
		try {
			this.stack.pop();
		} catch (java.util.EmptyStackException e) {
			System.out.println("End of stack:"+this.parser.getLineNumber());
		}
	}

	/*************************************************************************
	 * Begin a new CALLSITE
	 * <!ATTLIST C
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_CALLSITE(String[] attributes, String[] values) {  
		this.begin_S_internal(attributes, values, true);  
	}


	/*************************************************************************
	 *   Get the File for a callsite 	
	 *   Using Hashtable to store the "FileSystemSourceFile" object 
	 *   for the callsite's file attribute.
	 ************************************************************************/
	protected SourceFile getFileForCallsite(String fileLine, int keyFile)
	{
		SourceFile sourceFile=(SourceFile) this.hashSourceFileTable.get(keyFile);
		if (sourceFile == null) {
			File filename = new File(fileLine);
			sourceFile = new FileSystemSourceFile(experiment, filename, keyFile);
			this.hashSourceFileTable.put(Integer.valueOf(keyFile), sourceFile);
		}  

		return sourceFile;
	}

	/*************************************************************************
	 * 	end a callsite.
	 ************************************************************************/
	private void end_CALLSITE() 
	{
		end_S();
	}

	/************************************************************************
	 * Laks: special treatement when NV is called under INFO
	 * @param attributes
	 * @param values
	 ************************************************************************/
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
	
	/************************************************************************
	 * 
	 ************************************************************************/
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

	// treat XML attributes like a named property list; this is an alternative to a brittle
	// position-based approach for recognizing attributes
	String getAttributeByName(String name, String[] attributes, String[] values)
	{
		for (int i = 0; i < attributes.length; i++) if (name == attributes[i]) return values[i];
		return null;
	}
	
	/*************************************************************************
	 * Class to treat a string of line or range of lines into two lines: first line and last line 
	 * @author laksonoadhianto
	 *
	 ************************************************************************/
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

}

